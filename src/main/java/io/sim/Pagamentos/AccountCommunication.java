package io.sim.Pagamentos;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.json.JSONObject;

public class AccountCommunication extends Thread {

    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream saida;
    private AlphaBank alphaBank;

    public AccountCommunication(Socket _socket, AlphaBank _alphaBank) {
        this.socket = _socket;
        this.alphaBank = _alphaBank;
    }

    @Override
    public void run() {

        boolean sair = false;
        
        try {
        
            entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());
            
            while (!sair) {
                String[] login = extraiLogin(entrada.readUTF());

                if (alphaBank.conect(login)) {
                    TransferData tf = extraiTransferData(entrada.readUTF());
                    
                    System.out.println("Leu as informações de Operacao!!");
                    String operacao = tf.getOperacao();
                    switch (operacao) {
                        case "Pagamento":
                            String recebedorID = tf.getRecebedor();
                            double valor = tf.getvalor();
                            System.out.println(valor + " " + recebedorID);
                            if (alphaBank.transferencia(login[0], recebedorID, valor)) {
                                saida.writeUTF(criaRespostaTransferencia(true));
                                alphaBank.adicionaRegistros(tf);
                            } else {
                                saida.writeUTF(criaRespostaTransferencia(false));
                            }
                            
                            break;
                        case "Sair":
                            sair = true;
                            break;
                        default:
                            break;
                    }
                } else {
                    System.out.println("AB - Login mal sucedido, verifique o ID e a senha: " + login[0]);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    // Método responsável por extrair as credenciais de login 
    private String[] extraiLogin(String loginJSON) {
        JSONObject loginJSONObj = new JSONObject(loginJSON);
        String[] login = new String[] { loginJSONObj.getString("ID do Pagador"), loginJSONObj.getString("Senha do Pagador") };
        return login;
    }

    // Método responsável por extrair os dados para transferencia
    private TransferData extraiTransferData(String transferDataJSON) {
        JSONObject transferDataJSONObj = new JSONObject(transferDataJSON);
		String pagador = transferDataJSONObj.getString("ID do Pagador");
        String operacao = transferDataJSONObj.getString("Operacao");
        String recebedor = transferDataJSONObj.getString("ID do Recebedor");
		double valor = transferDataJSONObj.getDouble("valor");
        TransferData tf = new TransferData(pagador, operacao, recebedor, valor);
		return tf;
	}

    private String criaRespostaTransferencia(boolean sucesso) {
        JSONObject my_json = new JSONObject();
        my_json.put("Resposta", sucesso);
        return my_json.toString();
    }

}