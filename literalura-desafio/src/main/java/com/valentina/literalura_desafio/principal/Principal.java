package com.valentina.literalura_desafio.principal;

import com.valentina.literalura_desafio.model.*;
import com.valentina.literalura_desafio.repository.AutorRepository;
import com.valentina.literalura_desafio.repository.LibroRepository;
import com.valentina.literalura_desafio.service.ConsumoAPI;
import com.valentina.literalura_desafio.service.ConvierteDatos;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private static final Object URL_BASE = "https://gutendex.com/books/";
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosLibro> datosLibros = new ArrayList<>();
    private LibroRepository repositorioLibros;
    private AutorRepository repositorioAutores;


    public Principal(LibroRepository repositorioLibros, AutorRepository repositorioAutores) {
        this.repositorioLibros = repositorioLibros;
        this.repositorioAutores = repositorioAutores;
    }

    public void muestraElMenu(){
        var opcion = -1;
        while (opcion != 0){
            var menu = """
                    
                    --- MENU ---
                    Seleccione una opcion:
                    1 - Buscar libros por titulo
                    2 - Listar libros regristrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma
                    
                    0 - Salir
                    """;

            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion){

                case 1:
                    buscarLibroYMostrarAutores();
                    break;

                case 2:
                    listarLibrosRegistrados();
                    break;

                case 3:
                    listarAutoresRegistrados();
                    break;

                case 4:
                    listarAutorPorAño();
                    break;

                case 5:
                    listarLibrosPorIdioma();
                    break;
            }
        }
    }

    private DatosLibro getDatosLibro(){
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        Datos datos = conversor.obtenerDatos(json, Datos.class);

        Optional<DatosLibro> libroBuscado = datos.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();

        if(libroBuscado.isPresent()){
            return libroBuscado.get();

        }else {
            System.out.println("Libro no encontrado");
            return null;
        }
    }

    private List<DatosAutor> getDatosAutores() {
        // Obtener los datos del libro
        DatosLibro datosLibro = getDatosLibro();

        if (datosLibro == null) {
            System.out.println("No se encontró el libro, por lo tanto no se puede obtener información de autores.");
            return Collections.emptyList(); // Retornar una lista vacía si no hay libro
        }

        // Verificar si hay autores asociados
        if (datosLibro.autor() == null || datosLibro.autor().isEmpty()) {
            System.out.println("El libro no tiene información de autores.");
            return Collections.emptyList();
        }

        // Retornar la lista completa de autores
        return datosLibro.autor();
    }

    private void buscarLibroYMostrarAutores() {
        // Obtener los datos del libro
        DatosLibro datosLibro = getDatosLibro();

        // SI NO SE ENCUENTRA LIBRO
        if (datosLibro == null) {
            System.out.println("No se encontró el libro.");
            return;
        }

        //VERIFICAR SI EL LIBRO ESTA EN LA BASE DE DATOS
        Optional<Libro> libroExistente = repositorioLibros.findByTitulo(datosLibro.titulo());
        if(libroExistente.isPresent()){
            System.out.println("Libro ya registrado en base de datos");
            return;
        }

        // MOSTRAR INFORMACION DEL LIBRO
        System.out.println("\n--- LIBRO ---");
        System.out.println("Titulo: " + datosLibro.titulo());
        System.out.println("Idioma: " + datosLibro.idioma());
        System.out.println("NumeroDeDescargas: " + datosLibro.numeroDeDescargas());

        // GUARDAR LIBRO EN LA BASE DE DATOS
        Libro libro = new Libro(datosLibro);

        // OBTENER Y GUARDAR INFORMACION DE LOS AUTORES
        List<DatosAutor> datosAutores = datosLibro.autor();

        if (datosAutores == null || datosAutores.isEmpty()) {
            System.out.println("\nNo se encontró información de autor para este libro.");
        } else {
            //System.out.println("\n--- Información de los autores ---");
            for (DatosAutor datosAutor : datosAutores) {
                System.out.println("Autor: " + datosAutor.nombre());
                //System.out.println("Fecha de Nacimiento: " + datosAutor.fechaDeNacimiento());
                //System.out.println("Fecha de Fallecimiento: " + datosAutor.fechaDeFallecimiento());
                System.out.println();

                // Verificar si el autor ya existe en la base de datos
                Optional<Autor> autorExistente = repositorioAutores.findByNombre(datosAutor.nombre());

                Autor autor;
                if (autorExistente.isPresent()) {
                    // Si el autor ya existe, se utiliza el existente
                    autor = autorExistente.get();
                } else {
                    // Si el autor no existe, se crea y guarda uno nuevo
                    autor = new Autor(datosAutor);
                    repositorioAutores.save(autor);
                }

                // Asociar el autor al libro
                libro.setAutor(autor);
            }
        }

        // Guardar el libro en la base de datos
        repositorioLibros.save(libro);
        System.out.println("\nEl libro y sus autores han sido guardados en la base de datos.");
    }


    private void listarLibrosRegistrados(){
        List<Libro> libros = repositorioLibros.findAllWithAutor();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en la base de datos.");
            return;
        }

        System.out.println("--- Libros registrados ---");
        System.out.println();
        for (Libro libro : libros) {
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Numero de descargar: " + libro.getNumeroDeDescargas());
            System.out.println("Autor: " + libro.getAutor().getNombre());
            System.out.println("-----");
            System.out.println();
        }
    }

    private void listarAutoresRegistrados(){
        List<Autor> autores = repositorioAutores.findAllWithLibros();
        if(autores.isEmpty()){
            System.out.println("No hay autores registrados en la base de datos");
            return;
        }

        System.out.println("---Autores registrados---");
        System.out.println();
        for (Autor autor : autores){
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Fecha de nacimiento: " + autor.getFechaDeNacimiento());
            System.out.println("Fecha de fallecimiento: " + autor.getFechaDeFallecimiento());
            System.out.println("Libros: " + autor.getLibros().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.toList()));

            System.out.println("------");
            System.out.println();
        }
    }

    private void listarAutorPorAño(){
        System.out.println("Ingrese el año vivo del autor(es) que desea buscar: ");
        int year = teclado.nextInt();
        teclado.nextLine();
        List<Autor> autores = repositorioAutores.findAuthorBetweenYear(year);

        if(autores.isEmpty()){
            System.out.println("No hay autores vivos registrados en esa fecha ");
            return;
        }

        System.out.println("--- Autores vivos en año elegido ---");
        System.out.println();
        for (Autor autor : autores){
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Fecha de nacimiento: " + autor.getFechaDeNacimiento());
            System.out.println("Fecha de fallecimiento: " + autor.getFechaDeFallecimiento());
            System.out.println("------");
            System.out.println();
        }
    }

    private void listarLibrosPorIdioma(){
        System.out.println("Ingrese el idioma que desea buscar: ");
        System.out.println("""
                es -> Español
                en -> Inglés
                fr -> Francés
                pt -> Portugés
                """);

        String idioma = teclado.nextLine();
        List<Libro> libros = repositorioLibros.findAllWithAutorByIdioma(idioma);
        if(libros.isEmpty()){
            System.out.println("No hay libros registrados en ese idioma ");
            return;
        }
        System.out.println("--- Libros por idioma ---");
        System.out.println();
        for (Libro libro : libros){
            System.out.println("------");
            System.out.println("Titulo: " + libro.getTitulo());
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Numero de descargas: " + libro.getNumeroDeDescargas());
            System.out.println("Autor: " + libro.getAutor().getNombre());
            System.out.println("----");

        }
    }

}
