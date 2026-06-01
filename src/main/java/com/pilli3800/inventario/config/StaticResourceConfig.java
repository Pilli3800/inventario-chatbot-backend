package com.pilli3800.inventario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String itemsPath;

    public StaticResourceConfig(
            @Value("${app.uploads.items-path:uploads/items}") String itemsPath
    ) {
        this.itemsPath = itemsPath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(itemsPath).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/items/**")
                .addResourceLocations(location);
    }
}
