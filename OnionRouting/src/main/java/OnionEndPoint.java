import javax.crypto.*;
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

    public void addToFrontOfMessage(byte[] newBytes){
        byte[] tempBytes = new byte[newBytes.length + msgBytes.length];
        for (int i = 0; i < newBytes.length; i++) {
            tempBytes[i] = newBytes[i];
        }
        for (int i = 0; i < msgBytes.length; i++) {
            tempBytes[i + newBytes.length] = msgBytes[i];
        }
        msgBytes = tempBytes;
    }

    public void addPortToMessage(){
        byte[] portBytes = new byte[2];
        portBytes[0] = (byte) Math.floor(port/256);
        portBytes[1] = (byte) (port % 256);
        addToFrontOfMessage(portBytes);
    }

    public void encryptMessage(int nodePort, MessageMode mode){
        try {
            addPortToMessage();

            //https://stackoverflow.com/questions/18228579/how-to-create-a-secure-random-aes-key-in-java
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();

            encryptWithAES(secretKey);
            encryptAesKeyWithRsa(secretKey, nodePort, mode);
            setMode(mode);


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void encryptWithAES(SecretKey secretKey){//https://howtodoinjava.com/java/java-security/java-aes-encryption-example/
        try {
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            msgBytes = aesCipher.doFinal(msgBytes);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

    }

    public void setMode(MessageMode mode){
        byte[] modeByte = {mode.getValue()};
        addToFrontOfMessage(modeByte);
    }

    public void encryptAesKeyWithRsa(SecretKey secretKey, int nodePort, MessageMode mode){
        byte[] aesBytes = secretKey.getEncoded();

        try {
            rsaCipher.init(Cipher.ENCRYPT_MODE, keys.get(nodePort));
            rsaCipher.update(aesBytes);
            aesBytes = rsaCipher.doFinal();
            //System.out.println("Length of encrypted message from klient: " + bytes.length);

            addToFrontOfMessage(aesBytes);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public void wrapMessage(MessageMode mode, int numOnionPorts){
        msgBytes = msg.getBytes();
        ArrayList<Integer> onionPorts = getRandomPorts(numOnionPorts);
        for (int i = 0; i < onionPorts.size(); i++) {
            long startTime = System.nanoTime();
            encryptMessage(onionPorts.get(i), mode);
            long endTime = System.nanoTime();
            //System.out.println((endTime-startTime)/1000);
            port = onionPorts.get(i);
        }
        //Copy array into larger array
        //System.out.println("MSGbytes length: " + msgBytes.length);
    }




}
