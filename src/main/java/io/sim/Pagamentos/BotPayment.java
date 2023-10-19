package io.sim.Pagamentos;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.JSONConverter;

public class BotPayment extends Thread {
    private Socket socket;
    private String pagadorID;
    private String pagadorSenha;
    private String recebedorID;
    private double quantia;

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

            String[] login = { pagadorID, pagadorSenha };

            output.writeUTF(JSONConverter.criarJSONLogin(login));

            TransferData td = new TransferData(recebedorID, "Pagamento", pagadorID, quantia);

            output.writeUTF(JSONConverter.criaJSONTransferData(td));

            // Aguarde a resposta do servidor AlphaBank
            String resposta = input.readUTF();
            boolean sucesso = JSONConverter.extraiResposta(resposta);

            if (sucesso) {
                System.out.println("Transferência bem-sucedida!");
            } else {
                System.out.println("Transferência falhou.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


