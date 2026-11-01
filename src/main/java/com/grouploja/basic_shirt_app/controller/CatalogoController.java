package com.grouploja.basic_shirt_app.controller;

import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.service.CategoriaServico;
import com.grouploja.basic_shirt_app.service.ProdutoServico;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Controller
public class CatalogoController {

    private final ProdutoServico produtoServico;
    private final CategoriaServico categoriaServico;

    public CatalogoController(ProdutoServico produtoServico, CategoriaServico categoriaServico) {
        this.produtoServico = produtoServico;
        this.categoriaServico = categoriaServico;
    }

    // Pagina inicial manda direto pro catalogo
    @GetMapping("/")
    public String inicio() {
        return "redirect:/catalogo";
    }


    @GetMapping("/catalogo")
    public ModelAndView listar(@RequestParam(required = false) Long categoriaId) {

        List<Produto> todos = produtoServico.listarTodos();
        List<Produto> produtos = new ArrayList<>();

        for (Produto p : todos) {
            if (categoriaId == null || p.getCategoria().getId().equals(categoriaId)) {
                produtos.add(p);
            }
        }

        return new ModelAndView("catalogo/lista")
                .addObject("produtos", produtos)
                .addObject("categorias", categoriaServico.listarTodas())
                .addObject("categoriaSelecionada", categoriaId);
    }

    
    @GetMapping("/catalogo/{id}")
    public ModelAndView detalhe(@PathVariable Long id) {

        Optional<Produto> produto = produtoServico.buscarPorId(id);

       
        if (produto.isEmpty()) {
            return new ModelAndView("redirect:/catalogo");
        }

        return new ModelAndView("catalogo/detalhe")
                .addObject("produto", produto.get());
    }
}
