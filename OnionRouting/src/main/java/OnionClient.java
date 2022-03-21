import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class OnionClient {
    private DatagramSocket socket;
    private InetAddress address;
    private Scanner in;
    private String msg;
    HashMap<Integer, PublicKey> keys;
    Cipher cipher;

    private byte[] encryptedBytes = new byte[300];

    private byte[] buf;
    private byte[] buf2 = new byte[2048];

    public OnionClient() throws SocketException, UnknownHostException {
        in = new Scanner(System.in);
        socket = new DatagramSocket(8081);
        keys = new HashMap<>();
        address = InetAddress.getByName("localhost");
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
            cipher.init(Cipher.ENCRYPT_MODE, keys.get(port));
            cipher.update(msg.getBytes());
            msg = new String(cipher.doFinal());
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public void wrapMessage(int port){
        msg = "localhost\n1250\n" + msg;
        encryptMessage(port);
        msg = "F\n" + msg;

    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);

        msg = new String(packet.getData(), 0, packet.getLength());
    }

    public void recieveEncryption() throws IOException {
        msg = "E";
        sendMessage();
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);

        socket.receive(packet);
        msg = new String(packet.getData(), 0, packet.getLength());

        /*
        encryptedBytes = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        System.out.println("arraylength: " + encryptedBytes.length);
        System.out.println("Data recieved: " + packet.getLength());
         */
    }

    public void sendMessage() throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1251);
        socket.send(packet);
        //System.out.println("MEssage sent from client");
    }

    public void sendMessage(InetAddress address, int port) throws IOException {
        buf = msg.getBytes();
        System.out.println(msg.length() + " length of message");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        System.out.println("MEssage sent from client");
    }

    public void keyEchange(int port, InetAddress address1) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        recieveEncryption();

        String[] splitMessage = msg.split("\n"); //https://attacomsian.com/blog/java-split-string-new-line

        //System.out.println("Length of exchange array, should be 3: " + splitMessage.length);
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
        /*

        //source: https://stackoverflow.com/questions/2023549/creating-rsa-keys-from-known-parameters-in-java
        RSAPublicKeySpec spec = new RSAPublicKeySpec(((RSAPublicKey) publicKey).getModulus(), ((RSAPublicKey) publicKey).getPublicExponent());
        KeyFactory factory = KeyFactory.getInstance("RSA");
        byte[] input = new byte[5];
        try {
            PublicKey pub = factory.generatePublic(spec);
            cipher.init(Cipher.ENCRYPT_MODE, pub);

            byte[] message = "Success!".getBytes();
            cipher.update(message);
            input = cipher.doFinal();
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }










        byte[] message = "Success!".getBytes();
            cipher.update(message);
            byte[] encrypted = cipher.doFinal();
            System.out.println("Encrypted response is " + encrypted.length);
            newBytes = encrypted;
            System.out.println("After going back and forth: " + new String(newBytes, 0, newBytes.length).getBytes().length);


         */


        /*
        byte[] input = "Success".getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        cipher.update(input);
        input = cipher.doFinal();
        msg = cipher.doFinal().toString();




        cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
        System.out.println("Starting response: " + new String(cipher.doFinal(encryptedBytes)) + " is the response.");

        */


    }



    public void runCalculator() throws IOException {
        try {
            keyEchange(1251, InetAddress.getByName("localhost"));
            System.out.println("Finished sharing keys");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        msg = "Connecting";
        boolean running = true;
        while (running){
            wrapMessage(1251);
            sendMessage();
            recieveMessage();
            System.out.println(msg);
            msg = in.nextLine();
            if (msg.equals("")){
                running = false;
            }
        }
        System.out.println("Exiting");
    }

    public void close() {
        socket.close();
    }
}
