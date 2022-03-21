import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class OnionServer extends Thread{
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[2048];
    private byte[] buf2 = new byte[2048];
    private InetAddress address;
    private String msg;

    public OnionServer() throws SocketException {
        socket = new DatagramSocket(1250);
    }

    public String wrapMessage(String msg){
        String newMessage = "F\nlocalhost\n8081\n" + msg;
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


    public String sendMessageGetResponse(String message) throws IOException {
        msg = message;
        sendMessage();
        recieveMessage();
        return msg;

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
        try {
            address = InetAddress.getByName("localhost");
            running = true;



            recieveMessage();

            while (running){
                String num1 = sendMessageGetResponse("Choose number 1:");
                String num2 = sendMessageGetResponse("Choose number 2:");
                String operator = sendMessageGetResponse("Choose operator: (+/-):");

                String ans = getResult(num1, num2, operator);
                String playAgain = sendMessageGetResponse(ans + "\nPlay again? (y/n)");

                if (!playAgain.toLowerCase().startsWith("y")) {
                    running = false;
                    sendMessageGetResponse("Press enter to exit.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        socket.close();
    }
}
