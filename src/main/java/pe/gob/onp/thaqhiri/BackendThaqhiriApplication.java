package pe.gob.onp.thaqhiri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import pe.gob.onp.thaqhiri.auth.SaaProperties;

@SpringBootApplication
@EnableConfigurationProperties(SaaProperties.class)
@EnableAsync
public class BackendThaqhiriApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendThaqhiriApplication.class, args);
	}

}
