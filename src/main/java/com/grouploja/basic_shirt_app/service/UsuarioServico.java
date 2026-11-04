package com.grouploja.basic_shirt_app.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.entiny.PerfilUsuario;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.repository.PedidoRepositorio;
import com.grouploja.basic_shirt_app.repository.UsuarioRepositorio;

@Service
@Transactional
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServico(
            UsuarioRepositorio usuarioRepositorio,
            PedidoRepositorio pedidoRepositorio,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepositorio = usuarioRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    private void normalizarUsuario(Usuario usuario) {
        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase(Locale.ROOT));
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não informado.");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (usuario.getEmail() == null
                || !usuario.getEmail().trim()
                        .matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }

        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }

        if (usuario.getSenha().length() < 6) {
            throw new IllegalArgumentException(
                    "A senha deve possuir pelo menos 6 caracteres.");
        }
    }

    // Cadastra um novo cliente com confirmação de senha.
    public Usuario cadastrarCliente(
            String nome,
            String email,
            String senha,
            String confirmarSenha) {

        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException(
                    "A senha deve possuir pelo menos 6 caracteres.");
        }

        if (!senha.equals(confirmarSenha)) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }

        Usuario novoUsuario = new Usuario(
                nome,
                email,
                senha,
                PerfilUsuario.CLIENTE);

        return salvar(novoUsuario);
    }

    // Salva um usuário com senha criptografada.
    public Usuario salvar(Usuario usuario) {
        validarUsuario(usuario);
        normalizarUsuario(usuario);

        if (usuarioRepositorio.existsByEmailIgnoreCase(usuario.getEmail())) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com este e-mail.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        if (usuario.getPerfil() == null) {
            usuario.setPerfil(PerfilUsuario.CLIENTE);
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
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."));
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
        return email != null
                && usuarioRepositorio.existsByEmailIgnoreCase(email.trim());
    }

    // Atualiza os dados cadastrais de um usuário.
    public Usuario atualizar(Long id, Usuario dadosAtualizados) {
        Usuario usuarioExistente = buscarPorId(id);

        if (dadosAtualizados == null) {
            throw new IllegalArgumentException(
                    "Os dados do usuário não foram informados.");
        }

        if (dadosAtualizados.getNome() == null
                || dadosAtualizados.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (dadosAtualizados.getEmail() == null
                || !dadosAtualizados.getEmail().trim()
                        .matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }

        String novoEmail = dadosAtualizados.getEmail()
                .trim().toLowerCase(Locale.ROOT);

        boolean emailEmUso = usuarioRepositorio
                .findByEmailIgnoreCase(novoEmail)
                .filter(usuario -> !usuario.getId().equals(id))
                .isPresent();

        if (emailEmUso) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com este e-mail.");
        }

        usuarioExistente.setNome(dadosAtualizados.getNome().trim());
        usuarioExistente.setEmail(novoEmail);

        if (dadosAtualizados.getSenha() != null
                && !dadosAtualizados.getSenha().isBlank()) {

            if (dadosAtualizados.getSenha().length() < 6) {
                throw new IllegalArgumentException(
                        "A senha deve possuir pelo menos 6 caracteres.");
            }

            usuarioExistente.setSenha(
                    passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        return usuarioRepositorio.save(usuarioExistente);
    }

    // Atualiza o perfil e permite trocar a senha mediante confirmação.
    public Usuario atualizarPerfil(
            Long usuarioId,
            String nome,
            String senhaAtual,
            String novaSenha,
            String confirmarNovaSenha) {

        Usuario usuario = buscarPorId(usuarioId);

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        usuario.setNome(nome.trim());

        if (novaSenha != null && !novaSenha.isBlank()) {

            if (senhaAtual == null || senhaAtual.isBlank()
                    || !passwordEncoder.matches(
                            senhaAtual, usuario.getSenha())) {
                throw new IllegalArgumentException("Senha atual incorreta.");
            }

            if (novaSenha.length() < 6) {
                throw new IllegalArgumentException(
                        "A nova senha deve possuir pelo menos 6 caracteres.");
            }

            if (!novaSenha.equals(confirmarNovaSenha)) {
                throw new IllegalArgumentException(
                        "A confirmação da nova senha não coincide.");
            }

            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        return usuarioRepositorio.save(usuario);
    }

    // Exclui o usuário somente se ele não possuir pedidos.
    public void excluir(Long id) {
        Usuario usuario = buscarPorId(id);

        if (pedidoRepositorio.existsByUsuarioId(id)) {
            throw new IllegalArgumentException(
                    "Não é possível excluir um usuário que possui pedidos.");
        }

        usuarioRepositorio.delete(usuario);
    }
}