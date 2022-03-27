import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
import java.security.*;
import java.util.*;

public class OnionClient  extends OnionEndPoint{
    private Scanner in;
    private int serverPort;

    public OnionClient(int port, int serverPort, ArrayList<String> nodePorts) throws SocketException, UnknownHostException {
        super(port, nodePorts);
        this.serverPort = serverPort;
        in = new Scanner(System.in);
    }




    public void run() {
        try {
            keyEchange();
            System.out.println("Client finished sharing keys");

            msg = "Connecting";
            boolean running = true;
            targetSocketString = "1250 127.0.0.1";
            while (running){
                System.out.println(msg.getBytes().length + " Client length");
                wrapMessage(MessageMode.FORWARD_ON_NETWORK, 3);
                sendMessage();

                recieveMessageUpdatePort();
                System.out.println(new String(msgBytes));

                System.out.println("\nSelect option\n1: Echoserver\n2: Webserver\n3: Terminate");
                msg = in.nextLine();
                if (msg.equals("1")){
                    //
                    System.out.println("Echoserver chosen.\nEnter input then press Enter.");
                    msg = in.nextLine();
                    targetSocketString = "1250 127.0.0.1";
                } else if (msg.equals("2")){
                    System.out.println("Not implemented, using echo server.\nEnter input then press Enter.");
                    msg = in.nextLine();
                    targetSocketString = "1250 127.0.0.1";
                } else {
                    System.out.println("Exiting...");
                    running = false;
                }
            }
            System.out.println("Exiting");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        socket.close();
    }
}
