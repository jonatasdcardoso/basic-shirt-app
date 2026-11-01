
package com.grouploja.basic_shirt_app.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grouploja.basic_shirt_app.entiny.Carrinho;
import com.grouploja.basic_shirt_app.entiny.ItemCarrinho;
import com.grouploja.basic_shirt_app.service.CarrinhoServico;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoController {

    private final CarrinhoServico carrinhoServico;

    public CarrinhoController(CarrinhoServico carrinhoServico) {
        this.carrinhoServico = carrinhoServico;
    }

    // Consultar carrinho do usuário
    @GetMapping("/{usuarioId}")
    public ResponseEntity<CarrinhoResponse> consultar(
            @PathVariable Long usuarioId) {

        Carrinho carrinho = carrinhoServico.buscarOuCriar(usuarioId);
        BigDecimal total = carrinhoServico.calcularTotal(usuarioId);

        return ResponseEntity.ok(CarrinhoResponse.converter(carrinho, total));
    }

    // Adicionar produto ao carrinho
    @PostMapping("/{usuarioId}/itens")
    public ResponseEntity<CarrinhoResponse> adicionarProduto(
            @PathVariable Long usuarioId,
            @RequestBody AdicionarItemRequest request) {

        Carrinho carrinho = carrinhoServico.adicionarProduto(
            usuarioId,
            request.produtoId(),
            request.quantidade()
        );

        BigDecimal total = carrinhoServico.calcularTotal(usuarioId);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CarrinhoResponse.converter(carrinho, total));
    }

    // Alterar quantidade de um item
    @PutMapping("/{usuarioId}/itens/{itemId}")
    public ResponseEntity<CarrinhoResponse> alterarQuantidade(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId,
            @RequestBody AlterarQuantidadeRequest request) {

        Carrinho carrinho = carrinhoServico.alterarQuantidade(
            usuarioId, itemId, request.quantidade()
        );

        BigDecimal total = carrinhoServico.calcularTotal(usuarioId);

        return ResponseEntity.ok(CarrinhoResponse.converter(carrinho, total));
    }

    // Remover item do carrinho
    @DeleteMapping("/{usuarioId}/itens/{itemId}")
    public ResponseEntity<CarrinhoResponse> removerItem(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId) {

        Carrinho carrinho = carrinhoServico.removerItem(usuarioId, itemId);

        BigDecimal total = carrinhoServico.calcularTotal(usuarioId);

        return ResponseEntity.ok(CarrinhoResponse.converter(carrinho, total));
    }

    // Dados recebidos na inclusão de um produto
    public record AdicionarItemRequest(
        Long produtoId,
        Integer quantidade
    ) {
    }

    // Dados recebidos na alteração de quantidade
    public record AlterarQuantidadeRequest(
        Integer quantidade
    ) {
    }

    // Resposta simplificada do carrinho
    public record CarrinhoResponse(
        Long id,
        Long usuarioId,
        List<ItemResponse> itens,
        BigDecimal valorTotal
    ) {

        public static CarrinhoResponse converter(
                Carrinho carrinho, BigDecimal total) {

            List<ItemResponse> itens = carrinho.getItens().stream()
                .map(ItemResponse::converter)
                .toList();

            return new CarrinhoResponse(
                carrinho.getId(),
                carrinho.getUsuario().getId(),
                itens,
                total
            );
        }
    }

    // Resposta simplificada de cada item
    public record ItemResponse(
        Long id,
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
    ) {

        public static ItemResponse converter(ItemCarrinho item) {
            BigDecimal preco = item.getProduto().getPreco();

            return new ItemResponse(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                preco,
                preco.multiply(BigDecimal.valueOf(item.getQuantidade()))
            );
        }
    }
}

