package com.grouploja.basic_shirt_app.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grouploja.basic_shirt_app.entiny.Categoria;
import com.grouploja.basic_shirt_app.entiny.Pedido;
import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.service.CategoriaNaoEncontradaException;
import com.grouploja.basic_shirt_app.service.CategoriaServico;
import com.grouploja.basic_shirt_app.service.PedidoServico;
import com.grouploja.basic_shirt_app.service.ProdutoNaoEncontradoException;
import com.grouploja.basic_shirt_app.service.ProdutoServico;
import com.grouploja.basic_shirt_app.service.UsuarioServico;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    private final PedidoServico pedidoServico;
    private final ProdutoServico produtoServico;
    private final CategoriaServico categoriaServico;
    private final UsuarioServico usuarioServico;

    public AdminViewController(
            PedidoServico pedidoServico,
            ProdutoServico produtoServico,
            CategoriaServico categoriaServico,
            UsuarioServico usuarioServico) {
        this.pedidoServico = pedidoServico;
        this.produtoServico = produtoServico;
        this.categoriaServico = categoriaServico;
        this.usuarioServico = usuarioServico;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Pedido> pedidos = pedidoServico.listarTodos();
        List<Produto> produtos = produtoServico.listarTodos();
        List<Categoria> categorias = categoriaServico.listarTodas();
        List<Usuario> usuarios = usuarioServico.listarTodos();

        BigDecimal receitaTotal = pedidos.stream()
            .map(Pedido::getValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedio = pedidos.isEmpty()
            ? BigDecimal.ZERO
            : receitaTotal.divide(BigDecimal.valueOf(pedidos.size()), 2, RoundingMode.HALF_UP);

        model.addAttribute("receitaTotal", receitaTotal);
        model.addAttribute("totalPedidos", pedidos.size());
        model.addAttribute("totalClientes", usuarios.size());
        model.addAttribute("ticketMedio", ticketMedio);
        model.addAttribute("pedidosRecentes", pedidos);
        model.addAttribute("produtos", produtos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("usuarios", usuarios);
        return "admin/dashboard";
    }

    @PostMapping("/categorias")
    public String criarCategoria(
            @RequestParam String nome,
            RedirectAttributes redirectAttributes) {
        try {
            categoriaServico.salvar(new Categoria(nome));
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria cadastrada com sucesso.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/categorias/{id}/excluir")
    public String excluirCategoria(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            categoriaServico.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria excluída com sucesso.");
        } catch (CategoriaNaoEncontradaException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/produtos")
    public String criarProduto(
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam BigDecimal preco,
            @RequestParam Integer estoque,
            @RequestParam Long categoriaId,
            RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaServico.buscarPorId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Selecione uma categoria válida."));
            produtoServico.salvar(new Produto(nome, descricao, preco, estoque, categoria));
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto cadastrado com sucesso.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/produtos/{id}/estoque")
    public String atualizarEstoque(
            @PathVariable Long id,
            @RequestParam Integer estoque,
            RedirectAttributes redirectAttributes) {
        try {
            Produto existente = produtoServico.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
            Produto atualizado = new Produto(
                existente.getNome(),
                existente.getDescricao(),
                existente.getPreco(),
                estoque,
                existente.getCategoria()
            );
            produtoServico.atualizar(id, atualizado);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Estoque atualizado com sucesso.");
        } catch (ProdutoNaoEncontradoException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/produtos/{id}/excluir")
    public String excluirProduto(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            produtoServico.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto excluído com sucesso.");
        } catch (ProdutoNaoEncontradoException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                "mensagemErro",
                "Não é possível excluir um produto que já está vinculado a pedidos ou carrinhos."
            );
        }
        return "redirect:/admin";
    }
}
