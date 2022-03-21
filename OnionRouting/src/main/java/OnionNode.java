import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
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

    public OnionNode() throws SocketException {
        socket = new DatagramSocket(1251);
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

    public void decryptMessage() throws UnknownHostException, NoSuchAlgorithmException {

        //System.out.println("encrypted message: " + encryptedMsg + "\nEnd encrypted");
        String[] splitMessage = encryptedMsg.split("\n"); //https://attacomsian.com/blog/java-split-string-new-line
        for (String s :
                splitMessage) {
            //System.out.println("From inside node:" + s);
        }
        if ((mode = splitMessage[0]).equals("E")){
            //Exchange keys
            //System.out.println("Length of exchange array, should be 3: " + splitMessage.length);
            BigInteger modulus = new BigInteger(splitMessage[1]);
            BigInteger exponent = new BigInteger(splitMessage[2]);

            //System.out.println("Server:\nModulus: " +  String.valueOf(modulus) + "\nExponent: " +  String.valueOf(exponent));
            //source: https://stackoverflow.com/questions/2023549/creating-rsa-keys-from-known-parameters-in-java
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            try {
                PublicKey pub = factory.generatePublic(spec);
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, pub);

                byte[] message = "Success!".getBytes();
                cipher.update(message);
                byte[] encrypted = cipher.doFinal();
                System.out.println("Encrypted response is " + encrypted.length);
                newBytes = encrypted;
            } catch (InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }


        } else {
            //Forward message
            //address = InetAddress.getByName(splitMessage[1]);
            address = InetAddress.getByName("localhost");
            port = Integer.parseInt(splitMessage[2]);
            msg = String.join("\n", Arrays.copyOfRange(splitMessage, 3, splitMessage.length));
        }
    }



    public void forwardMessage() throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    public void returnMessage() throws IOException {
        buf = newBytes;
        System.out.println("REsponse form server length: " + buf.length);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }


    public void run(){
        try {

            while (true){
                recieveMessage();
                decryptMessage();
                if (mode.equals("F")){
                    forwardMessage();
                } else {
                    returnMessage();
                }

            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



        socket.close();
    }
}
