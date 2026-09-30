package com.grouploja.basic_shirt_app.service;

public class CategoriaNaoEncontradaException extends RuntimeException {
    public CategoriaNaoEncontradaException(Long id) {
        super("Categoria não encontrada: " + id);
    }
}
