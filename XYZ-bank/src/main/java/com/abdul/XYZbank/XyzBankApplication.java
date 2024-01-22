package com.abdul.XYZbank;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "XYZ Bank App",
                description = "Backend application for XYZ bank",
                version = "v1.0",
                contact = @Contact(
                        name = "Abdul Barambu",
                        email = "abdulkadirbarambutest@gmail.com",
                        url = "https://github.com/abdul-barambu"
                ),
                license = @License(
                        name = "Orellions",
                        url = "https://github.com/abdul-barambu"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "XYZ bank app documentation",
                url = "https://github.com/abdul-barambu"
        )
)
public class XyzBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyzBankApplication.class, args);
    }

}
