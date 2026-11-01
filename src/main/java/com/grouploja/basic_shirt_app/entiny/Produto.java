package com.grouploja.basic_shirt_app.entiny;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity 
public class Produto {
     
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer estoque;

    @ManyToOne // relacionamento de N para 1.
    private Categoria categoria;

    public Produto() {
    }

    public Produto(String nome,String descricao, BigDecimal preco, Integer estoque, Categoria categoria){
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }
    public String getNome(){
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getDescricao(){
        return descricao;
    }
    public void setDescricao(String descricao){
        this.descricao = descricao;
    }
    public BigDecimal getPreco() {
        return preco;
    }
    public void setPreco(BigDecimal preco){
        this.preco = preco;
    }
    public Integer getEstoque(){
        return estoque;
    }
    public void setEstoque(Integer estoque){
        this.estoque = estoque;
    }
    public Categoria getCategoria() {
        return categoria;
    }
    public void setCategoria(Categoria categoria){
        this.categoria = categoria;
    }

}


