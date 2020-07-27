package com.brummer.learningspringboot.images.domain;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class InitDatabase {
	
	@Bean
	CommandLineRunner init(ImageRepository imageRepository) {
		return args -> {	
			
			Flux.just(
				new Image("1", "learning-spring-boot-cover.jpg"),
				new Image("2", "learning-spring-boot-2nd-edition-cover.jpg"),
				new Image("3", "bazinga.png"))
				.flatMap(imageRepository::save)
				.subscribe(System.out::println);
		};
	}

}
