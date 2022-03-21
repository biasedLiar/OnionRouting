import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

public class OnionNode extends Thread{
    private DatagramSocket socket;
    private byte[] buf = new byte[2048];
    private byte[] buf2 = new byte[2048];
    private String encryptedMsg;
    private String msg;
    private InetAddress address;
    private int port;
    private String mode;
    private byte[] newBytes = new byte[244];
    private KeyPair pair;
    Cipher cipher;

    public OnionNode() throws SocketException {
        socket = new DatagramSocket(1251);
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);
        //System.out.println("Message recieved.");
        encryptedMsg = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Length of recieved: " + encryptedMsg.length());
        //unless specified otherwise, the response will be sent back;
        address = packet.getAddress();
        port = packet.getPort();
    }

    public void handleData() throws UnknownHostException, NoSuchAlgorithmException {

        //System.out.println("encrypted message: " + encryptedMsg + "\nEnd encrypted");
        String[] splitMessage = encryptedMsg.split("\n"); //https://attacomsian.com/blog/java-split-string-new-line
        for (String s :
                splitMessage) {
            System.out.println("From inside node:" + s);
        }
        if ((mode = splitMessage[0]).equals("E")){
            //Exchange keys
            //Source: https://www.tutorialspoint.com/java_cryptography/java_cryptography_quick_guide.htm
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            pair = keyPairGen.generateKeyPair();
            PublicKey publicKey = pair.getPublic();
            String modulus = String.valueOf(((RSAPublicKey) publicKey).getModulus());
            String exponent = String.valueOf(((RSAPublicKey) publicKey).getPublicExponent());
            msg = modulus + "\n" + exponent;


        } else {
            //Forward message
            //address = InetAddress.getByName(splitMessage[1]);
            decryptData(String.join("\n", Arrays.copyOfRange(splitMessage, 3, splitMessage.length)).getBytes());
            address = InetAddress.getByName("localhost");
            port = Integer.parseInt(splitMessage[2]);
            msg = String.join("\n", Arrays.copyOfRange(splitMessage, 3, splitMessage.length));
        }
    }

    public void decryptData(byte[] encryptedBytes){
        try {
            cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
            msg = new String(cipher.doFinal(encryptedBytes));
            System.out.println("We decoded message: " + msg);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

    }



    public void forwardMessage() throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }




    public void run(){
        try {

            while (true){
                recieveMessage();
                handleData();
                if (mode.equals("F")){
                    forwardMessage();
                } else {
                    forwardMessage();
                }

            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



        socket.close();
    }
}
