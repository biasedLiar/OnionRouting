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
    private MessageMode mode;
    private byte[] msgBytes = new byte[244];
    private KeyPair pair;
    Cipher cipher;

    public OnionNode() throws SocketException {
        socket = new DatagramSocket(1251);
        createKeys();
    }

    public void createKeys(){
        //Source: https://www.tutorialspoint.com/java_cryptography/java_cryptography_quick_guide.htm

        KeyPairGenerator keyPairGen = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(2048);
        pair = keyPairGen.generateKeyPair();
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

        msgBytes = packet.getData();
        mode = MessageMode.valueOf(msgBytes[0]);

        msgBytes = Arrays.copyOfRange(msgBytes, 1, packet.getLength());
        //System.out.println(packet.getLength());
        //unless specified otherwise, the response will be sent back;
        address = packet.getAddress();
        port = packet.getPort();

        System.out.println("Node recieved message");
    }

    public void handleData() throws UnknownHostException, NoSuchAlgorithmException {

        //System.out.println("encrypted message: " + encryptedMsg + "\nEnd encrypted");
        if (mode == MessageMode.KEY_EXCHANGE){
            //Exchange keys
            PublicKey publicKey = pair.getPublic();
            String modulus = String.valueOf(((RSAPublicKey) publicKey).getModulus());
            String exponent = String.valueOf(((RSAPublicKey) publicKey).getPublicExponent());

            msg = modulus + "\n" + exponent;
            msgBytes = msg.getBytes();

            //System.out.println("client:\nModulus: " +  String.valueOf(modulus) + "\nExponent: " +  String.valueOf(exponent));



        } else if (mode == MessageMode.FORWARD_ON_NETWORK){
            //Forward message
            //address = InetAddress.getByName(splitMessage[1]);
            //System.out.println("Starting decrypting");
            //msgBytes = Arrays.copyOfRange(msgBytes, 1, msgBytes.length);
            //System.out.println(msgBytes.length);
            decryptData(msgBytes);
            System.out.println("The message is: \n" + new String(msgBytes) + "\nEnd off message.");

            /*
            System.out.println("Done encrypting");
            decryptData(String.join("\n", Arrays.copyOfRange(splitMessage, 1, splitMessage.length)).getBytes());

            address = InetAddress.getByName("localhost");
            port = Integer.parseInt(splitMessage[2]);
            msg = String.join("\n", Arrays.copyOfRange(splitMessage, 3, splitMessage.length));
            */
        }

    }

    public void calculatePort(byte b1, byte b2){
        int n1 = b1;
        int n2 = b2;
        if (n1 < 0){
            n1 += 256;
        }
        if (n2 < 0){
            n2 += 256;
        }
        System.out.println("B1: " + n1);
        System.out.println("B2: " + n2);

        port = n1*256 + n2;
        System.out.println("Port = " + port);
    }

    public void decryptData(byte[] encryptedBytes){
        try {

            cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
            msgBytes = cipher.doFinal(msgBytes);
            if (mode == MessageMode.FORWARD_ON_NETWORK){
                calculatePort(msgBytes[0], msgBytes[1]);
                msgBytes = Arrays.copyOfRange(msgBytes, 2, msgBytes.length);
            } else if(mode == MessageMode.FORWARD_OFF_NETWORK){
                System.out.println("TODO: implement this function");
            }
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
        System.out.println("SEnding message to: " + port);
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
