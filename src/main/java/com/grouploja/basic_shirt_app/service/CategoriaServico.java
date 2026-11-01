
package com.grouploja.basic_shirt_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.grouploja.basic_shirt_app.entiny.Categoria;
import com.grouploja.basic_shirt_app.repository.CategoriaRepositorio;
import com.grouploja.basic_shirt_app.repository.ProdutoRepositorio;

//CRUD

@Service // Registra a classe no Spring Container
public class CategoriaServico {
    // Não pode ser modificada após a atribuicao
    private final CategoriaRepositorio categoriaRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    //Construtor
    public CategoriaServico(
            CategoriaRepositorio categoriaRepositorio,
            ProdutoRepositorio produtoRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.produtoRepositorio = produtoRepositorio;
    }

    // Validar categoria
    private void validarCategoria(Categoria categoria) {

        if (categoria == null || categoria.getNome() == null ||
            categoria.getNome().isBlank()) {

            throw new IllegalArgumentException(
                "O nome da categoria é obrigatório.");
        }
    }

    //Salvar
    public Categoria salvar(Categoria categoria) {
        validarCategoria(categoria);

        if (categoriaRepositorio.existsByNomeIgnoreCase(categoria.getNome())) {
            throw new IllegalArgumentException(
                "Já existe uma categoria com esse nome.");
        }

        return categoriaRepositorio.save(categoria);
    }

    //Listar
    public List<Categoria> listarTodas(){
        return categoriaRepositorio.findAll();
    }

    // Procurar por id (Opcional pra caso nao tenha Categoria no Id)
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepositorio.findById(id);
    }

    //Atualizar
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

    //Deletar
    public void excluir(Long id) {
        if (!categoriaRepositorio.existsById(id)) {
            throw new CategoriaNaoEncontradaException(id);
        }

        // Impede excluir categoria que possui produtos associados
        if (produtoRepositorio.existsByCategoriaId(id)) {
            throw new IllegalArgumentException(
                "Não é possível excluir uma categoria que possui produtos.");
        }

        categoriaRepositorio.deleteById(id);
    }
}
