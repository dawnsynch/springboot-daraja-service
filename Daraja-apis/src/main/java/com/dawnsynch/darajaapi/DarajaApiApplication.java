package com.dawnsynch.darajaapi;

import com.dawnsynch.darajaapi.configurations.DarajaProperties;
import com.dawnsynch.darajaapi.dtos.AcknowledgeResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(DarajaProperties.class)
public class DarajaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DarajaApiApplication.class, args);
    }

    @Bean
    public AcknowledgeResponse getAcknowledgeResponse(){
        AcknowledgeResponse response = new AcknowledgeResponse();
        response.setMessage("Success");
        return response;
    }
}
