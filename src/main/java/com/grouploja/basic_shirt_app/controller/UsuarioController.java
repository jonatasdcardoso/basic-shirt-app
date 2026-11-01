package com.grouploja.basic_shirt_app.controller;

import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.service.UsuarioServico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioServico usuarioServico;

    public UsuarioController(UsuarioServico usuarioServico) {
        this.usuarioServico = usuarioServico;
    }

    //Criar usuário
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody CriarUsuarioRequest request) {
        Usuario novoUsuario = new Usuario(request.nome(), request.email(), request.senha());
        Usuario usuarioSalvo = usuarioServico.salvar(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.converter(usuarioSalvo));
    }

    //Listar todos usuários
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        List<UsuarioResponse> usuarios = usuarioServico.listarTodos().stream()
                .map(UsuarioResponse::converter)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    //Buscar usuário usando ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioServico.buscarPorId(id);
        return ResponseEntity.ok(UsuarioResponse.converter(usuario));
    }

    //DTO de requisição
    public record CriarUsuarioRequest(
        String nome,
        String email,
        String senha
    ) {}

    //DTO de resposta
    public record UsuarioResponse(
        Long id,
        String nome,
        String email
    ) {
        public static UsuarioResponse converter(Usuario usuario) {
            return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
            );
        }
    }
}