package com.grouploja.basic_shirt_app.controller;

import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.service.ProdutoServico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoServico produtoServico;

    // criar dependências
    public ProdutoController(ProdutoServico produtoServico) {
        this.produtoServico = produtoServico;
    }

    // listar produtos
    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        List<Produto> produtos = produtoServico.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    // buscar produto com ID
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return produtoServico.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // criar produto
    @PostMapping
    public ResponseEntity<Produto> salvar(@RequestBody Produto produto) {
        Produto novoProduto = produtoServico.salvar(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    // atualizar produto
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        Produto produtoAtualizado = produtoServico.atualizar(id, produto);
        return ResponseEntity.ok(produtoAtualizado);
    }

    // excluir produto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoServico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}