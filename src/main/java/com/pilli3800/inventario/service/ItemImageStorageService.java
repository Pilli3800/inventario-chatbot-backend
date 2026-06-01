package com.pilli3800.inventario.service;

import com.pilli3800.inventario.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Service
public class ItemImageStorageService {

    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024;
    private static final Map<String, String> EXTENSIONES_PERMITIDAS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path itemsPath;
    private final String publicBaseUrl;

    public ItemImageStorageService(
            @Value("${app.uploads.items-path:uploads/items}") String itemsPath,
            @Value("${app.uploads.items-public-base-url:/uploads/items}") String publicBaseUrl
    ) {
        this.itemsPath = Paths.get(itemsPath).toAbsolutePath().normalize();
        this.publicBaseUrl = normalizarBaseUrl(publicBaseUrl);
    }

    public String guardarImagenItem(String codigoItem, MultipartFile file, String imagenActualUrl) throws IOException {
        validarArchivo(file);
        Files.createDirectories(itemsPath);

        eliminarImagen(imagenActualUrl);

        String extension = EXTENSIONES_PERMITIDAS.get(file.getContentType());
        String nombreArchivo = codigoItem + "." + extension;
        Path destino = itemsPath.resolve(nombreArchivo).normalize();

        if (!destino.startsWith(itemsPath)) {
            throw new ValidationException(List.of("Ruta de imagen invalida"));
        }

        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return publicBaseUrl + "/" + nombreArchivo;
    }

    public void eliminarImagen(String imagenUrl) throws IOException {
        if (imagenUrl == null || imagenUrl.isBlank() || !imagenUrl.startsWith(publicBaseUrl + "/")) {
            return;
        }

        String nombreArchivo = imagenUrl.substring((publicBaseUrl + "/").length());
        Path archivo = itemsPath.resolve(nombreArchivo).normalize();
        if (archivo.startsWith(itemsPath)) {
            Files.deleteIfExists(archivo);
        }
    }

    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException(List.of("La imagen es obligatoria"));
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ValidationException(List.of("La imagen no debe superar 2 MB"));
        }
        if (!EXTENSIONES_PERMITIDAS.containsKey(file.getContentType())) {
            throw new ValidationException(List.of("Solo se permiten imagenes JPG, PNG o WEBP"));
        }
    }

    private String normalizarBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
