package io.sim.reconciliation;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class IncrementXml {

    public static void main(String[] args) {
        // Substitua esses valores pelos incrementos desejados para id e depart
        int startingId = 7981;
        double startingDepart = 7981.0;

        // Caminho dos arquivos de entrada e saída
        String inputFile = "map/map.rou.xml";
        String outputFile = "C:/teste/map.alterado.xml";

        try {
            // Inicializa o parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Faz o parse do arquivo de entrada
            Document document = builder.parse(new File(inputFile));

            // Obtém a lista de todos os elementos "vehicle"
            NodeList vehicleList = document.getElementsByTagName("vehicle");

            // Itera sobre cada elemento "vehicle"
            for (int i = 0; i < vehicleList.getLength(); i++) {
                Node vehicleNode = vehicleList.item(i);

                if (vehicleNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element vehicleElement = (Element) vehicleNode;

                    // Obtém o atributo "id" e "depart"
                    String idValue = vehicleElement.getAttribute("id");
                    String departValue = vehicleElement.getAttribute("depart");

                    // Converte os valores para int e double
                    int currentId = Integer.parseInt(idValue);
                    double currentDepart = Double.parseDouble(departValue);

                    // Incrementa os valores
                    currentId += startingId;
                    currentDepart += startingDepart;

                    // Define os novos valores
                    vehicleElement.setAttribute("id", String.valueOf(currentId));
                    vehicleElement.setAttribute("depart", String.valueOf(currentDepart));
                }
            }

            // Salva as alterações em um novo arquivo
            saveXmlDocument(document, outputFile);

            System.out.println("Arquivo gerado com sucesso!");

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void saveXmlDocument(Document document, String filePath) throws TransformerException {
        // Salva o documento modificado em um novo arquivo
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }
}

