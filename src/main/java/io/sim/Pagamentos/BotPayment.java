package io.sim.Pagamentos;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

import io.sim.Crypto;

public class BotPayment extends Thread {
    private Socket socket;
    private String pagadorID;
    private String pagadorSenha;
    private String recebedorID;
    private double valor;

    public BotPayment(Socket _socket, String _pagadorID, String _pagadorSenha, String _recebedorID, double _valor) {
        this.socket = _socket;
        this.pagadorID = _pagadorID;
        this.pagadorSenha = _pagadorSenha;
        this.recebedorID = _recebedorID;
        this.valor = _valor;
    }

    @Override
    public void run() {
        try {
            // Crie streams de entrada e saída para comunicar com o servidor AlphaBank
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());

            int numBytesMsg;
            byte[] mensagemEncriptada;

            String[] login = { pagadorID, pagadorSenha };

            mensagemEncriptada = Crypto.encripta(criarJSONLogin(login));
			output.write(Crypto.encripta(criaJSONTamanhoBytes(mensagemEncriptada.length)));
			output.write(mensagemEncriptada);

            TransferData td = new TransferData(pagadorID, "Pagamento", recebedorID, valor);
            //System.out.println("!!!!!!!!!! - BP: " + recebedorID + " " + pagadorID);

            mensagemEncriptada = Crypto.encripta(criaJSONTransferData(td));
			output.write(Crypto.encripta(criaJSONTamanhoBytes(mensagemEncriptada.length)));
			output.write(mensagemEncriptada);

            // Aguarde a resposta do servidor AlphaBank
            numBytesMsg = extraiTamanhoBytes(Crypto.decripta(input.readNBytes(Crypto.getTamNumBytes())));
            boolean sucesso = extraiResposta(Crypto.decripta(input.readNBytes(numBytesMsg)));

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

    private String criaJSONTransferData(TransferData transferData) {
        JSONObject transferDataJSON = new JSONObject();
		transferDataJSON.put("ID do Pagador", transferData.getPagador());
        transferDataJSON.put("Operacao", transferData.getOperacao());
        transferDataJSON.put("ID do Recebedor", transferData.getRecebedor());
		transferDataJSON.put("valor", transferData.getvalor());
        return transferDataJSON.toString();
	}

    private String criarJSONLogin(String[] login) {
        JSONObject loginJSONObj = new JSONObject();
        loginJSONObj.put("ID do Pagador", login[0]);
		loginJSONObj.put("Senha do Pagador", login[1]);
        return loginJSONObj.toString();
    }

    private boolean extraiResposta(String respostaJSON) {
        JSONObject resposta = new JSONObject(respostaJSON);
        return resposta.getBoolean("Resposta");
    }

    private String criaJSONTamanhoBytes(int numBytes) {
        JSONObject my_json = new JSONObject();
        my_json.put("Num Bytes", numBytes);
        return my_json.toString();
    }

    private int extraiTamanhoBytes(String numBytesJSON) {
        JSONObject my_json = new JSONObject(numBytesJSON);
        int numBytes = my_json.getInt("Num Bytes");
        return numBytes;
    }

}


