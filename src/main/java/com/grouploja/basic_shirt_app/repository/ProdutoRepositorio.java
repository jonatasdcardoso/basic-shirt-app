package com.grouploja.basic_shirt_app.repository;

import java.util.List;
import java.util.Optional;

import com.grouploja.basic_shirt_app.entiny.Produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ProdutoRepositorio extends JpaRepository<Produto, Long> {

    boolean existsByCategoriaId(Long categoriaId);

    List<Produto> findByAtivoTrue();

    Optional<Produto> findByIdAndAtivoTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Produto p WHERE p.id = :id")
    Optional<Produto> buscarPorIdComBloqueio(@Param("id") Long id);
}