package com.harishkannarao.thirukkural;

import com.harishkannarao.thirukkural.epub.EpubCreator;
import com.harishkannarao.thirukkural.transform.JsonStructureTransformer;
import com.harishkannarao.thirukkural.transform.LanguageTransformer;
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
		} else if (Objects.equals(task, "transform_language")) {
			LanguageTransformer languageTransformer = context.getBean(LanguageTransformer.class);
			languageTransformer.transformBook();
		} else if (Objects.equals(task, "create_book")) {
			EpubCreator languageTransformer = context.getBean(EpubCreator.class);
			languageTransformer.createBook();
		}
	}

}
