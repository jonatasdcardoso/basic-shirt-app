package com.grouploja.basic_shirt_app.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.entiny.Carrinho;
import com.grouploja.basic_shirt_app.entiny.FormaPagamento;
import com.grouploja.basic_shirt_app.entiny.ItemCarrinho;
import com.grouploja.basic_shirt_app.entiny.ItemPedido;
import com.grouploja.basic_shirt_app.entiny.Pedido;
import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.repository.CarrinhoRepositorio;
import com.grouploja.basic_shirt_app.repository.PedidoRepositorio;
import com.grouploja.basic_shirt_app.repository.ProdutoRepositorio;
import com.grouploja.basic_shirt_app.repository.UsuarioRepositorio;

@Service
public class PedidoServico {

    private final PedidoRepositorio pedidoRepositorio;
    private final CarrinhoRepositorio carrinhoRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public PedidoServico(
            PedidoRepositorio pedidoRepositorio,
            CarrinhoRepositorio carrinhoRepositorio,
            ProdutoRepositorio produtoRepositorio,
            UsuarioRepositorio usuarioRepositorio) {

        this.pedidoRepositorio = pedidoRepositorio;
        this.carrinhoRepositorio = carrinhoRepositorio;
        this.produtoRepositorio = produtoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // Finalizar compra
    @Transactional
    public Pedido finalizarCompra(
            Long usuarioId, FormaPagamento formaPagamento) {

        if (formaPagamento == null) {
            throw new IllegalArgumentException(
                "A forma de pagamento é obrigatória.");
        }

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Usuário não encontrado."));

        Carrinho carrinho = carrinhoRepositorio.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Carrinho não encontrado."));

        if (carrinho.getItens().isEmpty()) {
            throw new IllegalArgumentException(
                "Não é possível finalizar um carrinho vazio.");
        }

        Pedido pedido = new Pedido(usuario, formaPagamento);

        BigDecimal valorTotal = BigDecimal.ZERO;

        // Conferir estoque e registrar os itens do pedido
        for (ItemCarrinho item : carrinho.getItens()) {

            // Buscar produto com bloqueio pessimista
            Produto produto = produtoRepositorio.buscarPorIdComBloqueio(
                item.getProduto().getId())
                .orElseThrow(() -> new ProdutoNaoEncontradoException(
                    item.getProduto().getId()));

            // Verificar se o produto continua ativo
            if (!produto.isAtivo()) {
                throw new IllegalArgumentException(
                    "O produto " + produto.getNome()
                    + " não está mais disponível para compra.");
            }

            // Verificar estoque
            if (produto.getEstoque() == null ||
                produto.getEstoque() < item.getQuantidade()) {

                throw new IllegalArgumentException(
                    "Estoque insuficiente para o produto: "
                    + produto.getNome());
            }

            // Registrar preço atual no histórico do pedido
            ItemPedido itemPedido = new ItemPedido(
                produto,
                item.getQuantidade(),
                produto.getPreco()
            );

            pedido.adicionarItem(itemPedido);

            valorTotal = valorTotal.add(itemPedido.getSubtotal());

            // Atualizar estoque
            produto.setEstoque(
                produto.getEstoque() - item.getQuantidade());

            produtoRepositorio.save(produto);
        }

        pedido.setValorTotal(valorTotal);

        // Salvar pedido
        Pedido pedidoSalvo = pedidoRepositorio.save(pedido);

        // Esvaziar carrinho somente após salvar o pedido
        carrinho.esvaziar();
        carrinhoRepositorio.save(carrinho);

        return pedidoSalvo;
    }

    // Listar pedidos de um usuário
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos(Long usuarioId) {
        List<Pedido> pedidos = pedidoRepositorio.findByUsuarioIdOrderByDataPedidoDesc(usuarioId);
        pedidos.forEach(p -> p.getItens().size());
        return pedidos;
    }

    // Listar todos os pedidos do sistema (Painel Administrativo)
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        List<Pedido> pedidos = pedidoRepositorio.findAllByOrderByDataPedidoDesc();
        pedidos.forEach(p -> p.getItens().size());
        return pedidos;
    }

    // Buscar pedido específico de um usuário
    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long pedidoId, Long usuarioId) {
        Pedido pedido = pedidoRepositorio.findByIdAndUsuarioId(pedidoId, usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        pedido.getItens().size();
        return pedido;
    }
}