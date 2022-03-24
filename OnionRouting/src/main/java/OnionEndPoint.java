import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public abstract class OnionEndPoint extends OnionParent{
    protected HashMap<Integer, PublicKey> keys;


    public OnionEndPoint(int port) {
        super(port);
        keys = new HashMap<>();
    }

    public void recieveEncryption() throws IOException {
        msgBytes = new byte[1];
        msgBytes[0] = MessageMode.KEY_EXCHANGE.getValue();
        System.out.println("Almost finsihsed sharing keys");
        sendMessage();

        recieveMessage();
    }

    public void keyEchange(int port, InetAddress address1) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.port = port;
        recieveEncryption();
        String[] splitMessage = msg.split("\n"); //https://attacomsian.com/blog/java-split-string-new-line
        BigInteger modulus = new BigInteger(splitMessage[0]);
        BigInteger exponent = new BigInteger(splitMessage[1]);

        //System.out.println("Server:\nModulus: " +  String.valueOf(modulus) + "\nExponent: " +  String.valueOf(exponent));
        //source: https://stackoverflow.com/questions/2023549/creating-rsa-keys-from-known-parameters-in-java
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        try {
            PublicKey pub = factory.generatePublic(spec);
            keys.put(port, pub);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getRandomPorts(int numPorts){
        ArrayList<Integer> ports = new ArrayList<>(keys.keySet());
        Collections.shuffle(ports);
        if (ports.size() < numPorts){
            return ports;
        } else {
            return new ArrayList<>(ports.subList(0, numPorts));
        }
    }

    public void addPortToMessage(){
        byte[] tempBytes = new byte[msgBytes.length + 2];
        tempBytes[0] = (byte) Math.floor(port/256);
        tempBytes[1] = (byte) (port % 256);
        System.out.println("ENcrypting. 0: " + tempBytes[0] + ", 1: " + tempBytes[1] + ", port: " + port);

        for (int i = 0; i < msgBytes.length; i++) {
            tempBytes[i + 2] = msgBytes[i];
        }
        msgBytes = tempBytes;
    }

    public void encryptMessage(int nodePort, MessageMode mode){
        try {
            addPortToMessage();
            cipher.init(Cipher.ENCRYPT_MODE, keys.get(nodePort));
            cipher.update(msgBytes);
            msgBytes = cipher.doFinal();
            //System.out.println("Length of encrypted message from klient: " + bytes.length);

            byte[] tempBytes = new byte[msgBytes.length+1];
            tempBytes[0] = mode.getValue();
            for (int i = 0; i < msgBytes.length; i++) {
                tempBytes[i + 1] = msgBytes[i];
            }
            msgBytes = tempBytes;

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public void wrapMessage(MessageMode mode, int numOnionPorts){
        msgBytes = msg.getBytes();
        ArrayList<Integer> onionPorts = getRandomPorts(numOnionPorts);
        for (int i = 0; i < onionPorts.size(); i++) {
            encryptMessage(onionPorts.get(i), mode);
            port = onionPorts.get(i);
        }
        //Copy array into larger array
        //System.out.println("MSGbytes length: " + msgBytes.length);
    }




}
