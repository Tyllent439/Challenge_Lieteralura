package com.valentina.literalura_desafio.repository;

import com.valentina.literalura_desafio.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor,Long> {
    Optional<Autor> findByNombre(String nombre);
    @Query(value = "SELECT * FROM autores WHERE :year >= fecha_de_nacimiento AND :year <= fecha_de_fallecimiento", nativeQuery = true)
    List<Autor> findAuthorBetweenYear(int year);
    @Query("SELECT a FROM Autor a JOIN FETCH a.libros")
    List<Autor> findAllWithLibros();
}
