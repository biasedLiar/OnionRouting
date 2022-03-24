import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class OnionServer extends OnionEndPoint{
    private boolean running;

    public OnionServer(int port, ArrayList<Integer> nodePorts) throws SocketException {
        super(port, nodePorts);
    }

    public void run(){
        try {
            keyEchange(1251, InetAddress.getByName("localhost"));
            System.out.println("Server finished sharing keys");
            running = true;

            while (running){
                recieveMessageUpdatePort();
                //System.out.println("Server her, message recieved: \n" + msg);
                System.out.println("Server recieved message, sending answer.");
                wrapMessage(MessageMode.FORWARD_ON_NETWORK, 3);
                sendMessage();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        socket.close();
    }
}
