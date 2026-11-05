package com.grouploja.basic_shirt_app.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grouploja.basic_shirt_app.dto.ItemCarrinhoViewDTO;
import com.grouploja.basic_shirt_app.entiny.FormaPagamento;
import com.grouploja.basic_shirt_app.entiny.Pedido;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.service.CarrinhoServico;
import com.grouploja.basic_shirt_app.service.PedidoServico;
import com.grouploja.basic_shirt_app.service.ProdutoNaoEncontradoException;
import com.grouploja.basic_shirt_app.service.UsuarioServico;

@Controller
public class CompraViewController {

    private final CarrinhoServico carrinhoServico;
    private final PedidoServico pedidoServico;
    private final UsuarioServico usuarioServico;

    public CompraViewController(
            CarrinhoServico carrinhoServico,
            PedidoServico pedidoServico,
            UsuarioServico usuarioServico) {
        this.carrinhoServico = carrinhoServico;
        this.pedidoServico = pedidoServico;
        this.usuarioServico = usuarioServico;
    }

    @GetMapping("/carrinho")
    public String verCarrinho(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioAutenticado(authentication);
        List<ItemCarrinhoViewDTO> itens = carrinhoServico.listarItensParaExibicao(usuario.getId());
        BigDecimal subtotal = carrinhoServico.calcularTotal(usuario.getId());

        int totalUnidades = itens.stream()
            .mapToInt(ItemCarrinhoViewDTO::quantidade)
            .sum();

        model.addAttribute("itens", itens);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("totalUnidades", totalUnidades);
        return "compra/carrinho";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionarAoCarrinho(
            Authentication authentication,
            @RequestParam Long produtoId,
            @RequestParam(defaultValue = "1") Integer quantidade,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioAutenticado(authentication);

        try {
            carrinhoServico.adicionarProduto(usuario.getId(), produtoId, quantidade);
            redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Produto adicionado ao seu carrinho!"
            );
            return "redirect:/carrinho";
        } catch (ProdutoNaoEncontradoException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/catalogo";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/catalogo/" + produtoId;
        }
    }

    @PostMapping("/carrinho/itens/{itemId}/quantidade")
    public String atualizarQuantidade(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestParam Integer quantidade,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioAutenticado(authentication);

        try {
            if (quantidade != null && quantidade == 0) {
                carrinhoServico.removerItem(usuario.getId(), itemId);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Item removido do carrinho.");
            } else {
                carrinhoServico.alterarQuantidade(usuario.getId(), itemId, quantidade);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Quantidade atualizada.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }

        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/itens/{itemId}/remover")
    public String removerDoCarrinho(
            Authentication authentication,
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioAutenticado(authentication);

        try {
            carrinhoServico.removerItem(usuario.getId(), itemId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Item removido do carrinho.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }

        return "redirect:/carrinho";
    }

    @GetMapping("/checkout")
    public String paginaCheckout(
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioAutenticado(authentication);
        List<ItemCarrinhoViewDTO> itens = carrinhoServico.listarItensParaExibicao(usuario.getId());

        if (itens.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                "mensagemErro",
                "Seu carrinho está vazio. Adicione produtos antes de finalizar a compra."
            );
            return "redirect:/carrinho";
        }

        BigDecimal subtotal = carrinhoServico.calcularTotal(usuario.getId());
        int totalUnidades = itens.stream()
            .mapToInt(ItemCarrinhoViewDTO::quantidade)
            .sum();

        model.addAttribute("usuario", usuario);
        model.addAttribute("itens", itens);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("totalUnidades", totalUnidades);
        model.addAttribute("formasPagamento", FormaPagamento.values());
        return "compra/checkout";
    }

    @PostMapping("/checkout/finalizar")
    public String finalizarCompra(
            Authentication authentication,
            @RequestParam(required = false) FormaPagamento formaPagamento,
            @RequestParam(required = false) String cep,
            @RequestParam(required = false) String endereco,
            @RequestParam(required = false) String cidade,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioAutenticado(authentication);

        try {
            Pedido pedido = pedidoServico.finalizarCompra(usuario.getId(), formaPagamento);
            redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Compra finalizada com sucesso! Seu pedido #" + pedido.getId() + " foi confirmado."
            );
            if (endereco != null && !endereco.isBlank()) {
                String resumoEntrega = endereco.trim()
                    + (cidade != null && !cidade.isBlank() ? " - " + cidade.trim() : "")
                    + (cep != null && !cep.isBlank() ? " (CEP " + cep.trim() + ")" : "");
                redirectAttributes.addFlashAttribute("enderecoConfirmado", resumoEntrega);
            }
            return "redirect:/pedidos/" + pedido.getId();
        } catch (ProdutoNaoEncontradoException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/pedidos")
    public String listarMeusPedidos(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioAutenticado(authentication);
        List<Pedido> pedidos = pedidoServico.listarPedidos(usuario.getId());

        model.addAttribute("pedidos", pedidos);
        return "compra/pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String detalhePedido(
            Authentication authentication,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioAutenticado(authentication);

        try {
            Pedido pedido = pedidoServico.buscarPorId(id, usuario.getId());
            model.addAttribute("pedido", pedido);
            return "compra/pedido-detalhe";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/pedidos";
        }
    }

    private Usuario obterUsuarioAutenticado(Authentication authentication) {
        return usuarioServico.buscarPorEmail(authentication.getName());
    }
}
