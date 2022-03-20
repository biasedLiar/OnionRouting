import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class OnionServer extends Thread{
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    private byte[] buf2 = new byte[256];

    public OnionServer() throws SocketException {
        socket = new DatagramSocket(1250);
    }


    public String sendMessageGetResponse(String msg, InetAddress address, int port, DatagramPacket packet) throws IOException {
        buf = ("F\nlocalhost\n8081\n" + msg).getBytes();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);

        packet = new DatagramPacket(buf2, buf2.length);
        socket.receive(packet);

        String received = new String(packet.getData(), 0, packet.getLength());
        return received;
    }

    public String getResult(String num1, String num2, String operator){


        Double n1 = null;
        Double n2 = null;
        try {
            n1 = Double.parseDouble(num1);
            n2 = Double.parseDouble(num2);
        } catch (NumberFormatException e) {
            return "Please enter a valid number";
        }

        if (operator.equals("+")){
            return Double.toString(n1 + n2);
        }
        if (operator.equals("-")){
            return Double.toString(n1 - n2);
        }

        return "Please enter a valid operator";

    }

    public void run(){
        running = true;
        try {
            running = true;

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            while (running){
                String num1 = sendMessageGetResponse("Choose number 1:", address, port, packet);
                String num2 = sendMessageGetResponse("Choose number 2:", address, port, packet);
                String operator = sendMessageGetResponse("Choose operator: (+/-):", address, port, packet);

                String ans = getResult(num1, num2, operator);
                String playAgain = sendMessageGetResponse(ans + "\nPlay again? (y/n)", address, port, packet);

                if (!playAgain.toLowerCase().startsWith("y")) {
                    running = false;
                    sendMessageGetResponse("Press enter to exit.", address, port, packet);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        socket.close();
    }
}
