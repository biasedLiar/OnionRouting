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

    public OnionClient(int port, int serverPort, ArrayList<Integer> nodePorts) throws SocketException, UnknownHostException {
        super(port, nodePorts);
        this.serverPort = serverPort;
        in = new Scanner(System.in);
    }




    public void run() {
        try {
            keyEchange(1251, InetAddress.getByName("localhost"));
            System.out.println("Client finished sharing keys");

            msg = "Connecting";
            boolean running = true;
            port = 1250;
            while (running){
                wrapMessage(MessageMode.FORWARD_ON_NETWORK, 3);
                sendMessage();
                recieveMessageUpdatePort();
                System.out.println(new String(msgBytes));
                msg = in.nextLine();
                if (msg.equals("")){
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
