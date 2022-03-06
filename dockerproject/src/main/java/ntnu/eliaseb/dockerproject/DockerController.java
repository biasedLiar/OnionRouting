package ntnu.eliaseb.dockerproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@CrossOrigin
@RestController
public class DockerController {

    @Autowired
    private DockerService service;

    Logger logger = LoggerFactory.getLogger(DockerController.class);


    @GetMapping("/Hello")
    public String hello(){
        logger.debug("Hello");
        System.out.println(" pressed, ");
        return String.format("Hello !");
    }


    @PostMapping("/Docker")
    public String docker(@RequestParam("code") String code){
        System.out.println("code: \n" + code);
        String response = service.runCode(code);
        System.out.println("\nresponse: \n" + response);
        return response;
    }

}
