package com.grouploja.basic_shirt_app.service;

import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.repository.ProdutoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service 
public class ProdutoServico {

    private final ProdutoRepositorio produtoRepositorio;

    public ProdutoServico(ProdutoRepositorio produtoRepositorio){
        this.produtoRepositorio = produtoRepositorio;
    }
    public Produto salvar(Produto produto){
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

}

