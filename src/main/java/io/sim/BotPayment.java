
package io.sim;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONObject;
import io.sim.Rota;

public class BotPayment extends Thread {
    private Socket socket;
    private String payerID;
    private String payerPassword;
    private String receiverID;
    private double amount;

    // Construtor para inicializar o objeto BotPayment
    public BotPayment(Socket _socket, String _payerID, String _payerPassword, String _receiverID, double _amount) {
        this.socket = _socket;
        this.payerID = _payerID;
        this.payerPassword = _payerPassword;
        this.receiverID = _receiverID;
        this.amount = _amount;
    }

    @Override
    public void run() {
        try {
            // Crie streams de entrada e saída para comunicar com o servidor AlphaBank
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());

            String[] login = { payerID, payerPassword };

            // Envia informações de login para o servidor AlphaBank
            output.writeUTF(loginJSON(login));

            TransferData td = new TransferData(payerID, "Pagamento", receiverID, amount);

            // Envia informações de transferência para o servidor AlphaBank
            output.writeUTF(transferDataJSON(td));

            // Aguarda a resposta do servidor AlphaBank
            String response = input.readUTF();
            boolean success = extractResponse(response);

            if (success) {
                System.out.println("Transferência bem-sucedida!");
            } else {
                System.out.println("Transferência falhou.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para transformar informações de transferência em formato JSON
    private String transferDataJSON(TransferData transferData) {
        JSONObject transferDataJSON = new JSONObject();
        transferDataJSON.put("payerID", transferData.getpayer());
        transferDataJSON.put("operation", transferData.getoperation());
        transferDataJSON.put("receiverID", transferData.getreceiver());
        transferDataJSON.put("amount", transferData.getamount());
        return transferDataJSON.toString();
    }

    // Método para transformar informações de login em formato JSON
    private String loginJSON(String[] login) {
        JSONObject json = new JSONObject();
        json.put("payerID", login[0]);
        json.put("payerPassword", login[1]);
        return json.toString();
    }

    // Método para extrair a resposta do formato JSON
    private boolean extractResponse(String responseJSON) {
        JSONObject response = new JSONObject(responseJSON);
        return response.getBoolean("response");
    }
}
