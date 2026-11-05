package com.grouploja.basic_shirt_app.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.entiny.Categoria;
import com.grouploja.basic_shirt_app.repository.CategoriaRepositorio;
import com.grouploja.basic_shirt_app.repository.ProdutoRepositorio;

// CRUD
@Service
public class CategoriaServico {

    private final CategoriaRepositorio categoriaRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    public CategoriaServico(
            CategoriaRepositorio categoriaRepositorio,
            ProdutoRepositorio produtoRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.produtoRepositorio = produtoRepositorio;
    }

    // Normalizar nome da categoria
    private String normalizarNome(String nome) {
        if (nome == null) {
            return null;
        }

        return nome.trim();
    }

    // Validar categoria
    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException(
                "Os dados da categoria são obrigatórios.");
        }

        categoria.setNome(normalizarNome(categoria.getNome()));

        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new IllegalArgumentException(
                "O nome da categoria é obrigatório.");
        }
    }

    // Salvar
    @Transactional
    public Categoria salvar(Categoria categoria) {
        validarCategoria(categoria);

        if (categoriaRepositorio.existsByNomeIgnoreCase(categoria.getNome())) {
            throw new IllegalArgumentException(
                "Já existe uma categoria com esse nome.");
        }

        return categoriaRepositorio.save(categoria);
    }

    // Listar todas
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepositorio.findAll();
    }

    // Buscar por ID
    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepositorio.findById(id);
    }

    // Atualizar
    @Transactional
    public Categoria atualizar(Long id, Categoria categoria) {

        Categoria categoriaExistente = categoriaRepositorio.findById(id)
            .orElseThrow(() -> new CategoriaNaoEncontradaException(id));

        validarCategoria(categoria);

        boolean nomeDuplicado = categoriaRepositorio
            .existsByNomeIgnoreCase(categoria.getNome());

        if (nomeDuplicado &&
            !categoriaExistente.getNome().equalsIgnoreCase(categoria.getNome())) {

            throw new IllegalArgumentException(
                "Já existe uma categoria com esse nome.");
        }

        categoriaExistente.setNome(categoria.getNome());

        return categoriaRepositorio.save(categoriaExistente);
    }

    // Excluir
    @Transactional
    public void excluir(Long id) {

        if (!categoriaRepositorio.existsById(id)) {
            throw new CategoriaNaoEncontradaException(id);
        }

        if (produtoRepositorio.existsByCategoriaId(id)) {
            throw new IllegalArgumentException(
                "Não é possível excluir uma categoria que possui produtos.");
        }

        categoriaRepositorio.deleteById(id);
    }
}
