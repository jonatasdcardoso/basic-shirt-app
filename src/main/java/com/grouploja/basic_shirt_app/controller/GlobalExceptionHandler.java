
package com.grouploja.basic_shirt_app.controller;

import com.grouploja.basic_shirt_app.service.CategoriaNaoEncontradaException;
import com.grouploja.basic_shirt_app.service.ProdutoNaoEncontradoException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Captura global de exceções
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoriaNaoEncontradaException.class)
    public ResponseEntity<String> tratarCategoriaNaoEncontrada(
            CategoriaNaoEncontradaException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());
    }

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<String> tratarProdutoNaoEncontrado(
            ProdutoNaoEncontradoException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());
    }

    // Tratar dados inválidos e regras de negócio não atendidas
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarArgumentoInvalido(
            IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.getMessage());
    }
}
