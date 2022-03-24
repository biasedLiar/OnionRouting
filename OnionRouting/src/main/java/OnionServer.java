import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class OnionServer extends OnionParent{
    private boolean running;

    public OnionServer() throws SocketException {
        super(1250);
    }

    public void wrapMessage(){
        String newMessage = "F\nlocalhost\n8081\n" + msg;
        msgBytes = newMessage.getBytes();
    }

    public String sendMessageGetResponse(String message) throws IOException {
        msg = message;
        sendMessage();
        wrapMessage();
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
            port = 1250;
            running = true;



            recieveMessage();
            System.out.println("Server reached");

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
