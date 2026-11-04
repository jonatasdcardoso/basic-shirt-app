package com.grouploja.basic_shirt_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.entiny.Carrinho;
import com.grouploja.basic_shirt_app.entiny.Categoria;
import com.grouploja.basic_shirt_app.entiny.FormaPagamento;
import com.grouploja.basic_shirt_app.entiny.Pedido;
import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.entiny.Usuario;

@SpringBootTest
@Transactional
class FluxoCompraServicoIntegracaoTest {

    @Autowired
    private CategoriaServico categoriaServico;

    @Autowired
    private ProdutoServico produtoServico;

    @Autowired
    private UsuarioServico usuarioServico;

    @Autowired
    private CarrinhoServico carrinhoServico;

    @Autowired
    private PedidoServico pedidoServico;

    @Test
    void deveExecutarCicloCompletoDoCarrinhoAoPedidoComBaixaDeEstoque() {
        Categoria categoria = categoriaServico.salvar(new Categoria("Camisetas Teste"));
        Produto produto = produtoServico.salvar(new Produto(
            "Camiseta Básica Algodão",
            "Camiseta de teste de integração",
            new BigDecimal("100.00"),
            5,
            categoria
        ));
        Usuario cliente = usuarioServico.cadastrarCliente(
            "Lucas Comprador",
            "lucas.compra@teste.com",
            "123456",
            "123456"
        );

        // Adiciona 2 unidades e depois soma mais 1 unidade do mesmo produto
        carrinhoServico.adicionarProduto(cliente.getId(), produto.getId(), 2);
        Carrinho carrinho = carrinhoServico.adicionarProduto(cliente.getId(), produto.getId(), 1);

        assertEquals(1, carrinho.getItens().size());
        assertEquals(3, carrinho.getItens().get(0).getQuantidade());
        assertEquals(0, new BigDecimal("300.00").compareTo(carrinhoServico.calcularTotal(cliente.getId())));

        // Impede adicionar acima do estoque disponivel (5 unidades)
        assertThrows(
            IllegalArgumentException.class,
            () -> carrinhoServico.adicionarProduto(cliente.getId(), produto.getId(), 3)
        );

        // Finaliza a compra com PIX
        Pedido pedido = pedidoServico.finalizarCompra(cliente.getId(), FormaPagamento.PIX);

        assertNotNull(pedido.getId());
        assertEquals(FormaPagamento.PIX, pedido.getFormaPagamento());
        assertEquals(0, new BigDecimal("300.00").compareTo(pedido.getValorTotal()));
        assertEquals(1, pedido.getItens().size());

        // Verifica baixa automatica de estoque (5 - 3 = 2) e esvaziamento do carrinho
        Produto produtoAtualizado = produtoServico.buscarPorId(produto.getId()).orElseThrow();
        assertEquals(2, produtoAtualizado.getEstoque());
        assertTrue(carrinhoServico.buscarOuCriar(cliente.getId()).getItens().isEmpty());
    }

    @Test
    void deveImpedirFinalizarCarrinhoVazioEIsolarPedidosPorUsuario() {
        Categoria categoria = categoriaServico.salvar(new Categoria("Isolamento Teste"));
        Produto produto = produtoServico.salvar(new Produto(
            "Camiseta Pima",
            "Teste de isolamento",
            new BigDecimal("120.00"),
            4,
            categoria
        ));
        Usuario usuarioA = usuarioServico.cadastrarCliente("Usuário A", "usuarioa@teste.com", "123456", "123456");
        Usuario usuarioB = usuarioServico.cadastrarCliente("Usuário B", "usuariob@teste.com", "123456", "123456");

        carrinhoServico.buscarOuCriar(usuarioA.getId());
        assertThrows(
            IllegalArgumentException.class,
            () -> pedidoServico.finalizarCompra(usuarioA.getId(), FormaPagamento.BOLETO)
        );

        carrinhoServico.adicionarProduto(usuarioA.getId(), produto.getId(), 1);
        Pedido pedidoA = pedidoServico.finalizarCompra(usuarioA.getId(), FormaPagamento.PIX);

        // Usuario B nao pode consultar o pedido pertencente ao Usuario A
        assertThrows(
            IllegalArgumentException.class,
            () -> pedidoServico.buscarPorId(pedidoA.getId(), usuarioB.getId())
        );
    }
}
