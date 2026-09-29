package com.grouploja.repository;

import com.grouploja.basic_shirt_app.entiny.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {
    
}
