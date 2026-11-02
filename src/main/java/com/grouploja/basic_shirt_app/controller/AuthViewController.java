package com.grouploja.basic_shirt_app.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grouploja.basic_shirt_app.dto.CadastroUsuarioDTO;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.service.PedidoServico;
import com.grouploja.basic_shirt_app.service.UsuarioServico;

import jakarta.validation.Valid;

@Controller
public class AuthViewController {

    private final UsuarioServico usuarioServico;
    private final PedidoServico pedidoServico;

    public AuthViewController(UsuarioServico usuarioServico, PedidoServico pedidoServico) {
        this.usuarioServico = usuarioServico;
        this.pedidoServico = pedidoServico;
    }

    @GetMapping("/login")
    public String paginaLogin(Authentication authentication) {
        if (estaAutenticado(authentication)) {
            return "redirect:/catalogo";
        }
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String paginaCadastro(Authentication authentication, Model model) {
        if (estaAutenticado(authentication)) {
            return "redirect:/catalogo";
        }
        if (!model.containsAttribute("cadastroForm")) {
            model.addAttribute("cadastroForm", new CadastroUsuarioDTO());
        }
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(
            @Valid @ModelAttribute("cadastroForm") CadastroUsuarioDTO form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/cadastro";
        }

        try {
            usuarioServico.cadastrarCliente(
                form.getNome(),
                form.getEmail(),
                form.getSenha(),
                form.getConfirmarSenha()
            );
            redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Conta criada com sucesso! Faça login para começar suas compras."
            );
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            return "auth/cadastro";
        }
    }

    @GetMapping("/minha-conta")
    public String minhaConta(Authentication authentication, Model model) {
        Usuario usuario = usuarioServico.buscarPorEmail(authentication.getName());
        int totalPedidos = pedidoServico.listarPedidos(usuario.getId()).size();

        model.addAttribute("usuario", usuario);
        model.addAttribute("totalPedidos", totalPedidos);
        return "auth/minha-conta";
    }

    @PostMapping("/minha-conta")
    public String atualizarMinhaConta(
            Authentication authentication,
            @RequestParam String nome,
            @RequestParam(required = false) String senhaAtual,
            @RequestParam(required = false) String novaSenha,
            @RequestParam(required = false) String confirmarNovaSenha,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioServico.buscarPorEmail(authentication.getName());

        try {
            usuarioServico.atualizarPerfil(
                usuario.getId(),
                nome,
                senhaAtual,
                novaSenha,
                confirmarNovaSenha
            );
            redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Seus dados foram atualizados com sucesso."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }

        return "redirect:/minha-conta";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "auth/acesso-negado";
    }

    private boolean estaAutenticado(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
