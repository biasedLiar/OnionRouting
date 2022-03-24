import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.sql.SQLOutput;
import java.util.*;

public class OnionClient  extends OnionEndPoint{
    private Scanner in;

    HashMap<Integer, PublicKey> keys;

    public OnionClient() throws SocketException, UnknownHostException {
        super(8081);
        in = new Scanner(System.in);
    }




    public void run() {
        try {
            System.out.println("Starting sharing keys");
            keyEchange(1251, InetAddress.getByName("localhost"));
            System.out.println("Finished sharing keys");

            msg = "Connecting";
            boolean running = true;
            while (running){
                port = 1250;
                wrapMessage(MessageMode.FORWARD_ON_NETWORK, 1);
                sendMessage();
                recieveMessage();
                System.out.println(msg);
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
