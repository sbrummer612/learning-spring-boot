package com.brummer.learningspringboot.images.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class LiveImageRepositoryTests {

	@Autowired
	ImageRepository repository;

	Flux<Image> setup;
	Flux<Image> find;
	Flux<Image> images;
	Mono<Image> findSome;
	
	@Before
	public void setUp() {

		Image one = new Image("1", "learning-spring-boot-cover.jpg");
		Image two = new Image("2", "learning-spring-boot-2nd-edition-cover.jpg");
		Image three = new Image("3", "bazinga.png");
		
		setup = repository
				.deleteAll()
				.thenMany(repository.saveAll(Flux.just(one, two, three)));
		
		find = repository.findAll();
		
		images = Flux.from(setup).thenMany(find);
		
	}
	
	@Test
	public void findAllShouldWork() {
		
		StepVerifier.create(images)
			.recordWith(ArrayList::new) 
			.expectNextCount(3)
			.consumeRecordedWith(results -> {
				assertThat(results).hasSize(3);
				assertThat(results)
					.extracting(Image::getName)
					.contains(
						"learning-spring-boot-cover.jpg",
						"learning-spring-boot-2nd-edition-cover.jpg",
						"bazinga.png"
					);
					
			})
			.expectComplete()
			.verify();
	}
	
	@Test
	public void findByNamesShouldwork() {
		
		findSome = repository.findByName("bazinga.png");
		
		StepVerifier.create(findSome)
			.expectNextMatches(results ->{
				assertThat(results.getName()).isEqualTo("bazinga.png");
				assertThat(results.getId()).isEqualTo("3");
				return true;
			});
	}
}
