package com.spring.resilience4j.producer;

import javax.annotation.PostConstruct;

import com.netsurfingzone.dto.Student;
import com.spring.resilience4j.constants.ApplicationConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/produce")
public class KafkaProducer {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	//@PostMapping("/message")
	@PostConstruct
	public String sendMessage() {

		try {
			kafkaTemplate.send(ApplicationConstant.TOPIC_NAME, creaetStudent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "json message sent succuessfully";
	}

	private Student creaetStudent() {
		return Student.builder()
			.id(1L)
			.name("Aashish")
			.rollNumber("1201")
			.build();
	}

}
