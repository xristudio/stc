package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve las imágenes de resources/autos bajo la URL /autos/
        registry.addResourceHandler("/autos/**")
                .addResourceLocations("classpath:autos/");
        // Sirve las imágenes de resources/pistas bajo la URL /pistas/
        registry.addResourceHandler("/pistas/**")
                .addResourceLocations("classpath:pistas/");
        // Sirve los estáticos estándar
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:static/");
    }
}
