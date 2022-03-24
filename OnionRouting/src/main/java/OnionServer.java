import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class OnionServer extends OnionEndPoint{
    private boolean running;

    public OnionServer() throws SocketException {
        super(1250);
    }

    public void run(){
        try {
            keyEchange(1251, InetAddress.getByName("localhost"));
            System.out.println("Client finished sharing keys");
            running = true;

            while (running){
                recieveMessageUpdatePort();
                //System.out.println("Server her, message recieved: \n" + msg);
                wrapMessage(MessageMode.FORWARD_ON_NETWORK, 1);
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
