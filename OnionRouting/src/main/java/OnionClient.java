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
import java.util.Scanner;

public class OnionClient {
    private DatagramSocket socket;
    private InetAddress address;
    private Scanner in;
    private String msg;

    private byte[] encryptedBytes = new byte[300];

    private byte[] buf;
    private byte[] buf2 = new byte[2048];

    public OnionClient() throws SocketException, UnknownHostException {
        in = new Scanner(System.in);
        socket = new DatagramSocket(8081);
        address = InetAddress.getByName("localhost");
    }

    public void wrapMessage(){
        msg = "F\nlocalhost\n1250\n" + msg;
    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);

        msg = new String(packet.getData(), 0, packet.getLength());
    }

    public void recieveEncryption() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);

        encryptedBytes = packet.getData();
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

    public void doEncryptionTest() throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        //Creating KeyPair generator object
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        //Initializing the key pair generator
        keyPairGen.initialize(2048);

        //Generate the pair of keys
        KeyPair pair = keyPairGen.generateKeyPair();

        //Getting the public key from the key pair
        PublicKey publicKey = pair.getPublic();

        //Creating a Cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        //Initializing a Cipher object
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        //Add data to the cipher
        byte[] input = "Welcome to Tutorialspoint".getBytes();
        cipher.update(input);

        //encrypting the data
        byte[] cipherText = cipher.doFinal();
        System.out.println( new String(cipherText, "UTF8"));

        //Initializing the same cipher for decryption
        cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());

        //Decrypting the text
        byte[] decipheredText = cipher.doFinal(cipherText);
        System.out.println(new String(decipheredText));
    }

    public void keyEchange(int port, InetAddress address1) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Source: https://www.tutorialspoint.com/java_cryptography/java_cryptography_quick_guide.htm
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair pair = keyPairGen.generateKeyPair();
        PublicKey publicKey = pair.getPublic();
        String modulus = String.valueOf(((RSAPublicKey) publicKey).getModulus());
        String exponent = String.valueOf(((RSAPublicKey) publicKey).getPublicExponent());
        //System.out.println("Client\nModulus: " + modulus + "\nExponent: " + exponent);
        msg = "E\n" + modulus + "\n" + exponent;
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");


        sendMessage(address1, port);
        recieveEncryption();

        byte[] input = encryptedBytes;
        System.out.println("INput length must not be over 245 but is: " + input.length);


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

         */


        /*
        byte[] input = "Success".getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        cipher.update(input);
        input = cipher.doFinal();
        msg = cipher.doFinal().toString();
        */

        cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
        System.out.println("Starting response: " + new String(cipher.doFinal(input)) + " is the response.");

    }



    public void runCalculator() throws IOException {
        try {
            //doEncryptionTest();
            keyEchange(1251, InetAddress.getByName("localhost"));
            System.out.println("Finished sharing keys");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        msg = "Connecting";
        boolean running = true;
        while (running){
            wrapMessage();
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
