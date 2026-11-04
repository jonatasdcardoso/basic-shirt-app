package com.grouploja.basic_shirt_app.entiny;

public enum PerfilUsuario {
    CLIENTE("Cliente", "ROLE_USER"),
    ADMIN("Administrador", "ROLE_ADMIN");

    private final String descricao;
    private final String authority;

    PerfilUsuario(String descricao, String authority) {
        this.descricao = descricao;
        this.authority = authority;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getAuthority() {
        return authority;
    }
}
