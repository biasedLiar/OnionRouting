import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
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
    private byte[] msgBytes = new byte[244];
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

    public void encryptMessage(int port){
        try {

            cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
            cipher.update("localhost\n1250\nConnecting".getBytes());
            System.out.println(pair.getPublic());
        } catch (InvalidKeyException   e) {
            e.printStackTrace();
        }
    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);
        //System.out.println("Message recieved.");

        msgBytes = packet.getData();
        msgBytes = Arrays.copyOfRange(msgBytes, 0, packet.getLength());
        System.out.println(packet.getLength());
        //unless specified otherwise, the response will be sent back;
        address = packet.getAddress();
        port = packet.getPort();
    }

    public void handleData() throws UnknownHostException, NoSuchAlgorithmException {

        //System.out.println("encrypted message: " + encryptedMsg + "\nEnd encrypted");
        if (msgBytes[0] == 1){

            //Exchange keys
            //Source: https://www.tutorialspoint.com/java_cryptography/java_cryptography_quick_guide.htm
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            pair = keyPairGen.generateKeyPair();
            PublicKey publicKey = pair.getPublic();

            String modulus = String.valueOf(((RSAPublicKey) publicKey).getModulus());
            byte[] modBytes = modulus.getBytes();
            String exponent = String.valueOf(((RSAPublicKey) publicKey).getPublicExponent());
            byte[] expBytes = exponent.getBytes();

            msg = modulus + "\n" + exponent;
            msgBytes = msg.getBytes();

            //System.out.println("client:\nModulus: " +  String.valueOf(modulus) + "\nExponent: " +  String.valueOf(exponent));



        } else {
            //Forward message
            //address = InetAddress.getByName(splitMessage[1]);
            System.out.println("Starting decrypting");
            //msgBytes = Arrays.copyOfRange(msgBytes, 1, msgBytes.length);
            System.out.println(msgBytes.length);
            decryptData(msgBytes);
            System.out.println("The message is: " + new String(msgBytes));

            /*
            System.out.println("Done encrypting");
            decryptData(String.join("\n", Arrays.copyOfRange(splitMessage, 1, splitMessage.length)).getBytes());

            address = InetAddress.getByName("localhost");
            port = Integer.parseInt(splitMessage[2]);
            msg = String.join("\n", Arrays.copyOfRange(splitMessage, 3, splitMessage.length));
            */
        }

    }

    public void decryptData(byte[] encryptedBytes){
        try {

            cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
            msgBytes = cipher.doFinal(msgBytes);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

    }



    public void sendBytes() throws IOException {
        buf = msgBytes;
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    public void sendString() throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }




    public void run(){
        try {

            while (true){
                recieveMessage();
                handleData();
                sendBytes();


            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



        socket.close();
    }
}
