package com.kopibru.bookmanagement;

import com.kopibru.bookmanagement.config.SchemaBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookManagementApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BookManagementApplication.class);
        application.addListeners(new SchemaBootstrap());
        application.run(args);
    }
}
