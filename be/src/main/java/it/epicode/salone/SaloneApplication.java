package it.epicode.salone;

import it.epicode.salone.config.DatabaseUrl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// @EnableAsync: serve ad AvvisoListener per mandare le mail in un thread separato
@EnableAsync
@SpringBootApplication
public class SaloneApplication {

	public static void main(String[] args) {
		// Su Render le credenziali arrivano in DATABASE_URL, formato non JDBC:
		// la traduzione va fatta prima che parta il contesto Spring.
		DatabaseUrl.applicaSePresente();

		SpringApplication.run(SaloneApplication.class, args);
	}
}
