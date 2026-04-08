package com.thehorselegend.summs;

import com.thehorselegend.summs.infrastructure.config.GoogleMapsProperties;
import com.thehorselegend.summs.infrastructure.config.StmApiProperties;
import com.thehorselegend.summs.shared.time.SummsTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties({StmApiProperties.class, GoogleMapsProperties.class})
public class SummsApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(SummsTime.zoneId()));
		SpringApplication.run(SummsApplication.class, args);
	}

}
