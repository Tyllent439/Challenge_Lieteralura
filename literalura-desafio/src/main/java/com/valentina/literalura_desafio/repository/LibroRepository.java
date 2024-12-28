package com.valentina.literalura_desafio.repository;

import com.valentina.literalura_desafio.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro,Long> {
    Optional<Libro> findByTitulo(String titulo);
    @Query("SELECT l FROM Libro l JOIN FETCH l.autor")
    List<Libro> findAllWithAutor();

    @Query("SELECT l FROM Libro l JOIN FETCH l.autor WHERE l.idioma = :idioma")
    List<Libro> findAllWithAutorByIdioma(@Param("idioma") String idioma);




}
