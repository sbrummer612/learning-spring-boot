package com.brummer.learningspringboot.images.domain;

//import org.junit.Test;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.brummer.learningspringboot.images.domain.Image;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class ImageTests {

	@Test
	public void imagesManagedByLombokShouldWork() {
		String id = "id";
		String fileName = "file-name.jpg";
		Image image = new Image(id, fileName);
		assertThat(image.getId()).isEqualTo(id);
		assertThat(image.getName()).isEqualTo(fileName);
	}
}
