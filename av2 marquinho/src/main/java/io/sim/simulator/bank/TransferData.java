package io.sim.simulator.bank;

import java.sql.Timestamp;

/**
 *      A classe TransferData representa informações associadas a transferências financeiras, como quem fez o pagamento, quem está 
 * recebendo, a quantia transferida, entre outros.
 */
public class TransferData {
    private String accountID;      // ID da conta associada a esta transferência
    private String pagador;        // Quem está fazendo o pagamento
    private String operacao;       // O tipo de operação (Pagamento ou Recebimento)
    private String recebedor;      // Quem está recebendo o pagamento
    private double quantia;        // A quantidade de dinheiro envolvida na transferência
    private Timestamp timestamp;   // Hora da transferência
    private double saldoAtual;     // O saldo atual da conta

    // Construtor para criar uma instância de TransferData com informações iniciais
    public TransferData(String _pagador, String _operacao, String _recebedor, double _quantia) {
        this.pagador = _pagador;
        this.operacao = _operacao;
        this.recebedor = _recebedor;
        this.quantia = _quantia;
    }

    // Define o ID da conta associada a esta transferência
    public void setAccountID(String _accoutID) {
        accountID = _accoutID;
    }

    // Obtém o ID da conta associada a esta transferência
    public String getAccountID() {
        return accountID;
    }

    // Obtém o ID do pagador
    public String getPagador() {
        return pagador;
    }

    // Obtém o tipo de operação (Pagamento ou Recebimento)
    public String getOperacao() {
        return operacao;
    }

    // Obtém o ID do recebedor
    public String getRecebedor() {
        return recebedor;
    }

    // Obtém a quantidade de dinheiro envolvida na transferência
    public double getQuantia() {
        return quantia;
    }

    // Define a hora da transferência
    public void setTimestamp() {
        timestamp = new Timestamp(System.nanoTime());
    }

    // Obtém o carimbo de data e hora da transferência
    public Timestamp getTimestamp() {
        return timestamp;
    }

    // Define o saldo atual da conta após a transferência
    public void setSaldoAtual(double novoSaldo) {
        saldoAtual = novoSaldo;
    }

    // Obtém o saldo atual da conta após a transferência
    public double getSaldoAtual() {
        return saldoAtual;
    }

    // Cria uma descrição da transferência com base no tipo de operação
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
