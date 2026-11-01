package com.grouploja.basic_shirt_app.service;

import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.repository.ProdutoRepositorio;
import org.springframework.stereotype.Service;
import com.grouploja.basic_shirt_app.repository.CategoriaRepositorio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service 
public class ProdutoServico {

    private final ProdutoRepositorio produtoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;

    public ProdutoServico(ProdutoRepositorio produtoRepositorio, CategoriaRepositorio categoriaRepositorio){
        this.produtoRepositorio = produtoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
    }
    public Produto salvar(Produto produto){
        validarProduto(produto);
        return produtoRepositorio.save(produto);
    }
    public List<Produto> listarTodos() {
        return produtoRepositorio.findAll();
    }
    public Optional<Produto> buscarPorId(Long id){
        return produtoRepositorio.findById(id);
    }
    public Produto atualizar(Long id, Produto produto){
        Produto produtoExistente = produtoRepositorio.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException(id));
        validarProduto(produto);
        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricao(produto.getDescricao());
        produtoExistente.setPreco(produto.getPreco());
        produtoExistente.setEstoque(produto.getEstoque());
        produtoExistente.setCategoria(produto.getCategoria());

        return produtoRepositorio.save(produtoExistente);
    }
    public void excluir(Long id) {
        if(!produtoRepositorio.existsById(id)) {
            throw new ProdutoNaoEncontradoException(id);
        }

        produtoRepositorio.deleteById(id);
    }

    private void validarProduto(Produto produto) {

    // Validar nome
    if (produto.getNome() == null || produto.getNome().isBlank()) {
        throw new IllegalArgumentException(
            "O nome do produto é obrigatório."
        );
    }

    // Validar preço
    if (produto.getPreco() == null) {
        throw new IllegalArgumentException(
            "O preço do produto é obrigatório."
        );
    }

    if (produto.getPreco().compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException(
            "O preço do produto não pode ser negativo."
        );
    }

    // Validar estoque
    if (produto.getEstoque() != null && produto.getEstoque() < 0) {
        throw new IllegalArgumentException(
            "O estoque do produto não pode ser negativo."
        );
    }

    // Validar categoria
    if (produto.getCategoria() == null) {
        throw new IllegalArgumentException(
            "A categoria do produto é obrigatória."
        );
    }

    if (!categoriaRepositorio.existsById(produto.getCategoria().getId())) {
    throw new IllegalArgumentException(
        "A categoria informada não existe."
    );
    }
}

}

