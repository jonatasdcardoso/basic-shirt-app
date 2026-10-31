package com.grouploja.basic_shirt_app.repository;

import com.grouploja.basic_shirt_app.entiny.Produto;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProdutoRepositorio extends JpaRepository<Produto, Long> {
}
