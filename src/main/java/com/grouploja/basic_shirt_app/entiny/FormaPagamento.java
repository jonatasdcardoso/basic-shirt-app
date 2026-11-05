package com.grouploja.basic_shirt_app.entiny;

public enum FormaPagamento {
    PIX("PIX", "Pagamento instantâneo com confirmação imediata"),
    CARTAO_CREDITO("Cartão de Crédito", "Parcelamento em até 3x sem juros"),
    CARTAO_DEBITO("Cartão de Débito", "Pagamento à vista no débito"),
    BOLETO("Boleto Bancário", "Vencimento em 3 dias úteis");

    private final String descricao;
    private final String detalhe;

    FormaPagamento(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }
}
