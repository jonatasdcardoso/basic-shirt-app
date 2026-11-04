package com.grouploja.basic_shirt_app.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.grouploja.basic_shirt_app.service.CarrinhoServico;
import com.grouploja.basic_shirt_app.service.UsuarioServico;

@ControllerAdvice(annotations = Controller.class)
public class LayoutModelAdvice {

    private final UsuarioServico usuarioServico;
    private final CarrinhoServico carrinhoServico;

    public LayoutModelAdvice(
            UsuarioServico usuarioServico,
            CarrinhoServico carrinhoServico) {
        this.usuarioServico = usuarioServico;
        this.carrinhoServico = carrinhoServico;
    }

    @ModelAttribute("quantidadeItensCarrinho")
    public int quantidadeItensCarrinho(Authentication authentication) {
        if (!estaAutenticado(authentication)) {
            return 0;
        }

        return usuarioServico.buscarPorEmailOpcional(authentication.getName())
            .map(usuario -> carrinhoServico.contarUnidadesPorUsuarioId(usuario.getId()))
            .orElse(0);
    }

    @ModelAttribute("nomeUsuarioLogado")
    public String nomeUsuarioLogado(Authentication authentication) {
        if (!estaAutenticado(authentication)) {
            return null;
        }

        return usuarioServico.buscarPorEmailOpcional(authentication.getName())
            .map(usuario -> {
                String nome = usuario.getNome().trim();
                int espaco = nome.indexOf(' ');
                return espaco > 0 ? nome.substring(0, espaco) : nome;
            })
            .orElse(authentication.getName());
    }

    private boolean estaAutenticado(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
