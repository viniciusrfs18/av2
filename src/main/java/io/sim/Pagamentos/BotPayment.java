package io.sim.Pagamentos;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class BotPayment extends Thread {
    private Socket socket;
    private double valor;
    private int contaPag;
    private int contaRec;

    public BotPayment(Socket s, int contaPag, int contaRec, double valor) {
        this.socket = s;
        this.valor = valor;
        this.contaPag = contaPag;
        this.contaRec = contaRec;
    }

    @Override
    public void run() {
        try {
            // Crie streams de entrada e saída para comunicar com o servidor AlphaBank
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            // Construa a solicitação em formato JSON
            String jsonRequest = buildJsonRequest(contaPag, contaRec, valor);

            // Envie a solicitação ao servidor AlphaBank
            outputStream.writeUTF(jsonRequest);
            //outputStream.flush();

            // Aguarde a resposta do servidor AlphaBank
            String jsonResponse = inputStream.readUTF();

            // Analise a resposta em formato JSON
            boolean success = parseJsonResponse(jsonResponse);

            if (success) {
                System.out.println("Transferência bem-sucedida!");
            } else {
                System.out.println("Transferência falhou. Verifique o saldo ou a existência das contas.");
            }

            // Feche a conexão
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Construa a solicitação em formato JSON
    private String buildJsonRequest(int contaPag, int contaRec, double valor) {
        // Aqui você pode usar uma biblioteca JSON, como Jackson ou Gson, para construir a solicitação JSON.
        // Neste exemplo, construiremos manualmente uma string JSON simples.
        return "{\"command\":\"TRANSFER\",\"senderID\":" + contaPag + ",\"receiverID\":" + contaRec + ",\"amount\":" + valor + "}";
    }

    // Analise a resposta em formato JSON
    private boolean parseJsonResponse(String jsonResponse) {
        // Aqui você pode usar uma biblioteca JSON, como Jackson ou Gson, para analisar a resposta JSON.
        // Neste exemplo, analisaremos manualmente a string JSON.
        return jsonResponse.equals("true");
    }
}

