
package com.grouploja.basic_shirt_app.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grouploja.basic_shirt_app.entiny.FormaPagamento;
import com.grouploja.basic_shirt_app.entiny.ItemPedido;
import com.grouploja.basic_shirt_app.entiny.Pedido;
import com.grouploja.basic_shirt_app.service.PedidoServico;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoServico pedidoServico;

    public PedidoController(PedidoServico pedidoServico) {
        this.pedidoServico = pedidoServico;
    }

    // Finalizar compra
    @PostMapping("/{usuarioId}")
    public ResponseEntity<PedidoResponse> finalizarCompra(
            @PathVariable Long usuarioId,
            @RequestBody FinalizarPedidoRequest request) {

        Pedido pedido = pedidoServico.finalizarCompra(
            usuarioId, request.formaPagamento()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(PedidoResponse.converter(pedido));
    }

    // Listar pedidos de um usuário
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<PedidoResponse>> listarPedidos(
            @PathVariable Long usuarioId) {

        List<PedidoResponse> pedidos = pedidoServico
            .listarPedidos(usuarioId)
            .stream()
            .map(PedidoResponse::converter)
            .toList();

        return ResponseEntity.ok(pedidos);
    }

    // Buscar pedido específico de um usuário
    @GetMapping("/{usuarioId}/{pedidoId}")
    public ResponseEntity<PedidoResponse> buscarPorId(
            @PathVariable Long usuarioId,
            @PathVariable Long pedidoId) {

        Pedido pedido = pedidoServico.buscarPorId(pedidoId, usuarioId);

        return ResponseEntity.ok(PedidoResponse.converter(pedido));
    }

    // Dados recebidos para finalizar a compra
    public record FinalizarPedidoRequest(
        FormaPagamento formaPagamento
    ) {
    }

    // Resposta simplificada do pedido
    public record PedidoResponse(
        Long id,
        Long usuarioId,
        LocalDateTime dataPedido,
        FormaPagamento formaPagamento,
        BigDecimal valorTotal,
        List<ItemPedidoResponse> itens
    ) {

        public static PedidoResponse converter(Pedido pedido) {

            List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(ItemPedidoResponse::converter)
                .toList();

            return new PedidoResponse(
                pedido.getId(),
                pedido.getUsuario().getId(),
                pedido.getDataPedido(),
                pedido.getFormaPagamento(),
                pedido.getValorTotal(),
                itens
            );
        }
    }

    // Resposta simplificada dos itens do pedido
    public record ItemPedidoResponse(
        Long id,
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
    ) {

        public static ItemPedidoResponse converter(ItemPedido item) {
            return new ItemPedidoResponse(
                item.getId(),
                item.getProduto().getId(),
                item.getNomeProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
            );
        }
    }
}
