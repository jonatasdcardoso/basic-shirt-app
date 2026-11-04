
package com.grouploja.basic_shirt_app.service;

import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.repository.ProdutoRepositorio;
import com.grouploja.basic_shirt_app.repository.CategoriaRepositorio;
import com.grouploja.basic_shirt_app.repository.PedidoRepositorio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServico {

    private final ProdutoRepositorio produtoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    public ProdutoServico(
            ProdutoRepositorio produtoRepositorio,
            CategoriaRepositorio categoriaRepositorio,
            PedidoRepositorio pedidoRepositorio) {

        this.produtoRepositorio = produtoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    //Salvar
    @Transactional
    public Produto salvar(Produto produto) {
        validarProduto(produto);
        produto.setNome(produto.getNome().trim());
        produto.setAtivo(true);
        return produtoRepositorio.save(produto);
    }

    //Listar
    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepositorio.findByAtivoTrue();
    }

    //Buscar por ID
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepositorio.findByIdAndAtivoTrue(id);
    }

    //Atualizar
    @Transactional
    public Produto atualizar(Long id, Produto produto) {

        Produto produtoExistente = produtoRepositorio.findByIdAndAtivoTrue(id)
            .orElseThrow(() -> new ProdutoNaoEncontradoException(id));

        validarProduto(produto);

        produtoExistente.setNome(produto.getNome().trim());
        produtoExistente.setDescricao(produto.getDescricao());
        produtoExistente.setPreco(produto.getPreco());
        produtoExistente.setEstoque(produto.getEstoque());
        produtoExistente.setCategoria(produto.getCategoria());

        return produtoRepositorio.save(produtoExistente);
    }

    //Deletar (exclusão lógica)
    @Transactional
    public void excluir(Long id) {

        Produto produto = produtoRepositorio.findByIdAndAtivoTrue(id)
            .orElseThrow(() -> new ProdutoNaoEncontradoException(id));

        // Desativa o produto sem apagar seu histórico
        produto.setAtivo(false);

        produtoRepositorio.save(produto);
    }

    //Validar produto
    private void validarProduto(Produto produto) {

        if (produto == null) {
            throw new IllegalArgumentException(
                "Os dados do produto são obrigatórios."
            );
        }

        // Validar nome
        if (produto.getNome() == null ||
            produto.getNome().isBlank()) {

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
        if (produto.getEstoque() == null) {
            throw new IllegalArgumentException(
                "O estoque do produto é obrigatório."
            );
        }

        if (produto.getEstoque() < 0) {
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

        if (produto.getCategoria().getId() == null ||
            !categoriaRepositorio.existsById(
                produto.getCategoria().getId())) {

            throw new IllegalArgumentException(
                "A categoria informada não existe."
            );
        }
    }
}




