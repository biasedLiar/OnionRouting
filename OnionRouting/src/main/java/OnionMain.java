import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class OnionMain {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        try {
            ArrayList<Integer> nodePorts = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                new OnionNode(1251 + i).start();
                nodePorts.add(1251+i);
            }
            new OnionServer(1250, nodePorts).start();
            new OnionClient(8081, 1250, nodePorts).start();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
