package com.grouploja.basic_shirt_app.service;

import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.repository.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioServico(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        // Usa o método declarado no seu UsuarioRepositorio
        if (usuarioRepositorio.existsByEmailIgnoreCase(usuario.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }
        return usuarioRepositorio.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com o ID: " + id));
    }
}