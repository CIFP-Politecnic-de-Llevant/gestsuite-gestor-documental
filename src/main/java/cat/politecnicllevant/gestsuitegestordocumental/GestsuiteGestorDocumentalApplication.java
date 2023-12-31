package cat.politecnicllevant.gestsuitegestordocumental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GestsuiteGestorDocumentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestsuiteGestorDocumentalApplication.class, args);
	}

}
