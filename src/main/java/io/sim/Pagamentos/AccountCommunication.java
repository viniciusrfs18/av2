package io.sim.Pagamentos;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.json.JSONObject;

public class AccountCommunication extends Thread {

    private Socket socket;            // O socket usado para comunicação com um cliente
    private DataInputStream input;    // Stream de entrada de dados do socket
    private DataOutputStream output;  // Stream de saída de dados do socket
    private AlphaBank alphaBank;      // Referência a uma instância da classe AlphaBank

    public AccountCommunication(Socket _socket, AlphaBank _alphaBank) {
        this.socket = _socket;
        this.alphaBank = _alphaBank;
    }

    @Override
    public void run() {

        boolean aux = false;

        try {

            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            while (!aux) {
                // Extrai os dados de login da mensagem JSON recebida
                String[] login = loginExtration(input.readUTF());

                // Verifica se o login é bem-sucedido consultando o AlphaBank
                if (alphaBank.conect(login)) {

                    // Extrai os dados de transferência da mensagem JSON
                    TransferData tf = transferDataExtration(input.readUTF());
                    String operation = tf.getoperation();

                    if (operation.equals("Pagamento")) {
                        String receiverID = tf.getreceiver();
                        double amount = tf.getamount();

                        if (alphaBank.transferencia(login[0], receiverID, amount)) {
                            // Se a transferência for bem-sucedida, envia uma resposta JSON de sucesso
                            output.writeUTF(responseJSON(true));
                            // Adiciona o registro da transferência ao AlphaBank
                            alphaBank.addRecords(tf);
                        } else {
                            // Se a transferência falhar, envia uma resposta JSON de falha
                            output.writeUTF(responseJSON(false));
                        }
                    } else if (operation.equals("Sair")) {
                        aux = true;
                    }
                } else {
                    System.out.println("AB - Login malsucedido, verifique o ID e a senha: " + login[0]);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Método responsável por extrair as credenciais de login a partir de um JSON
    private String[] loginExtration(String loginJSON) {
        JSONObject json = new JSONObject(loginJSON);
        String[] login = new String[] { json.getString("payerID"), json.getString("payerPassword") };
        return login;
    }

    // Método responsável por extrair os dados para transferência a partir de um JSON
    private TransferData transferDataExtration(String transferDataJSON) {
        JSONObject transferDataJSONObj = new JSONObject(transferDataJSON);

        String payer = transferDataJSONObj.getString("payerID");
        String operation = transferDataJSONObj.getString("operation");
        String receiver = transferDataJSONObj.getString("receiverID");
        double amount = transferDataJSONObj.getDouble("amount");

        TransferData tf = new TransferData(payer, operation, receiver, amount);

        return tf;
    }

    // Método que gera uma resposta JSON com base no sucesso da operação
    private String responseJSON(boolean success) {
        JSONObject json = new JSONObject();
        json.put("response", success);
        return json.toString();
    }
}
