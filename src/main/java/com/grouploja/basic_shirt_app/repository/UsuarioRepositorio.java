package com.grouploja.basic_shirt_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grouploja.basic_shirt_app.entiny.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);
}