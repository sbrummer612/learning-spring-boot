package com.brummer.learningspringboot.images.domain;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Greg Turnquist
 */
// tag::code[]
@Data
@Document
//@NoArgsConstructor
public class Image {

	@Id 
	final public String id;
	final public String name;

	public Image(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
// end::code[]
