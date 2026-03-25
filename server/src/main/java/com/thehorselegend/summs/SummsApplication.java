package com.thehorselegend.summs;

import com.thehorselegend.summs.shared.time.SummsTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SummsApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(SummsTime.zoneId()));
		SpringApplication.run(SummsApplication.class, args);
	}

}
