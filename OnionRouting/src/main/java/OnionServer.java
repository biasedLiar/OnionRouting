import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class OnionServer extends OnionEndPoint{
    private boolean running;

    public OnionServer() throws SocketException {
        super(1250);
    }

    /*
    public void wrapMessage(){
        String newMessage = "F\nlocalhost\n8081\n" + msg;
        msgBytes = newMessage.getBytes();
    }

     */

    public String sendMessageGetResponse(String message) throws IOException {
        msg = message;
        sendMessage();
        wrapMessage(MessageMode.FORWARD_ON_NETWORK, 1);
        recieveMessage();
        return msg;

    }

    public void run(){
        try {

            keyEchange(1251, InetAddress.getByName("localhost"));
            System.out.println("Finished sharing keys");
            address = InetAddress.getByName("localhost");
            running = true;


            System.out.println("Server reached");

            while (running){
                recieveMessage();
                port = 8081;
                msgBytes = msg.getBytes();
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
