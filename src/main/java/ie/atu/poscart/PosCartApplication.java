package ie.atu.poscart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PosCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(PosCartApplication.class, args);
    }
}
