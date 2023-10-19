package io.sim.Pagamentos;

import java.sql.Timestamp;

public class TransferData {
    private String accountID;
    private String pagador;
    private String operacao;
    private String recebedor;
    private double valor;
    private Timestamp timestamp;

    public TransferData(String _pagador, String _operacao, String _recebedor, double _valor) {
        this.pagador = _pagador;
        this.operacao = _operacao;
        this.recebedor = _recebedor;
        this.valor = _valor;
    }

    public void setAccountID(String _accoutID) {
        accountID = _accoutID;
    }

    public String getAccountID() {
        return accountID;
    }

    public String getPagador() {
        return pagador;
    }

    public String getOperacao() {
        return operacao;
    }

    public String getRecebedor() {
        return recebedor;
    }

    public double getvalor() {
        return valor;
    }

    public void setTimestamp() {
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getDescricao() {
        String descricao = "";
        if (operacao.equals("Pagamento")) {
            descricao = pagador + " transferiu R$" + valor + " para " + recebedor;
        } else if (operacao.equals("Recebimento")) {
            descricao = recebedor + " recebeu R$" + valor + " de " + pagador;
        }

        return descricao;
    }
}

