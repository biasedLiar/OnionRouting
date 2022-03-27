import javax.crypto.*;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public abstract class OnionEndPoint extends OnionParent{
    protected HashMap<String, PublicKey> keys;
    private ArrayList<String> socketStrings;
    protected ArrayList<byte[]> waitingMessages;
    public static int ONION_LAYERS = 10;



    public OnionEndPoint(int port, ArrayList<String> originalSocketStrings) {
        super(port);
        socketStrings = new ArrayList<>();
        for (String socketString :
                originalSocketStrings) {
            socketStrings.add(socketString);
        }
        keys = new HashMap<>();
        waitingMessages = new ArrayList<>();
    }



    public void recieveEncryption() throws IOException {
        msgBytes = new byte[1];
        msgBytes[0] = MessageMode.KEY_EXCHANGE.getValue();

        sendMessage();
        boolean waiting = true;
        while (waiting){
            recieveMessage();
            System.out.println(mode + " is the current mode");
            if (mode != MessageMode.KEY_EXCHANGE){
                System.out.println("Added to waiting");
                waitingMessages.add(msgBytes);
            } else {
                waiting = false;
                msg = new String(msgBytes);
            }
        }
    }

    public void recieveMessageUpdatePort() throws IOException {
        if (waitingMessages.size() == 0){
            recieveMessage();
            calculatePort();
        } else {
            msgBytes = waitingMessages.remove(0);
            msg = new String(msgBytes);
        }

    }
    
    public void singleKeyExchange(String socketString) throws IOException, NoSuchAlgorithmException {
        targetSocketString = socketString;
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
            keys.put(targetSocketString, pub);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void keyEchange() throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        for (String socketString :
                socketStrings) {
            System.out.println("STarting exchange with" + socketString);
            singleKeyExchange(socketString);
            if (mode != MessageMode.KEY_EXCHANGE){
                System.out.println("Wrong mode, waiting for keyexhange");
            }
        }
        System.out.println(myPort + " has " + keys.size() + " keys.");
    }

    public ArrayList<String> getRandomSockets(int numSockets){
        Collections.shuffle(socketStrings);
        if (socketStrings.size() < numSockets){
            return socketStrings;
        } else {
            return new ArrayList<>(socketStrings.subList(0, numSockets));
        }
    }



    public void addTargetSocketToMessage(){
        addSocketToMessage(targetSocketString);
    }

    public void addSourceSocketToMessage(){
        addSocketToMessage(myPort + " " + myAddress.getHostAddress());
    }

    public void addSocketToMessage(String socketString){
        byte[] socketBytes = new byte[6];
        socketBytes[0] = (byte) Math.floor(getPort(socketString)/256);
        socketBytes[1] = (byte) (getPort(socketString) % 256);
        String[] myStrings = getAddress(socketString).split("\\.");
        for (int i = 0; i < myStrings.length; i++) {
            socketBytes[2+i] = Byte.valueOf(myStrings[i]);
        }
        addToFrontOfMessage(socketBytes);
    }



    public void encryptMessage(String nodeSocket, MessageMode mode){
        try {
            addTargetSocketToMessage();

            //https://stackoverflow.com/questions/18228579/how-to-create-a-secure-random-aes-key-in-java
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();

            encryptWithAES(secretKey);
            encryptAesKeyWithRsa(secretKey, nodeSocket);
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



    public void encryptAesKeyWithRsa(SecretKey secretKey, String socketString){
        byte[] aesBytes = secretKey.getEncoded();

        try {
            rsaCipher.init(Cipher.ENCRYPT_MODE, keys.get(socketString));
            rsaCipher.update(aesBytes);
            aesBytes = rsaCipher.doFinal();
            //System.out.println("Length of encrypted message from klient: " + bytes.length);

            addToFrontOfMessage(aesBytes);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public void wrapMessage(MessageMode mode, int numOnionNodes){
        msgBytes = msg.getBytes();
        addSourceSocketToMessage();
        setMode(mode);
        ArrayList<String> onionSockets = getRandomSockets(numOnionNodes);
        for (int i = 0; i < onionSockets.size(); i++) {
            encryptMessage(onionSockets.get(i), mode);
            targetSocketString = onionSockets.get(i);
        }
        //System.out.println("MSGbytes length: " + msgBytes.length);
    }




}
