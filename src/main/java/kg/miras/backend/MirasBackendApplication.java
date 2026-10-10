package kg.miras.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic(systemName = "Miras Backend")
@SpringBootApplication
public class MirasBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MirasBackendApplication.class, args);
    }
}

