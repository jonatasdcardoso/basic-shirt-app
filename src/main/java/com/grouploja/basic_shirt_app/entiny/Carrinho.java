package com.grouploja.basic_shirt_app.entiny;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrinhos")
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    // Um carrinho possui vários itens
    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<ItemCarrinho> itens = new ArrayList<>();

    public Carrinho() {
    }

    public Carrinho(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public List<ItemCarrinho> getItens() {
        return itens;
    }

    public void adicionarItem(ItemCarrinho item) {
        itens.add(item);
        item.setCarrinho(this);
    }

    public void removerItem(ItemCarrinho item) {
        itens.remove(item);
        item.setCarrinho(null);
    }

    public void esvaziar() {
        itens.clear();
    }
}
