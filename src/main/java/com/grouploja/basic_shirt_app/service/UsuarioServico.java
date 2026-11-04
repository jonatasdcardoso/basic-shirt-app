package com.grouploja.basic_shirt_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.entiny.PerfilUsuario;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.repository.UsuarioRepositorio;

@Service
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServico(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrarCliente(String nome, String email, String senha, String confirmarSenha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }

        if (!senha.equals(confirmarSenha)) {
            throw new IllegalArgumentException("A confirmação de senha não confere.");
        }

        Usuario novoUsuario = new Usuario(nome, email, senha, PerfilUsuario.CLIENTE);
        return salvar(novoUsuario);
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Os dados do usuário são obrigatórios.");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank() || !usuario.getEmail().contains("@")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }

        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }

        String nomeLimpo = usuario.getNome().trim();
        String emailLimpo = usuario.getEmail().trim().toLowerCase();

        if (usuarioRepositorio.existsByEmailIgnoreCase(emailLimpo)) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }

        usuario.setNome(nomeLimpo);
        usuario.setEmail(emailLimpo);

        if (!isHashBcrypt(usuario.getSenha())) {
            if (usuario.getSenha().length() < 6) {
                throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
            }
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        if (usuario.getPerfil() == null) {
            usuario.setPerfil(PerfilUsuario.CLIENTE);
        }

        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public Usuario atualizarPerfil(
            Long usuarioId,
            String nome,
            String senhaAtual,
            String novaSenha,
            String confirmarNovaSenha) {

        Usuario usuario = buscarPorId(usuarioId);

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ficar em branco.");
        }

        usuario.setNome(nome.trim());

        if (novaSenha != null && !novaSenha.isBlank()) {
            if (senhaAtual == null || !passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
                throw new IllegalArgumentException("A senha atual informada está incorreta.");
            }
            if (novaSenha.length() < 6) {
                throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
            }
            if (!novaSenha.equals(confirmarNovaSenha)) {
                throw new IllegalArgumentException("A confirmação da nova senha não confere.");
            }
            usuario.setSenha(passwordEncoder.encode(novaSenha));
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

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return buscarPorEmailOpcional(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para o e-mail: " + email));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmailOpcional(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepositorio.findByEmailIgnoreCase(email.trim());
    }

    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        return email != null && usuarioRepositorio.existsByEmailIgnoreCase(email.trim());
    }

    private boolean isHashBcrypt(String valor) {
        return valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$");
    }
}
