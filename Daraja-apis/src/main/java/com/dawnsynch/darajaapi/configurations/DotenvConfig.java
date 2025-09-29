package com.dawnsynch.darajaapi.configurations;

//import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class DotenvConfig {
//
//    @PostConstruct
//    public void loadEnv() {
//        Dotenv dotenv = Dotenv.configure()
//                .directory(System.getProperty("user.dir")) // project root
//                .ignoreIfMissing()
//                .load();
//
//        // Put selected values into system properties (or all entries)
//        // Option 1: set selected keys
//        if (dotenv.get("DB_USERNAME") != null) {
//            System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
//        }
//        if (dotenv.get("DB_PASSWORD") != null) {
//            System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
//        }
//        // set MPESA keys too (or loop all entries)
//        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
//    }
//}
