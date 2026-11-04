package com.grouploja.basic_shirt_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grouploja.basic_shirt_app.entiny.Pedido;

public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {

    // Listar os pedidos de um usuário, do mais recente para o mais antigo
    List<Pedido> findByUsuarioIdOrderByDataPedidoDesc(Long usuarioId);

    // Listar todos os pedidos do sistema, do mais recente para o mais antigo (Painel Admin)
    List<Pedido> findAllByOrderByDataPedidoDesc();

    // Buscar um pedido específico pertencente a determinado usuário
    Optional<Pedido> findByIdAndUsuarioId(Long id, Long usuarioId);
}
