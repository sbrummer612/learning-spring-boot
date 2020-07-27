package com.brummer.learningspringboot.images.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import com.brummer.learningspringboot.images.domain.Image;
import com.brummer.learningspringboot.images.domain.ImageRepository;

/**
 * @author Greg Turnquist
 */
// tag::1[]
@Service
public class ImageService {

	private static String UPLOAD_ROOT = "upload-dir";

	private final ResourceLoader resourceLoader;
	
	private final ImageRepository imageRepository;

	public ImageService(ResourceLoader resourceLoader, ImageRepository imageRepository) {
		this.resourceLoader = resourceLoader;
		this.imageRepository = imageRepository;
	}
// end::1[]

	// tag::2[]
	public Flux<Image> findAllImages() {
		System.out.println("ImageService-findAllImages");
		return imageRepository.findAll().log("findAllImages");
	}
	// end::2[]

	// tag::3[]
	public Mono<Resource> findOneImage(String filename) {
		return Mono.fromSupplier(() ->
			resourceLoader.getResource(
				"file:" + UPLOAD_ROOT + "/" + filename));
	}
	// end::3[]

	// tag::4[]
	public Mono<Void> createImage(Flux<FilePart> files) {
		return files
			.flatMap(file ->
			{
				Mono<Image> saveDatabaseImage = imageRepository.save(
					new Image(
						UUID.randomUUID().toString(), 
						file.filename()))
						.log("createImage_save");
					
					Mono<Void> copyFile = Mono.just(
						Paths.get(UPLOAD_ROOT, file.filename())
						.toFile())
						.log("createImage-picktarget")
						.map(destFile -> {
							try {
								destFile.createNewFile();
								return destFile;
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						})
						.log("createImage-newfile")
						.flatMap(file::transferTo)
						.log("createImage-copy");
					
					return Mono.when(saveDatabaseImage, copyFile)
							.log("createImage-when");
			})
			.log("createImage-flatMap")
			.then()
			.log("createImage-done`");
	}
	// end::4[]

	// tag::5[]
	public Mono<Void> deleteImage(String filename) {
		Mono<Void> deleteDatabaseImage = imageRepository
				.findByName(filename)
				.log("deleteImage-find")
				.flatMap(imageRepository::delete)
				.log("deleteImage-record");
		
		Mono<Object> deleteFile = Mono.fromRunnable(() -> {
			try {
				Files.deleteIfExists(
					Paths.get(UPLOAD_ROOT, filename));
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).log("deleteImage-file");
		
		return Mono.when(deleteDatabaseImage, deleteFile)
				.log("deleteImage-when")
				.then()
				.log("deleteImage-done");
		
	}
	// end::5[]

	// tag::6[]
	/**
	 * Pre-load some test images
	 *
	 * @return Spring Boot {@link CommandLineRunner} automatically
	 *         run after app context is loaded.
	 */
	@Bean
	CommandLineRunner setUp() throws IOException {
		return (args) -> {
			FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));

			Files.createDirectory(Paths.get(UPLOAD_ROOT));

			FileCopyUtils.copy("Test file",
				new FileWriter(UPLOAD_ROOT +
					"/learning-spring-boot-cover.jpg"));

			FileCopyUtils.copy("Test file2",
				new FileWriter(UPLOAD_ROOT +
					"/learning-spring-boot-2nd-edition-cover.jpg"));

			FileCopyUtils.copy("Test file3",
				new FileWriter(UPLOAD_ROOT + "/bazinga.png"));
		};
	}
	// end::6[]
}