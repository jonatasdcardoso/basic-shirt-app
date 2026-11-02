package com.grouploja.basic_shirt_app.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.grouploja.basic_shirt_app.entiny.Carrinho;
import com.grouploja.basic_shirt_app.entiny.Categoria;
import com.grouploja.basic_shirt_app.entiny.PerfilUsuario;
import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.service.CarrinhoServico;
import com.grouploja.basic_shirt_app.service.CategoriaServico;
import com.grouploja.basic_shirt_app.service.ProdutoServico;
import com.grouploja.basic_shirt_app.service.UsuarioServico;

@SpringBootTest
@Transactional
class SegurancaEFluxoWebIntegracaoTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoriaServico categoriaServico;

    @Autowired
    private ProdutoServico produtoServico;

    @Autowired
    private UsuarioServico usuarioServico;

    @Autowired
    private CarrinhoServico carrinhoServico;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private Produto produtoDisponivel;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        Categoria categoria = categoriaServico.salvar(new Categoria("Essenciais Web"));
        produtoDisponivel = produtoServico.salvar(new Produto(
            "Camiseta Oxford Minimal",
            "Tecido 100% algodão penteado",
            new BigDecimal("149.90"),
            10,
            categoria
        ));
    }

    @Test
    void rotasPublicasDoCatalogoELoginDevemFuncionarSemAutenticacao() throws Exception {
        mockMvc.perform(get("/catalogo"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Camiseta Oxford Minimal")))
            .andExpect(content().string(containsString("Ver Produto")));

        mockMvc.perform(get("/catalogo/" + produtoDisponivel.getId()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Adicionar ao Carrinho")));

        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Entrar na Conta")));

        mockMvc.perform(get("/cadastro"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cadastrar Usuário")));

        mockMvc.perform(get("/acesso-negado"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Acesso Restrito")));

        mockMvc.perform(get("/api/produtos"))
            .andExpect(status().isOk());
    }

    @Test
    void rotasProtegidasDeCompraEAdminDevemExigirLogin() throws Exception {
        mockMvc.perform(get("/carrinho"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/checkout"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/pedidos"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/minha-conta"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/admin"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void formulariosWebDevemRejeitarRequisicoesPostSemTokenCsrf() throws Exception {
        mockMvc.perform(post("/cadastro")
                .param("nome", "Tentativa Sem CSRF")
                .param("email", "semcsrf@teste.com")
                .param("senha", "123456")
                .param("confirmarSenha", "123456"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/carrinho/adicionar")
                .with(user("cliente@teste.com").roles("USER"))
                .param("produtoId", produtoDisponivel.getId().toString())
                .param("quantidade", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void deveCadastrarNovoUsuarioPelaTelaAutenticarERejeitarSenhaIncorreta() throws Exception {
        mockMvc.perform(post("/cadastro")
                .with(csrf())
                .param("nome", "Beatriz Costa")
                .param("email", "beatriz@teste.com")
                .param("senha", "minhaSenha123")
                .param("confirmarSenha", "minhaSenha123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        Usuario cadastrado = usuarioServico.buscarPorEmail("beatriz@teste.com");
        assertTrue(passwordEncoder.matches("minhaSenha123", cadastrado.getSenha()));
        assertEquals(PerfilUsuario.CLIENTE, cadastrado.getPerfil());

        mockMvc.perform(formLogin("/login")
                .user("email", "beatriz@teste.com")
                .password("senha", "senhaErrada"))
            .andExpect(unauthenticated())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error=true"));

        mockMvc.perform(formLogin("/login")
                .user("email", "beatriz@teste.com")
                .password("senha", "minhaSenha123"))
            .andExpect(authenticated().withRoles("USER"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/catalogo"));
    }

    @Test
    void deveRealizarFluxoCompletoDeCompraAlteracaoDeQuantidadeEMeusPedidos() throws Exception {
        Usuario cliente = usuarioServico.cadastrarCliente("Rafael Cliente", "rafael@teste.com", "123456", "123456");

        // 1. Adiciona produto a partir da pagina do catalogo
        mockMvc.perform(post("/carrinho/adicionar")
                .with(csrf())
                .with(user("rafael@teste.com").roles("USER"))
                .param("produtoId", produtoDisponivel.getId().toString())
                .param("quantidade", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/carrinho"));

        // 2. Aumenta quantidade pelo controle (+) do carrinho para 2 unidades
        Carrinho carrinho = carrinhoServico.buscarOuCriar(cliente.getId());
        Long itemId = carrinho.getItens().get(0).getId();

        mockMvc.perform(post("/carrinho/itens/" + itemId + "/quantidade")
                .with(csrf())
                .with(user("rafael@teste.com").roles("USER"))
                .param("quantidade", "2"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/carrinho"));

        // 3. Visualiza Meu Carrinho com subtotal calculado (2 x 149,90 = 299,80) e badge no menu
        mockMvc.perform(get("/carrinho")
                .with(user("rafael@teste.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Camiseta Oxford Minimal")))
            .andExpect(content().string(containsString("299,80")));

        // 4. Acessa tela de Checkout
        mockMvc.perform(get("/checkout")
                .with(user("rafael@teste.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Finalizar Compra")))
            .andExpect(content().string(containsString("Cartão de Crédito")));

        // 5. Finaliza compra e confirma redirecionamento para o pedido gerado
        MvcResult resultadoCheckout = mockMvc.perform(post("/checkout/finalizar")
                .with(csrf())
                .with(user("rafael@teste.com").roles("USER"))
                .param("formaPagamento", "CARTAO_CREDITO")
                .param("cep", "79000-000")
                .param("cidade", "Campo Grande / MS")
                .param("endereco", "Av. Afonso Pena, 1000"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/pedidos/*"))
            .andReturn();

        String urlPedido = resultadoCheckout.getResponse().getRedirectedUrl();

        // 6. Visualiza confirmacao do pedido, listagem Meus Pedidos e Minha Conta
        mockMvc.perform(get(urlPedido)
                .with(user("rafael@teste.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Pedido Confirmado")))
            .andExpect(content().string(containsString("Camiseta Oxford Minimal")))
            .andExpect(content().string(containsString("299,80")));

        mockMvc.perform(get("/pedidos")
                .with(user("rafael@teste.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("#BS-")))
            .andExpect(content().string(containsString("299,80")));

        mockMvc.perform(get("/minha-conta")
                .with(user("rafael@teste.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Rafael Cliente")));

        assertEquals(8, produtoServico.buscarPorId(produtoDisponivel.getId()).orElseThrow().getEstoque());
    }

    @Test
    void deveBloquearClienteComumNoPainelAdminELiberarAdministradorComGerenciamento() throws Exception {
        usuarioServico.salvar(new Usuario("Cliente Comum", "comum@teste.com", "123456", PerfilUsuario.CLIENTE));
        usuarioServico.salvar(new Usuario("Admin Loja", "admin@teste.com", "123456", PerfilUsuario.ADMIN));

        // Cliente comum recebe 403 tanto no Painel Admin quanto na escrita da API
        mockMvc.perform(get("/admin")
                .with(user("comum@teste.com").roles("USER")))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/categorias")
                .with(user("comum@teste.com").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Proibida\"}"))
            .andExpect(status().isForbidden());

        // Administrador cadastra categoria pelo painel e visualiza Dashboard Administrativo
        mockMvc.perform(post("/admin/categorias")
                .with(csrf())
                .with(user("admin@teste.com").roles("ADMIN"))
                .param("nome", "Manga Longa"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));

        mockMvc.perform(get("/admin")
                .with(user("admin@teste.com").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Dashboard Administrativo")))
            .andExpect(content().string(containsString("Manga Longa")));
    }
}
