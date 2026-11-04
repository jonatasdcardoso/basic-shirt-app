package com.grouploja.basic_shirt_app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grouploja.basic_shirt_app.entiny.PerfilUsuario;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.service.UsuarioServico;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioServico usuarioServico;

    public UsuarioController(UsuarioServico usuarioServico) {
        this.usuarioServico = usuarioServico;
    }

    // Criar usuário
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(
            @Valid @RequestBody CriarUsuarioRequest request) {

        Usuario novoUsuario = new Usuario(
            request.nome(),
            request.email(),
            request.senha()
        );

        Usuario usuarioSalvo = usuarioServico.salvar(novoUsuario);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UsuarioResponse.converter(usuarioSalvo));
    }

    // Listar todos os usuários
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {

        List<UsuarioResponse> usuarios = usuarioServico.listarTodos()
            .stream()
            .map(UsuarioResponse::converter)
            .toList();

        return ResponseEntity.ok(usuarios);
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable Long id) {

        Usuario usuario = usuarioServico.buscarPorId(id);

        return ResponseEntity.ok(
            UsuarioResponse.converter(usuario));
    }

    // DTO de requisição
    public record CriarUsuarioRequest(

        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
        String senha

    ) {
    }

    // DTO de resposta
    public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil
    ) {

        public static UsuarioResponse converter(Usuario usuario) {

            return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil()
            );
        }
    }
}