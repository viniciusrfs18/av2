package io.sim.simulator.bank;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.simulator.comunication.AESencrypt;
import io.sim.simulator.comunication.JSONConverter;

/**
 * A classe EndAccount é uma thread que lida com o processo de encerramento de uma conta bancária.
 */
public class EndAccount extends Thread {
    private Socket socket;           // O socket de comunicação com o servidor AlphaBank
    private Account account;         // A conta a ser encerrada

    public EndAccount(Socket _socket, Account _account) {
        this.socket = _socket;
        this.account = _account;
    }

    @Override
    public void run() {
        try {
            // Crie streams de entrada e saída para comunicar com o servidor AlphaBank
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            byte[] mensagemEncriptada;

            // Preparar informações de login para a conta a ser encerrada
            String[] login = { account.getAccountID(), account.getSenha() };

            // Enviar informações de login para o servidor AlphaBank
            mensagemEncriptada = AESencrypt.encripta(JSONConverter.criarJSONLogin(login));
			output.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
			output.write(mensagemEncriptada);

            // Preparar informações para encerrar a conta
            TransferData td = new TransferData(account.getAccountID(), "Sair", "", 0);

            // Enviar informações para encerrar a conta para o servidor AlphaBank
            mensagemEncriptada = AESencrypt.encripta(JSONConverter.criaJSONTransferData(td));
			output.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
			output.write(mensagemEncriptada);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
