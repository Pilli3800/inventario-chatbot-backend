package com.pilli3800.inventario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class RecursosEstaticosConfig implements WebMvcConfigurer {

    private final String rutaItems;

    public RecursosEstaticosConfig(
            @Value("${app.uploads.items-path}") String rutaItems
    ) {
        this.rutaItems = rutaItems;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        agregarRecursosEstaticos(registry);
    }

    private void agregarRecursosEstaticos(ResourceHandlerRegistry registry) {
        String ubicacion = Paths.get(rutaItems).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/items/**")
                .addResourceLocations(ubicacion);
    }
}
