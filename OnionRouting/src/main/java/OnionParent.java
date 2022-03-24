import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public abstract class OnionParent extends Thread{
    protected DatagramSocket socket;
    protected InetAddress address;
    protected String msg;
    protected byte[] msgBytes;
    protected Cipher cipher;
    protected byte[] buf;
    protected byte[] buf2;
    protected int port;

    public OnionParent(int port){
        buf = new byte[2048];
        buf2 = new byte[2048];
        msgBytes = new byte[244];
        this.port = port;
        try {
            socket = new DatagramSocket(port);
            address = InetAddress.getByName("localhost");
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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
        //System.out.println(packet.getLength());
        //unless specified otherwise, the response will be sent back;
        address = packet.getAddress();
        port = packet.getPort();

        System.out.println("Node recieved message");
    }

    public void sendMessage() throws IOException {
        buf = msgBytes;
        //System.out.println("MEssage is " + msgBytes.length + " at clientside.");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1251);
        socket.send(packet);
        System.out.println("MEssage sent from client");
    }



}
