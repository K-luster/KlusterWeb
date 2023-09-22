package kluster.klusterweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KlusterWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(KlusterWebApplication.class, args);
	}
}
