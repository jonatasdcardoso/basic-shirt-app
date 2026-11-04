
package com.grouploja.basic_shirt_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grouploja.basic_shirt_app.entiny.Carrinho;

public interface CarrinhoRepositorio extends JpaRepository<Carrinho, Long> {

    // Buscar carrinho pelo ID do usuário
    Optional<Carrinho> findByUsuarioId(Long usuarioId);

    // Verificar se o usuário já possui um carrinho
    boolean existsByUsuarioId(Long usuarioId);
}

