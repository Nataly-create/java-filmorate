package ru.yandex.practicum.filmorate;

import com.sun.net.httpserver.HttpServer;
import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;

@SpringBootApplication
public class FilmorateApplication {
	private  static final int PORT = 8080;
	static HttpServer httpServer;
	private static final Logger log = LoggerFactory.getLogger(FilmorateApplication.class);

	public static void main(String[] args) {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
				.setLevel(Level.INFO);
		log.info("Start at {}", LocalDate.now());
		SpringApplication.run(FilmorateApplication.class, args);
	}
}
