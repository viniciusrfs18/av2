package io.sim;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.json.JSONObject;

public class AccountCommunication extends Thread {

    private Socket socket;          
    private DataInputStream input;    
    private DataOutputStream output;  
    private AlphaBank alphaBank;      

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
              
                String[] login = loginExtration(input.readUTF());

                
                if (alphaBank.conect(login)) {

                    
                    Auxilio tf = AuxilioExtration(input.readUTF());
                    String operation = tf.getoperation();

                    if (operation.equals("Pagamento")) {
                        String receiverID = tf.getreceiver();
                        double amount = tf.getamount();

                        if (alphaBank.transferencia(login[0], receiverID, amount)) {
                        
                            output.writeUTF(responseJSON(true));
                
                            alphaBank.addRecords(tf);
                        } else {
                    
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
    private String[] loginExtration(String loginJSON) {
        JSONObject json = new JSONObject(loginJSON);
        String[] login = new String[] { json.getString("payerID"), json.getString("payerPassword") };
        return login;
    }

    private Auxilio AuxilioExtration(String AuxilioJSON) {
        JSONObject AuxilioJSONObj = new JSONObject(AuxilioJSON);

        String payer = AuxilioJSONObj.getString("payerID");
        String operation = AuxilioJSONObj.getString("operation");
        String receiver = AuxilioJSONObj.getString("receiverID");
        double amount = AuxilioJSONObj.getDouble("amount");

        Auxilio tf = new Auxilio(payer, operation, receiver, amount);

        return tf;
    }
    private String responseJSON(boolean success) {
        JSONObject json = new JSONObject();
        json.put("response", success);
        return json.toString();
    }
}
