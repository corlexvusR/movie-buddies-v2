package com.moviebuddies;

import org.springframework.boot.SpringApplication;

public class TestMovieBuddiesBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(MovieBuddiesBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
