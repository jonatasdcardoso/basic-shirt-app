
package com.grouploja.basic_shirt_app.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.repository.UsuarioRepositorio;
import com.grouploja.basic_shirt_app.repository.PedidoRepositorio;

@Service
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    public UsuarioServico(
            UsuarioRepositorio usuarioRepositorio,
            PedidoRepositorio pedidoRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    // Normalizar dados do usuário
    private void normalizarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                "Os dados do usuário são obrigatórios.");
        }

        if (usuario.getNome() != null) {
            usuario.setNome(usuario.getNome().trim());
        }

        if (usuario.getEmail() != null) {
            usuario.setEmail(
                usuario.getEmail().trim().toLowerCase(Locale.ROOT));
        }
    }

    // Validar usuário
    private void validarUsuario(Usuario usuario) {
        if (usuario == null)
            throw new IllegalArgumentException(
                "Os dados do usuário são obrigatórios.");

        if (usuario.getNome() == null || usuario.getNome().isBlank())
            throw new IllegalArgumentException(
                "O nome do usuário é obrigatório.");

        if (usuario.getEmail() == null || usuario.getEmail().isBlank())
            throw new IllegalArgumentException(
                "O e-mail do usuário é obrigatório.");

        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
            throw new IllegalArgumentException(
                "O e-mail informado é inválido.");

        if (usuario.getSenha() == null || usuario.getSenha().isBlank())
            throw new IllegalArgumentException(
                "A senha do usuário é obrigatória.");
    }

    // Cadastrar usuário
    @Transactional
    public Usuario salvar(Usuario usuario) {
        normalizarUsuario(usuario);
        validarUsuario(usuario);

        if (usuarioRepositorio.existsByEmailIgnoreCase(usuario.getEmail()))
            throw new IllegalArgumentException(
                "Já existe um usuário cadastrado com esse e-mail.");

        return usuarioRepositorio.save(usuario);
    }

    // Listar usuários
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    // Buscar usuário por ID
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepositorio.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Usuário não encontrado."));
    }

    // Atualizar usuário
    @Transactional
    public Usuario atualizar(Long id, Usuario usuario) {
        Usuario usuarioExistente = buscarPorId(id);

        normalizarUsuario(usuario);
        validarUsuario(usuario);

        boolean emailDuplicado =
            usuarioRepositorio.existsByEmailIgnoreCase(usuario.getEmail());

        if (emailDuplicado &&
            !usuarioExistente.getEmail().trim()
                .equalsIgnoreCase(usuario.getEmail())) {
            throw new IllegalArgumentException(
                "Já existe um usuário cadastrado com esse e-mail.");
        }

        usuarioExistente.setNome(usuario.getNome());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setSenha(usuario.getSenha());

        return usuarioRepositorio.save(usuarioExistente);
    }

    // Excluir usuário
    @Transactional
    public void excluir(Long id) {

        if (!usuarioRepositorio.existsById(id))
            throw new IllegalArgumentException(
                "Usuário não encontrado.");

        // Impede excluir usuário que possui pedidos registrados
        if (pedidoRepositorio.existsByUsuarioId(id))
            throw new IllegalArgumentException(
                "Não é possível excluir um usuário que possui pedidos registrados.");

        usuarioRepositorio.deleteById(id);
    }
}
