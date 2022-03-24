import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public abstract class OnionParent extends Thread{
    protected DatagramSocket socket;
    protected InetAddress address;
    protected String msg;
    protected byte[] msgBytes;
    protected Cipher rsaCipher;
    protected Cipher aesCipher;
    protected byte[] buf;
    protected byte[] buf2;
    protected int port;
    protected int myPort;

    public OnionParent(int port){
        buf = new byte[2048];
        buf2 = new byte[2048];
        msgBytes = new byte[244];
        this.port = port;
        myPort = port;
        try {
            socket = new DatagramSocket(port);
            address = InetAddress.getByName("localhost");
            rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//https://howtodoinjava.com/java/java-security/java-aes-encryption-example/
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);
        msgBytes = packet.getData();

        msgBytes = Arrays.copyOfRange(msgBytes, 0, packet.getLength());
        msg = new String(msgBytes);
        //unless specified otherwise, the response will be sent back;
        address = packet.getAddress();
        port = packet.getPort();

        //System.out.println("Node recieved message");
    }

    public void sendMessage() throws IOException {
        buf = msgBytes;
        //System.out.println("MEssage is " + msgBytes.length + " at clientside.");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);

        System.out.println("Message sent from " + myPort + " to " + port);
    }


    public void calculatePort(){
        int n1 = msgBytes[0];
        int n2 = msgBytes[1];
        if (n1 < 0){
            n1 += 256;
        }
        if (n2 < 0){
            n2 += 256;
        }

        port = n1*256 + n2;
        msgBytes = Arrays.copyOfRange(msgBytes, 2, msgBytes.length);
    }



}
