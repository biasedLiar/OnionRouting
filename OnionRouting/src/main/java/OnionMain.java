import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class OnionMain {
    public static void main(String[] args) throws IOException {
        System.out.println("HEllo world");
        Scanner in = new Scanner(System.in);
        try {
            new OnionServer().start();
            OnionClient client = new OnionClient();
            client.runCalculator();
            client.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
