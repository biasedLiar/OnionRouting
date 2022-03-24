import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class OnionMain {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        try {
            new OnionNode().start();
            new OnionServer().start();
            new OnionClient().start();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
