package io.sim.simulator.bank;

import io.sim.simulator.report.ExcelReport;

/**
 *          A classe AlphaBankAttExcel é responsável por atualizar um relatório Excel com registros de transferências bancárias
 *  provenientes do AlphaBank.
 */
public class AlphaBankAttExcel extends Thread {
    private AlphaBank alphaBank;
    private boolean funcionando;

    public AlphaBankAttExcel(AlphaBank _alphaBank) {
        this.alphaBank = _alphaBank;
        this.funcionando = true;
    }

    @Override
    public void run() {
        try {
            while (funcionando) {
                Thread.sleep(10); // Aguarda por um curto período (10 milissegundos) para evitar uso intensivo da CPU
                if (alphaBank.temRegistro()) {
                    // Se houver registros pendentes no AlphaBank
                    TransferData registro = alphaBank.pegaRegistro(); // Pega o próximo registro
                    // Atualiza a planilha Excel com o registro
                    ExcelReport.atualizaPlanilhaAccount(registro);
                    // Chama o método do banco para enviar o registro ao histórico da conta correspondente
                    alphaBank.mandaRegistroAcc(registro);
                }
            }
            System.out.println("AlphaBankAttExcel encerrou!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setFuncionado(boolean _funcionado) {
        funcionando = _funcionado;
    }
}

