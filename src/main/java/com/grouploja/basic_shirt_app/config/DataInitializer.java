package com.grouploja.basic_shirt_app.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.entiny.Categoria;
import com.grouploja.basic_shirt_app.entiny.PerfilUsuario;
import com.grouploja.basic_shirt_app.entiny.Produto;
import com.grouploja.basic_shirt_app.entiny.Usuario;
import com.grouploja.basic_shirt_app.service.CategoriaServico;
import com.grouploja.basic_shirt_app.service.ProdutoServico;
import com.grouploja.basic_shirt_app.service.UsuarioServico;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioServico usuarioServico;
    private final CategoriaServico categoriaServico;
    private final ProdutoServico produtoServico;

    public DataInitializer(
            UsuarioServico usuarioServico,
            CategoriaServico categoriaServico,
            ProdutoServico produtoServico) {
        this.usuarioServico = usuarioServico;
        this.categoriaServico = categoriaServico;
        this.produtoServico = produtoServico;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Garante usuarios padrao para homologacao de Cliente e Administrador
        if (!usuarioServico.existePorEmail("admin@basicshirt.com")) {
            usuarioServico.salvar(new Usuario(
                "Administrador Basic Shirt",
                "admin@basicshirt.com",
                "admin123",
                PerfilUsuario.ADMIN
            ));
        }

        if (!usuarioServico.existePorEmail("cliente@basicshirt.com")) {
            usuarioServico.salvar(new Usuario(
                "Cliente Demonstração",
                "cliente@basicshirt.com",
                "cliente123",
                PerfilUsuario.CLIENTE
            ));
        }

        // Popula o catalogo inicial apenas quando o banco estiver vazio
        if (categoriaServico.listarTodas().isEmpty() && produtoServico.listarTodos().isEmpty()) {
            Categoria essenciais = categoriaServico.salvar(new Categoria("Essenciais"));
            Categoria oversized = categoriaServico.salvar(new Categoria("Oversized"));
            Categoria premium = categoriaServico.salvar(new Categoria("Linha Pima"));
            Categoria kits = categoriaServico.salvar(new Categoria("Kits"));

            produtoServico.salvar(new Produto(
                "Camiseta Básica Preta 100% Algodão",
                "Modelagem regular com gola canelada reforçada, toque macio e pré-encolhida para manter o caimento perfeito.",
                new BigDecimal("89.90"),
                25,
                essenciais
            ));

            produtoServico.salvar(new Produto(
                "Camiseta Básica Branca Off-White",
                "Algodão penteado de gramatura média que não fica transparente. Ideal para uso diário e sobreposições.",
                new BigDecimal("89.90"),
                18,
                essenciais
            ));

            produtoServico.salvar(new Produto(
                "Camiseta Oversized Heavyweight Chumbo",
                "Gramatura encorpada 240g/m², ombros deslocados e caimento urbano estruturado.",
                new BigDecimal("129.90"),
                12,
                oversized
            ));

            produtoServico.salvar(new Produto(
                "Camiseta Pima Classic Azul Marinho",
                "Produzida com algodão Pima de fibra extra longa: brilho sutil, alta respirabilidade e durabilidade superior.",
                new BigDecimal("159.90"),
                10,
                premium
            ));

            produtoServico.salvar(new Produto(
                "Kit 3 Camisetas Essenciais (Preta, Branca e Cinza)",
                "Combo econômico com as três cores indispensáveis do guarda-roupa minimalista Basic Shirt.",
                new BigDecimal("239.90"),
                8,
                kits
            ));

            produtoServico.salvar(new Produto(
                "Camiseta Oversized Areia Edição Limitada",
                "Lote especial esgotado temporariamente, demonstrando o controle automático de estoque na vitrine.",
                new BigDecimal("139.90"),
                0,
                oversized
            ));
        }
    }
}
