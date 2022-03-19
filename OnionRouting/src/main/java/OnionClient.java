import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class OnionClient {
    private DatagramSocket socket;
    private InetAddress address;
    private Scanner in;

    private byte[] buf;
    private byte[] buf2 = new byte[256];

    public OnionClient() throws SocketException, UnknownHostException {
        in = new Scanner(System.in);
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void runCalculator() throws IOException {
        String msg = "Connecting";
        boolean running = true;
        DatagramPacket packet;
        while (running){
            buf = msg.getBytes();
            packet = new DatagramPacket(buf, buf.length, address, 1250);
            socket.send(packet);

            packet = new DatagramPacket(buf2, buf2.length);
            socket.receive(packet);

            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println(received);
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
