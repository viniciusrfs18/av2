package io.sim.simulator.company;

import io.sim.simulator.report.ExcelReport;

/**
 *      A classe CompanyAttExcel é responsável por atualizar relatórios no formato Excel, com base nas informações de comunicação 
 * recebidas pela classe Company.
 */
public class CompanyAttExcel extends Thread {
    private Company company; // Uma instância da classe Company
    private boolean funcionando;

    public CompanyAttExcel(Company _company) {
        this.company = _company;
        this.funcionando = true;
    }

    @Override
    public void run() {
        try {
            // Loop principal que verifica a disponibilidade de rotas
            while (funcionando) {
                Thread.sleep(10); // Aguarda por um curto período (10 milissegundos) para evitar uso intensivo da CPU
                if (company.temReport()) {
                    // Se a instância da classe Company possui relatórios para serem atualizados
                    ExcelReport.atualizaPlanilhaCar(company.pegaComunicacao());  // Atualiza o relatório no Excel
                }
            }
            System.out.println("CompanyAttExcel encerrou!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setFuncionando(boolean _funcionando) {
        funcionando = _funcionando;
    }
}
