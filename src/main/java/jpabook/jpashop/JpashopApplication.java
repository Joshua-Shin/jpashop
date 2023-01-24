package jpabook.jpashop;

import javax.persistence.PersistenceContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
//		롬복 테스트
//		Hello hello = new Hello();
//		hello.setData("hello");
//		System.out.println("data = " + hello.getData());
		SpringApplication.run(JpashopApplication.class, args);

	}

}
