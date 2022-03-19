import java.io.IOException;
import java.net.*;

public class OnionNode extends Thread{
    private DatagramSocket socket;
    private byte[] buf = new byte[256];
    private byte[] buf2 = new byte[256];
    private String encryptedMsg;
    private String msg;
    private String metadata;
    private InetAddress address;
    private int port;

    public OnionNode() throws SocketException {
        socket = new DatagramSocket(1251);
    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);
        encryptedMsg = new String(packet.getData(), 0, packet.getLength());
    }

    public String decryptMessage() throws UnknownHostException {
        String[] splitMessage = encryptedMsg.split("\\r?\\n"); //https://attacomsian.com/blog/java-split-string-new-line
        address = InetAddress.getByName(splitMessage[0]);
        port = Integer.parseInt(splitMessage[1]);
        return "";
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
                decryptMessage();
                forwardMessage();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        socket.close();
    }
}
