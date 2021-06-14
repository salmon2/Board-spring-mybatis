package com.example.blog_board;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.example.blog_board")
public class BlogBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogBoardApplication.class, args);
	}

}
