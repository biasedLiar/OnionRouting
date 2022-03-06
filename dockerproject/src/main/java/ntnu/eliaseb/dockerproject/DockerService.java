package ntnu.eliaseb.dockerproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class DockerService {


    public DockerService() {}

    public String runCode(String code){
        String response = "";
        try {


            File myObj = new File("dockerproject\\hello-docker\\hello.py");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
            }


            FileWriter myWriter = new FileWriter(myObj);
            myWriter.write(code);
            myWriter.close();


            ProcessBuilder builder = new ProcessBuilder();
            builder.command("docker", "build", "-t", "hello-docker", "dockerproject\\hello-docker");
            builder.redirectErrorStream(true);
            Process proc = builder.start();
            System.out.println("Built");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = "";
            while((line = reader.readLine()) != null) {
                //System.out.print(line + "\n");
                //response += line + "\n";
            }

            proc.waitFor();

            builder = new ProcessBuilder();
            builder.command("docker", "run", "--rm", "hello-docker");
            builder.redirectErrorStream(true);
            proc = builder.start();
            System.out.println("Built");
            reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            while((line = reader.readLine()) != null) {
                //System.out.print(line + "\n");
                response += line + "\n";
            }

            proc.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }


}
