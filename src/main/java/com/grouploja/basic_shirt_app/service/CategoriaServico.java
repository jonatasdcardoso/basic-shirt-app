package com.grouploja.basic_shirt_app.service;

import com.grouploja.basic_shirt_app.entiny.Categoria;
import com.grouploja.basic_shirt_app.repository.CategoriaRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//CRUD

@Service // Registra a classe no Spring Container
public class CategoriaServico {
    // Não pode ser modificada após a atribuicao
    private final CategoriaRepositorio categoriaRepositorio;
    //Construtor
    public CategoriaServico(CategoriaRepositorio categoriaRespositorio) {
        this.categoriaRepositorio = categoriaRespositorio;
    }
    //Salvar
    public Categoria salvar(Categoria categoria) {
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
    public Categoria atualizar(Long id, Categoria categoria){
        Categoria categoriaExistente = categoriaRepositorio.findById(id).orElseThrow(() -> new CategoriaNaoEncontradaException(id));
        categoriaExistente.setNome(categoria.getNome());
        return categoriaRepositorio.save(categoriaExistente);
    }
    //Deletar
    public void excluir(Long id) {
        if(!categoriaRepositorio.existsById(id)) {
            throw new CategoriaNaoEncontradaException(id);
        }
        categoriaRepositorio.deleteById(id);
    }
}