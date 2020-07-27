package com.brummer.learningspringboot.comments.service;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import com.brummer.learningspringboot.comments.domain.Comment;
import com.brummer.learningspringboot.comments.domain.CommentWriterRepository;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class CommentService {
	private CommentWriterRepository commentWriterRepository;
	private final MeterRegistry meterRegistry;
	
	public CommentService(CommentWriterRepository commentWriterRepository, MeterRegistry meterRegistry) {
		this.commentWriterRepository = commentWriterRepository;
		this.meterRegistry = meterRegistry;
	}
	
	@RabbitListener(bindings = @QueueBinding(
		value = @Queue,
		exchange = @Exchange(value = "learning-spring-boot"),
		key= "comments.new"
	))
	public void save(Comment newComment) {
		commentWriterRepository
			.save(newComment)
			.log("commentService-Save")
			.subscribe(comment -> {
				meterRegistry.counter("comments.consumed", "imageId", comment.getImageId())
				.increment();
			});
	}
	
	@Bean
	Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
	
	@Profile("dev")
	@Bean
	CommandLineRunner setUpComments(CommentWriterRepository operations) {
		return args -> {
			operations.deleteAll().subscribe();
		};
	}
	
}
