package com.grouploja.basic_shirt_app.controller;

import com.grouploja.basic_shirt_app.entiny.Categoria;
import com.grouploja.basic_shirt_app.service.CategoriaServico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaServico categoriaServico;

    // criar dependencias
    public CategoriaController(CategoriaServico categoriaServico) {
        this.categoriaServico = categoriaServico;
    }

    // listar categorias
    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        List<Categoria> categorias = categoriaServico.listarTodas();
        return ResponseEntity.ok(categorias);
    }

    // buscar categoria com id
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        return categoriaServico.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // criar categoria
    @PostMapping
    public ResponseEntity<Categoria> salvar(@RequestBody Categoria categoria) {
        Categoria novaCategoria = categoriaServico.salvar(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCategoria);
    }

    // atualizar categoria
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(@PathVariable Long id, @RequestBody Categoria categoria) {
        Categoria categoriaAtualizada = categoriaServico.atualizar(id, categoria);
        return ResponseEntity.ok(categoriaAtualizada);
    }

    // excluir categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaServico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}