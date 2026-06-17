package pe.gob.onp.thaqhiri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BackendThaqhiriApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendThaqhiriApplication.class, args);
	}

}
