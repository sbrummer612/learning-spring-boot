package com.brummer.learningspringboot.comments.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import com.brummer.learningspringboot.comments.domain.Comment;

import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Mono;

@Controller
public class CommentController {

	private final RabbitTemplate rabbitTemplate;
	private final MeterRegistry meterRegistry;
	
	public CommentController(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry) {
		this.rabbitTemplate = rabbitTemplate;
		this.meterRegistry = meterRegistry;
	}
	
	@PostMapping("/comments")
	public Mono<String> addComments (Mono<Comment> newComment) {
		return newComment.flatMap(comment -> 
				Mono.fromRunnable(() -> rabbitTemplate
						.convertAndSend(
								"learning-spring-boot", 
								"comments.new",
								comment))
				.then(Mono.just(comment)))
				.log("commentService-publish")
				.flatMap(comment -> {
					meterRegistry
						.counter("comments.produced", "imageId", comment.getImageId())
						.increment();
					return Mono.just("redirect:/test");
				});
	
	}
}
