package com.iba.config;

import com.iba.service.Translator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TranslatorConfig {

    @Bean
    public Translator getTranslator(){
        return new Translator();
    }
}
