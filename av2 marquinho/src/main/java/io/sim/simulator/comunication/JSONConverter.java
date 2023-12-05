package io.sim.simulator.comunication;

import org.json.JSONObject;

import io.sim.simulator.bank.TransferData;
import io.sim.simulator.company.Rota;
import io.sim.simulator.driver.DrivingData;

/**
 *      A Classe JSONConverter é responsável por converter dados entre objetos Java e representações JSON em forma de String, 
 * facilitando a comunicação entre diferentes partes do sistema
 */
public class JSONConverter {

    public JSONConverter() {
        // Construtor vazio, não realiza nenhuma ação especial.
    }

    //                                  Métodos usados no envio e recebimento de mensagens via Socket
    //--------------------------------------------------------------------------------------------------------------------------

    public static String criaJSONTamanhoBytes(int numBytes) {
        // Cria um objeto JSON com o número de bytes especificado e retorna a representação JSON em forma de String.
        JSONObject my_json = new JSONObject();
        my_json.put("Num Bytes", numBytes);
        return my_json.toString();
    }

    public static int extraiTamanhoBytes(String numBytesJSON) {
        // Extrai o número de bytes a partir de uma representação JSON em forma de String.
        JSONObject my_json = new JSONObject(numBytesJSON);
        int numBytes = my_json.getInt("Num Bytes");
        return numBytes;
    }

    //                                              Métodos usados na lógica de pagamentos
    //--------------------------------------------------------------------------------------------------------------------------

    // -> CLIENTE
    // Chamada em BotPayment
    public static String criarJSONLogin(String[] login) {
        // Cria um objeto JSON com as informações de login e senha e retorna a representação JSON em forma de String.
        JSONObject loginJSONObj = new JSONObject();
        loginJSONObj.put("ID do Pagador", login[0]);
        loginJSONObj.put("Senha do Pagador", login[1]);
        return loginJSONObj.toString();
    }

    // -> SERVIDOR
    // Chamada em AccountManipulator
    public static String[] extraiLogin(String loginJSON) {
        // Extrai o ID do pagador e a senha a partir de uma representação JSON em forma de String.
        JSONObject loginJSONObj = new JSONObject(loginJSON);
        String[] login = new String[] { loginJSONObj.getString("ID do Pagador"), loginJSONObj.getString("Senha do Pagador") };
        return login;
    }

    // -> CLIENTE
    // Chamada em BotPayment
    public static String criaJSONTransferData(TransferData transferData) {
        // Cria um objeto JSON com os dados de transferência e retorna a representação JSON em forma de String.
        JSONObject transferDataJSON = new JSONObject();
        transferDataJSON.put("ID do Pagador", transferData.getPagador());
        transferDataJSON.put("Operacao", transferData.getOperacao());
        transferDataJSON.put("ID do Recebedor", transferData.getRecebedor());
        transferDataJSON.put("Quantia", transferData.getQuantia());
        return transferDataJSON.toString();
    }

    // -> SERVIDOR
    // Chamada em AccountManipulator
    public static TransferData extraiTransferData(String transferDataJSON) {
        // Extrai os dados de transferência a partir de uma representação JSON em forma de String e retorna um objeto TransferData.
        JSONObject transferDataJSONObj = new JSONObject(transferDataJSON);
        String pagador = transferDataJSONObj.getString("ID do Pagador");
        String operacao = transferDataJSONObj.getString("Operacao");
        String recebedor = transferDataJSONObj.getString("ID do Recebedor");
        double quantia = transferDataJSONObj.getDouble("Quantia");
        TransferData tf = new TransferData(pagador, operacao, recebedor, quantia);
        return tf;
    }

    // -> SERVIDOR
    // Chamada em AccountManipulator
    public static String criaRespostaTransferencia(boolean sucesso) {
        // Cria um objeto JSON com a resposta de uma transferência e retorna a representação JSON em forma de String.
        JSONObject my_json = new JSONObject();
        my_json.put("Resposta", sucesso);
        return my_json.toString();
    }

    // -> CLIENTE
    // Chamada em BotPayment
    public static boolean extraiResposta(String respostaJSON) {
        // Extrai a resposta de uma transferência a partir de uma representação JSON em forma de String.
        JSONObject resposta = new JSONObject(respostaJSON);
        return resposta.getBoolean("Resposta");
    }

    //                                        Métodos usados na lógica de tratamento de rotas
    //--------------------------------------------------------------------------------------------------------------------------

