// PATH: src/main/java/com/ojash/workoutrec/config/TensorFlowConfig.java
package com.ojash.workoutrec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.tensorflow.SavedModelBundle;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class TensorFlowConfig {

    @Bean
    public SavedModelBundle savedModelBundle() {
        try {
            // NOTE: The user mentioned they renamed the folder.
            // Using the name from the latest logs.
            Path modelPath = Paths.get(new ClassPathResource("rl_workout_saved_model").getURI());
            System.out.println("Loading TensorFlow model from: " + modelPath);
            return SavedModelBundle.load(modelPath.toString(), "serve");
        } catch (IOException e) {
            System.err.println("FAILED TO LOAD TENSORFLOW MODEL");
            throw new RuntimeException(e);
        }
    }
}