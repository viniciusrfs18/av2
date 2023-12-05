package io.sim.simulator.bank;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import io.sim.simulator.comunication.AESencrypt;
import io.sim.simulator.comunication.JSONConverter;

/**
 *      A classe AccountManipulator faz a comunicação com os clientes do AlphaBank e lida com solicitações de login, transferência 
 * bancárias e encerramento de contas. Ela funciona como uma thread de servidor, processando as ações do cliente de acordo 
 * com as operações recebidas e os dados do banco AlphaBank
 */
public class AccountManipulator extends Thread {

    // Atributos da classe
    private Socket socket; // Socket para comunicação com o cliente
    private DataInputStream entrada; // Fluxo de entrada de dados do cliente
    private DataOutputStream saida; // Fluxo de saída de dados para o cliente
    private AlphaBank alphaBank; // Instância do banco AlphaBank

    public AccountManipulator(Socket _socket, AlphaBank _alphaBank) {
        // Inicialização da classe
        this.socket = _socket;
        this.alphaBank = _alphaBank;
    }

    @Override
    public void run() {
        boolean sair = false; // Variável para controlar a execução da thread
        try {
            entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());

            // Loop principal para processar solicitações do cliente
            while (!sair) {
                // Recebe uma mensagem encriptada contendo informações do cliente
                int numBytesMsg = JSONConverter.extraiTamanhoBytes(AESencrypt.decripta(entrada.readNBytes(AESencrypt.getTamNumBytes())));
                String[] login = JSONConverter.extraiLogin(AESencrypt.decripta(entrada.readNBytes(numBytesMsg)));

                // Verifica se o login do cliente foi bem-sucedido
                if (alphaBank.fazerLogin(login)) {
                    // Se o login foi bem-sucedido, continue processando as operações do cliente
                    numBytesMsg = JSONConverter.extraiTamanhoBytes(AESencrypt.decripta(entrada.readNBytes(AESencrypt.getTamNumBytes())));
                    TransferData tf = JSONConverter.extraiTransferData(AESencrypt.decripta(entrada.readNBytes(numBytesMsg)));
                    String operacao = tf.getOperacao();

                    switch (operacao) {
                        case "Pagamento":
                            // Processa uma operação de pagamento, que envolve transferência bancária
                            String recebedorID = tf.getRecebedor();
                            double quantia = tf.getQuantia();

                            // Realiza a transferência bancária
                            if (alphaBank.transferencia(login[0], recebedorID, quantia)) {
                                // Se a transferência for bem-sucedida, envia uma resposta positiva ao cliente
                                byte[] mensagemEncriptada = AESencrypt.encripta(JSONConverter.criaRespostaTransferencia(true));
                                saida.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
                                saida.write(mensagemEncriptada);
                                alphaBank.adicionaRegistros(tf); // Chama o método da Alpha Bank para criar e adicionar registros sobre a transferência
                            } else {
                                // Caso contrário, envia uma resposta negativa ao cliente
                                byte[] mensagemEncriptada = AESencrypt.encripta(JSONConverter.criaRespostaTransferencia(false));
                                saida.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
                                saida.write(mensagemEncriptada);
                            }
                            break;

                        case "Sair":
                            // Finaliza a thread e remove a conta associada
                            sair = true;
                            String accountID = tf.getPagador();
                            alphaBank.removerAccount(accountID);
                            System.out.println("Conta de " + accountID + " foi removida!!");
                            break;

                        default:
                            break;
                    }
                } else {
                    System.out.println("AB - Login mal sucedido, verifique o ID e a senha: " + login[0]);
                }
                // System.out.println("Account Manipulator encerrado...");
            } 
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