    // -> CLIENTE
    // Chamada em Car
    public static String criarJSONDrivingData(DrivingData drivingData) {
        // Cria um objeto JSON com os dados de direção (DrivingData) e retorna a representação JSON em forma de String.
        JSONObject drivingDataJSON = new JSONObject();
        drivingDataJSON.put("Car ID", drivingData.getCarID());
        drivingDataJSON.put("Driver ID", drivingData.getDriverID());
        drivingDataJSON.put("Car Status", drivingData.getCarStatus());
        drivingDataJSON.put("Latitude Anterior", drivingData.getLatAnt());
        drivingDataJSON.put("Longitude Anterior", drivingData.getLonAnt());
        drivingDataJSON.put("Latitude Atual", drivingData.getLatAtual());
        drivingDataJSON.put("Longitude Atual", drivingData.getLonAtual());
        drivingDataJSON.put("Precisa Att Excel", drivingData.getPrecisaAttExcel());
        drivingDataJSON.put("TimeStamp", drivingData.getTimeStamp());
        drivingDataJSON.put("RouteIDSUMO", drivingData.getRouteIDSUMO());
        drivingDataJSON.put("Speed", drivingData.getSpeed());
        drivingDataJSON.put("Distance", drivingData.getDistance());
        drivingDataJSON.put("FuelConsumption", drivingData.getFuelConsumption());
        drivingDataJSON.put("FuelType", drivingData.getFuelType());
        drivingDataJSON.put("Co2Emission", drivingData.getCo2Emission());
        return drivingDataJSON.toString();
    }

    // -> SERVIDOR
    // Chamada em CarManipulator
    public static DrivingData extraiDrivingData(String drivingDataJSON) {
        // Extrai os dados de direção (DrivingData) a partir de uma representação JSON em forma de String e retorna um objeto DrivingData.
        JSONObject drivingDataJSONObj = new JSONObject(drivingDataJSON);
        // (Os comentários abaixo seguem o mesmo padrão dos métodos anteriores)
        String carID = drivingDataJSONObj.getString("Car ID");
        String driverID = drivingDataJSONObj.getString("Driver ID");
        String carStatus = drivingDataJSONObj.getString("Car Status");
        double latAnt = drivingDataJSONObj.getDouble("Latitude Anterior");
        double lonAnt = drivingDataJSONObj.getDouble("Longitude Anterior");
        double latAtual = drivingDataJSONObj.getDouble("Latitude Atual");
        double lonAtual = drivingDataJSONObj.getDouble("Longitude Atual");
        int precisaAttExcel = drivingDataJSONObj.getInt("Precisa Att Excel");
        long timeStamp = drivingDataJSONObj.getLong("TimeStamp");
        String routeIDSUMO = drivingDataJSONObj.getString("RouteIDSUMO");
        double speed = drivingDataJSONObj.getDouble("Speed");
        double distance = drivingDataJSONObj.getDouble("Distance");
        double fuelConsumption = drivingDataJSONObj.getDouble("FuelConsumption");
        int fuelType = drivingDataJSONObj.getInt("FuelType");
        double Co2Emission = drivingDataJSONObj.getDouble("Co2Emission");

        DrivingData drivingData = new DrivingData(carID, driverID, carStatus, latAnt, lonAnt, latAtual, lonAtual, precisaAttExcel, 
                                                    timeStamp, routeIDSUMO, speed, distance, fuelConsumption, fuelType, Co2Emission);

        return drivingData;
    }

    // -> SERVIDOR
    // Chamada em CarManipulator
    public static String criaJSONRota(Rota rota) {
        // Cria um objeto JSON com informações de uma rota e retorna a representação JSON em forma de String.
        JSONObject rotaJSON = new JSONObject();
        rotaJSON.put("ID da Rota", rota.getID());
        rotaJSON.put("Edges", rota.getEdges());
        return rotaJSON.toString();
    }

    // -> CLIENTE
    // Chamada em Car
    public static Rota extraiRota(String rotaJSON) {
        // Extrai as informações de uma rota a partir de uma representação JSON em forma de String e retorna um objeto Rota.
        JSONObject rotaJSONObj = new JSONObject(rotaJSON);
        Rota rota = new Rota(rotaJSONObj.getString("ID da Rota"), rotaJSONObj.getString("Edges"));
        return rota;
    }
}
