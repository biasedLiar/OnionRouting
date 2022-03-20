import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class OnionClient {
    private DatagramSocket socket;
    private InetAddress address;
    private Scanner in;
    private String msg;

    private byte[] buf;
    private byte[] buf2 = new byte[256];

    public OnionClient() throws SocketException, UnknownHostException {
        in = new Scanner(System.in);
        socket = new DatagramSocket(8081);
        address = InetAddress.getByName("localhost");
    }

    public String wrapMessage(String msg){
        String newMessage = "F\nlocalhost\n1250\n" + msg;
        return newMessage;
    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);

        msg = new String(packet.getData(), 0, packet.getLength());
    }

    public void sendMessage() throws IOException {
        buf = wrapMessage(msg).getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1251);
        socket.send(packet);
        //System.out.println("MEssage sent from client");
    }



    public void runCalculator() throws IOException {
        msg = "Connecting";
        boolean running = true;
        while (running){
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
