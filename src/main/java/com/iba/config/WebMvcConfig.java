package com.iba.config;

import com.iba.security.InterceptorAdapter;
import com.iba.utils.PasswordEncoderMD5;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final InterceptorAdapter interceptorAdapter;

    private OAuth2Config oAuth2Config = new OAuth2Config();

    @Value("${cross.origin.mapping}")
    private String origin;

    @Autowired
    public WebMvcConfig(InterceptorAdapter interceptorAdapter) {
        this.interceptorAdapter = interceptorAdapter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorAdapter);
    }

    @Bean
    public PasswordEncoderMD5 passwordEncoder() {
        return new PasswordEncoderMD5();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .exposedHeaders(oAuth2Config.getSecret(),
                        oAuth2Config.getJwt().getAccess().getSecret(),
                        oAuth2Config.getJwt().getRefresh().getSecret())
                .allowedOrigins(origin)
                .allowedMethods("PUT", "DELETE", "GET", "POST")
                .allowCredentials(false).maxAge(3600);
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]`~\\/");
            }
        });
        return factory;
    }
}
