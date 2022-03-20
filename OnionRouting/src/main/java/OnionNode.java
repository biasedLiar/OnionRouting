import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class OnionNode extends Thread{
    private DatagramSocket socket;
    private byte[] buf = new byte[256];
    private byte[] buf2 = new byte[256];
    private String encryptedMsg;
    private String msg;
    private InetAddress address;
    private int port;
    private String mode;

    public OnionNode() throws SocketException {
        socket = new DatagramSocket(1251);
    }

    public void recieveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);
        encryptedMsg = new String(packet.getData(), 0, packet.getLength());
    }

    public void decryptMessage() throws UnknownHostException {
        String[] splitMessage = encryptedMsg.split("\n"); //https://attacomsian.com/blog/java-split-string-new-line
        //System.out.println(splitMessage);
        for (String s :
                splitMessage) {
            System.out.println("From inside node:" + s);
        }
        if ((mode = splitMessage[0]).equals("E")){
            //Exchange keys
        } else {
            //Forward message
            //address = InetAddress.getByName(splitMessage[1]);
            address = InetAddress.getByName("localhost");
            port = Integer.parseInt(splitMessage[2]);
            msg = String.join("", Arrays.copyOfRange(splitMessage, 3, splitMessage.length));
        }
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
                if (mode.equals("F")){
                    forwardMessage();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        socket.close();
    }
}
