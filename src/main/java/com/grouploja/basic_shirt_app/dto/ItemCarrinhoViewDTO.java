package com.grouploja.basic_shirt_app.dto;

import java.math.BigDecimal;

import com.grouploja.basic_shirt_app.entiny.ItemCarrinho;
import com.grouploja.basic_shirt_app.entiny.Produto;

public record ItemCarrinhoViewDTO(
    Long itemId,
    Long produtoId,
    String nomeProduto,
    String categoriaNome,
    BigDecimal precoUnitario,
    Integer quantidade,
    Integer estoqueDisponivel,
    BigDecimal subtotal
) {
    public static ItemCarrinhoViewDTO de(ItemCarrinho item) {
        Produto produto = item.getProduto();
        String categoria = produto.getCategoria() != null
            ? produto.getCategoria().getNome()
            : "Sem categoria";
        BigDecimal preco = produto.getPreco();
        BigDecimal subtotal = preco.multiply(BigDecimal.valueOf(item.getQuantidade()));

        return new ItemCarrinhoViewDTO(
            item.getId(),
            produto.getId(),
            produto.getNome(),
            categoria,
            preco,
            item.getQuantidade(),
            produto.getEstoque() != null ? produto.getEstoque() : 0,
            subtotal
        );
    }
}
