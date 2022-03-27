import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class OnionWebServer extends OnionEndPoint{
    private boolean running;

    public OnionWebServer(int port, ArrayList<String> nodePorts) throws SocketException {
        super(port, nodePorts);
    }

    public void run(){
        try {
            keyEchange();
            System.out.println("Server finished sharing keys");
            running = true;

            while (running){
                recieveMessageUpdatePort();
                //System.out.println("Server her, message recieved: \n" + msg);
                System.out.println("Webserver recieved message, sending answer.");
                //https://www.baeldung.com/java-http-request
                URL url = new URL(msg);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                con.disconnect();

                msg = content.toString();
                msgBytes = msg.getBytes();
                addModeToMessage(MessageMode.FORWARD_ON_NETWORK);
                wrapMessage();
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
