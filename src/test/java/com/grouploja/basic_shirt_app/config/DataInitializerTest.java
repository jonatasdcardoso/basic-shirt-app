package com.grouploja.basic_shirt_app.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.grouploja.basic_shirt_app.service.CategoriaServico;
import com.grouploja.basic_shirt_app.service.ProdutoServico;
import com.grouploja.basic_shirt_app.service.UsuarioServico;

@SpringBootTest
@Transactional
class DataInitializerTest {

    @Autowired
    private UsuarioServico usuarioServico;

    @Autowired
    private CategoriaServico categoriaServico;

    @Autowired
    private ProdutoServico produtoServico;

    @Test
    void devePopularUsuariosPadraoECatalogoInicialQuandoBancoEstiverVazio() {
        DataInitializer initializer = new DataInitializer(
            usuarioServico,
            categoriaServico,
            produtoServico
        );

        initializer.run();

        assertTrue(usuarioServico.existePorEmail("admin@basicshirt.com"));
        assertTrue(usuarioServico.existePorEmail("cliente@basicshirt.com"));
        assertTrue(categoriaServico.listarTodas().size() >= 4);
        assertTrue(produtoServico.listarTodos().size() >= 6);
    }
}
