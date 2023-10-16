package io.sim.Pagamentos;

import java.util.ArrayList;
import java.io.*;
import java.net.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AlphaBank extends Thread {
    private ArrayList<Account> accounts;
    private ServerSocket serverSocket;
    private ObjectMapper objectMapper; // Usaremos a biblioteca Jackson para lidar com JSON

    public AlphaBank(ServerSocket serverSocket) throws IOException {
        this.accounts = new ArrayList<>();
        this.serverSocket = serverSocket;
        this.objectMapper = new ObjectMapper();
    }

    public boolean hasAccount(int identifier) {
        for (Account account : accounts) {
            if (account.getIdentifier() == identifier) {
                return true;
            }
        }
        return false;
    }

    public boolean transfer(int senderID, int receiverID, double amount) {
        Account sender = getAccountByID(senderID);
        Account receiver = getAccountByID(receiverID);

        //System.out.println("PROBELMA COM FOI SENDER OU RECIEVER");
        
        if (sender != null && receiver != null) {
            //System.out.println("NAO FOI SENDER OU RECIEVER");
            //System.out.println(sender.getBalance());
            //System.out.println(amount);
            if (sender.getBalance() >= amount) {
                sender.withdraw(amount);
                receiver.deposit(amount);
                return true;
            }
        }
        return false;
    }

    private Account getAccountByID(int identifier) {
        for (Account account : accounts) {
            if (account.getIdentifier() == identifier) {
                return account;
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            System.out.println("AlphaBank iniciado. Aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(() -> {
                    try (DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                         DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream())) {

                        String jsonRequest = inputStream.readUTF();
                        Request request = objectMapper.readValue(jsonRequest, Request.class);

                        if (request.getCommand().equals("TRANSFER")) {
                            int senderID = request.getSenderID();
                            int receiverID = request.getReceiverID();
                            double amount = request.getAmount();
                            
                            boolean success = transfer(senderID, receiverID, amount);
                            Response response = new Response(success);
                            String jsonResponse = objectMapper.writeValueAsString(response);

                            outputStream.writeUTF(jsonResponse);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAccount(ArrayList<Account> accounts){
        this.accounts = accounts;
    }
    // Resto do código permanece inalterado

    public static class Request {
        private String command;
        private int senderID;
        private int receiverID;
        private double amount;

        public String getCommand(){
            return command;
        }

        public int getSenderID(){
            return senderID;
        }

        public int getReceiverID(){
            return receiverID;
        }

        public double getAmount(){
            return amount;
        }

    }

    public static class Response {
        private boolean success;

        public Response(boolean success){
            this.success = success;
        }

        public boolean getSuccess(){
            return success;
        }
    }
}

