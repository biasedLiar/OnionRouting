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
import java.util.*;

public class OnionClient {
    private DatagramSocket socket;
    private InetAddress address;
    private Scanner in;
    private String msg;
    private byte[] msgBytes;
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

    public ArrayList<Integer> getRandomPorts(int numPorts){
         ArrayList<Integer> ports = new ArrayList<>(keys.keySet());
         Collections.shuffle(ports);
         if (ports.size() < numPorts){
             return ports;
         } else {
             return new ArrayList<>(ports.subList(0, numPorts));
         }

    }

    public void addPortToMessage(int port){
        byte[] tempBytes = new byte[msgBytes.length + 2];
        tempBytes[0] = (byte) Math.floor(port/256);
        tempBytes[1] = (byte) (port % 256);
        System.out.println("ENcrypting. 0: " + tempBytes[0] + ", 1: " + tempBytes[1] + ", port: " + port);

        for (int i = 0; i < msgBytes.length; i++) {
            tempBytes[i + 2] = msgBytes[i];
        }
        msgBytes = tempBytes;
    }

    public void encryptMessage(int endPort, int nodePort, MessageMode mode){
        try {
            addPortToMessage(endPort);
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

    public void wrapMessage(int endPort, MessageMode mode, int numOnionPorts){
        msgBytes = msg.getBytes();
        ArrayList<Integer> onionPorts = getRandomPorts(numOnionPorts);
        for (int i = 0; i < onionPorts.size(); i++) {
            encryptMessage(endPort, onionPorts.get(i), mode);
            endPort = onionPorts.get(i);
        }
        //Copy array into larger array

        //System.out.println("MSGbytes length: " + msgBytes.length);

    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);

        msgBytes = packet.getData();
        msgBytes = Arrays.copyOfRange(msgBytes, 0, packet.getLength());
        msg = new  String(msgBytes);
    }

    public void recieveEncryption() throws IOException {
        msgBytes = new byte[1];
        msgBytes[0] = MessageMode.KEY_EXCHANGE.getValue();
        sendMessage();
        recieveMessage();



        /*
        encryptedBytes = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        System.out.println("arraylength: " + encryptedBytes.length);
        System.out.println("Data recieved: " + packet.getLength());
         */
    }

    public void sendMessage() throws IOException {
        buf = msgBytes;
        //System.out.println("MEssage is " + msgBytes.length + " at clientside.");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1251);
        socket.send(packet);
        System.out.println("MEssage sent from client");
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
            wrapMessage(1250, MessageMode.FORWARD_ON_NETWORK, 1);
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
