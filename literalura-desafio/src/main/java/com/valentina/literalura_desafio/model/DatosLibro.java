package com.valentina.literalura_desafio.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosLibro(
        @JsonAlias("title") String titulo,
        @JsonAlias("authors") List<DatosAutor> autor,
        @JsonAlias("languages") List<String> idioma,
        @JsonAlias("download_count") Double numeroDeDescargas) {


    @Override
    public String toString() {

        String nombreAutor = autor.stream()
                .map(DatosAutor::nombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Autor desconocido");

        String idiomasString= String.join(", ", idioma);

        return
                "" +
                "---LIBRO---" + "\n" +
                "Titulo: " + titulo + "\n" +
                "Autor: " + nombreAutor + "\n" +
                "Idioma: " + idiomasString + "\n" +
                "NumeroDeDescargas: " + numeroDeDescargas;
    }

    public String getIdiomaString() {
        return String.join(", ", idioma);
    }

    public String getNombreAutor() {
        return autor.stream()
                .map(DatosAutor::nombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Autor desconocido");
    }

}
