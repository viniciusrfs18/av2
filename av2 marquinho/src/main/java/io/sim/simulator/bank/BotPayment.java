package io.sim.simulator.bank;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.simulator.comunication.AESencrypt;
import io.sim.simulator.comunication.JSONConverter;

/**
 *      Essa classe representa um bot que realiza transferências bancárias por meio do servidor AlphaBank. Ela se comunica 
 * com o servidor para autenticação e envio dos detalhes da transferência
 */
public class BotPayment extends Thread {

    private Socket socket;            // Socket de comunicação com o servidor AlphaBank
    private String pagadorID;         // O identificador da conta do pagador
    private String pagadorSenha;      // A senha da conta do pagador
    private String recebedorID;       // O identificador da conta do recebedor
    private double quantia;           // A quantia a ser transferida

    public BotPayment(Socket _socket, String _pagadorID, String _pagadorSenha, String _recebedorID, double _quantia) {
        this.socket = _socket;
        this.pagadorID = _pagadorID;
        this.pagadorSenha = _pagadorSenha;
        this.recebedorID = _recebedorID;
        this.quantia = _quantia;
    }

    @Override
    public void run() {
        try {
            // Crie streams de entrada e saída para comunicar com o servidor AlphaBank
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            int numBytesMsg;
            byte[] mensagemEncriptada;

            // Prepare informações de login para o servidor AlphaBank
            String[] login = { pagadorID, pagadorSenha };

            // Encripte e envie a mensagem de login
            mensagemEncriptada = AESencrypt.encripta(JSONConverter.criarJSONLogin(login));
            output.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
            output.write(mensagemEncriptada);

            // Crie um objeto TransferData com detalhes da transferência
            TransferData td = new TransferData(pagadorID, "Pagamento", recebedorID, quantia);

            // Encripte e envie os detalhes da transferência
            mensagemEncriptada = AESencrypt.encripta(JSONConverter.criaJSONTransferData(td));
            output.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
            output.write(mensagemEncriptada);

            // Aguarde a resposta do servidor AlphaBank
            numBytesMsg = JSONConverter.extraiTamanhoBytes(AESencrypt.decripta(input.readNBytes(AESencrypt.getTamNumBytes())));
            boolean sucesso = JSONConverter.extraiResposta(AESencrypt.decripta(input.readNBytes(numBytesMsg)));

            // Exiba uma mensagem de sucesso ou falha com base na resposta do servidor
            if (sucesso) {
                System.out.println("Transferência bem-sucedida!");
            } else {
                System.out.println("Transferência falhou.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
