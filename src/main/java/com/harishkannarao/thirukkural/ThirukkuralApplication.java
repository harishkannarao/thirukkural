package com.harishkannarao.thirukkural;

import com.harishkannarao.thirukkural.data.JsonStructureTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

@SpringBootApplication
public class ThirukkuralApplication {

	private static final Logger LOG = LoggerFactory.getLogger(ThirukkuralApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ThirukkuralApplication.class, args);
		String task = context.getEnvironment().getProperty("task");
		if (Objects.equals(task, "transform_raw")) {
			JsonStructureTransformer structureTransformer = context.getBean(JsonStructureTransformer.class);
			structureTransformer.transform();
		}
	}

}
