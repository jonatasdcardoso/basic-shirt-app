package com.grouploja.basic_shirt_app.service;

public class ProdutoNaoEncontradoException extends RuntimeException{
    public ProdutoNaoEncontradoException(Long id){
        super("Produto não encontrado: " + id);
    }
}