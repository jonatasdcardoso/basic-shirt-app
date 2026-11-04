package com.grouploja.basic_shirt_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.grouploja.basic_shirt_app.entiny.PerfilUsuario;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.repository.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class UsuarioServicoTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    private PasswordEncoder passwordEncoder;
    private UsuarioServico usuarioServico;

    @BeforeEach
    void configurar() {
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioServico = new UsuarioServico(usuarioRepositorio, passwordEncoder);
    }

    @Test
    void deveCadastrarClienteComSenhaCriptografadaEPerfilCliente() {
        when(usuarioRepositorio.existsByEmailIgnoreCase("maria@teste.com")).thenReturn(false);
        when(usuarioRepositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario salvo = usuarioServico.cadastrarCliente(
            "  Maria Silva  ",
            "  MARIA@teste.com ",
            "senhaSegura123",
            "senhaSegura123"
        );

        assertEquals("Maria Silva", salvo.getNome());
        assertEquals("maria@teste.com", salvo.getEmail());
        assertEquals(PerfilUsuario.CLIENTE, salvo.getPerfil());
        assertNotEquals("senhaSegura123", salvo.getSenha());
        assertTrue(passwordEncoder.matches("senhaSegura123", salvo.getSenha()));
    }

    @Test
    void deveRejeitarCadastroQuandoConfirmacaoDeSenhaNaoConfere() {
        IllegalArgumentException erro = assertThrows(
            IllegalArgumentException.class,
            () -> usuarioServico.cadastrarCliente("João", "joao@teste.com", "123456", "654321")
        );

        assertTrue(erro.getMessage().contains("confirmação"));
        verify(usuarioRepositorio, never()).save(any());
    }

    @Test
    void deveRejeitarCadastroComEmailJaExistente() {
        when(usuarioRepositorio.existsByEmailIgnoreCase("duplicado@teste.com")).thenReturn(true);

        assertThrows(
            IllegalArgumentException.class,
            () -> usuarioServico.cadastrarCliente("Ana", "duplicado@teste.com", "123456", "123456")
        );

        verify(usuarioRepositorio, never()).save(any());
    }

    @Test
    void deveAtualizarPerfilESenhaQuandoSenhaAtualEstiverCorreta() {
        Usuario existente = new Usuario(
            "Carlos",
            "carlos@teste.com",
            passwordEncoder.encode("senhaAntiga"),
            PerfilUsuario.CLIENTE
        );

        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario atualizado = usuarioServico.atualizarPerfil(
            1L,
            "Carlos Eduardo",
            "senhaAntiga",
            "novaSenha123",
            "novaSenha123"
        );

        assertEquals("Carlos Eduardo", atualizado.getNome());
        assertTrue(passwordEncoder.matches("novaSenha123", atualizado.getSenha()));
    }
}
