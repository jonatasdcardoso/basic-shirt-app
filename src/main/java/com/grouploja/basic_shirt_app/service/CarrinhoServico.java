
package com.grouploja.basic_shirt_app.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.entiny.Carrinho;
import com.grouploja.basic_shirt_app.entiny.ItemCarrinho;
import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.repository.CarrinhoRepositorio;
import com.grouploja.basic_shirt_app.repository.ProdutoRepositorio;
import com.grouploja.basic_shirt_app.repository.UsuarioRepositorio;

@Service
public class CarrinhoServico {

    private final CarrinhoRepositorio carrinhoRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public CarrinhoServico(
            CarrinhoRepositorio carrinhoRepositorio,
            ProdutoRepositorio produtoRepositorio,
            UsuarioRepositorio usuarioRepositorio) {
        this.carrinhoRepositorio = carrinhoRepositorio;
        this.produtoRepositorio = produtoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // Buscar carrinho ou criar caso ainda não exista
    @Transactional
    public Carrinho buscarOuCriar(Long usuarioId) {

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Usuário não encontrado."));

        return carrinhoRepositorio.findByUsuarioId(usuarioId)
            .orElseGet(() -> {
                Carrinho carrinho = new Carrinho(usuario);
                usuario.setCarrinho(carrinho);
                return carrinhoRepositorio.save(carrinho);
            });
    }

    // Adicionar produto ao carrinho
    @Transactional
    public Carrinho adicionarProduto(
            Long usuarioId, Long produtoId, Integer quantidade) {

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                "A quantidade deve ser maior que zero.");
        }

        Carrinho carrinho = buscarOuCriar(usuarioId);

        Produto produto = produtoRepositorio.findById(produtoId)
            .orElseThrow(() -> new ProdutoNaoEncontradoException(produtoId));

        ItemCarrinho itemExistente = carrinho.getItens().stream()
            .filter(item -> item.getProduto().getId().equals(produtoId))
            .findFirst()
            .orElse(null);

        int quantidadeFinal = quantidade;

        if (itemExistente != null) {
            quantidadeFinal += itemExistente.getQuantidade();
        }

        if (produto.getEstoque() == null ||
            quantidadeFinal > produto.getEstoque()) {
            throw new IllegalArgumentException(
                "Estoque insuficiente para esse produto.");
        }

        if (itemExistente != null) {
            itemExistente.setQuantidade(quantidadeFinal);
        } else {
            ItemCarrinho novoItem = new ItemCarrinho(produto, quantidade);
            carrinho.adicionarItem(novoItem);
        }

        return carrinhoRepositorio.save(carrinho);
    }

    // Alterar quantidade de um item
    @Transactional
    public Carrinho alterarQuantidade(
            Long usuarioId, Long itemId, Integer quantidade) {

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                "A quantidade deve ser maior que zero.");
        }

        Carrinho carrinho = buscarOuCriar(usuarioId);

        ItemCarrinho item = carrinho.getItens().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Item não encontrado no carrinho."));

        if (item.getProduto().getEstoque() == null ||
            quantidade > item.getProduto().getEstoque()) {
            throw new IllegalArgumentException(
                "Estoque insuficiente para esse produto.");
        }

        item.setQuantidade(quantidade);

        return carrinhoRepositorio.save(carrinho);
    }

    // Remover item do carrinho
    @Transactional
    public Carrinho removerItem(Long usuarioId, Long itemId) {

        Carrinho carrinho = buscarOuCriar(usuarioId);

        ItemCarrinho item = carrinho.getItens().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Item não encontrado no carrinho."));

        carrinho.removerItem(item);

        return carrinhoRepositorio.save(carrinho);
    }

    // Calcular valor total do carrinho
    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(Long usuarioId) {

        Carrinho carrinho = carrinhoRepositorio.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Carrinho não encontrado."));

        return carrinho.getItens().stream()
            .map(item -> item.getProduto().getPreco()
                .multiply(BigDecimal.valueOf(item.getQuantidade())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

