package com.dawnsynch.darajaapitutorial;

import com.dawnsynch.darajaapitutorial.dtos.AcknowledgeResponse;
import com.dawnsynch.darajaapitutorial.dtos.TransactionResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DarajaApiTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.run(DarajaApiTutorialApplication.class, args);
    }

    @Bean
    public AcknowledgeResponse getAcknowledgeResponse(){
        AcknowledgeResponse response = new AcknowledgeResponse();
        response.setMessage("Success");
        return response;
    }
}
