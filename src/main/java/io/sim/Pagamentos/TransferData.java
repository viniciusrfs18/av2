package io.sim.Pagamentos;

import java.sql.Timestamp;

public class TransferData {
    private String accountID;
    private String pagador;
    private String operacao;
    private String recebedor;
    private double quantia;
    private Timestamp timestamp;

    public TransferData(String _pagador, String _operacao, String _recebedor, double _quantia) {
        this.pagador = _pagador;
        this.operacao = _operacao;
        this.recebedor = _recebedor;
        this.quantia = _quantia;
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

    public double getQuantia() {
        return quantia;
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
            descricao = pagador + " transferiu R$" + quantia + " para " + recebedor;
        } else if (operacao.equals("Recebimento")) {
            descricao = recebedor + " recebeu R$" + quantia + " de " + pagador;
        }

        return descricao;
    }
}

