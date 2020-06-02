package org.wso2.carbon.user.core.soap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SoapClient {

    private static void createSoapEnvelope(SOAPMessage soapMessage, String username, String credential) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespace = "tem";
        String myNamespaceURI = "http://tempuri.org/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("Authentication", myNamespace);

        SOAPElement usernameId = soapBodyElem.addChildElement("userName", myNamespace);
        usernameId.addTextNode(username);

        SOAPElement credentialE = soapBodyElem.addChildElement("passWord", myNamespace);
        credentialE.addTextNode(credential);

        SOAPElement authorE = soapBodyElem.addChildElement("author", myNamespace);
        authorE.addTextNode("AICLTKNVPUBND08112019");
    }

    public static boolean callSoapWebService(String soapEndpointUrl, String soapAction, String username, String credential) {
        boolean authenticated = false;
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            SOAPMessage soapRequest = createSOAPRequest(soapAction, username, credential);
            SOAPMessage soapResponse = soapConnection.call(soapRequest, soapEndpointUrl);

            if(soapResponse != null){
                List<String> ids = getTokenIdFromXml(soapResponse.toString(), "AuthenticationResponse");
                if(ids.size() > 0){
                    authenticated = true;
                }
            }
        } catch (Exception e) {
            log.error("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n" + e.getMessage());
        }
        return authenticated;
    }

    private static SOAPMessage createSOAPRequest(String soapAction, String username, String credential) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        createSoapEnvelope(soapMessage, username, credential);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);

        soapMessage.saveChanges();

        /* Print the request message, just for debugging purposes */
        System.out.println("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println("\n");

        return soapMessage;
    }

    public static Document loadXMLString(String response) throws Exception
    {
        DocumentBuilderFactory dbf =DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(response));

        return db.parse(is);
    }

    public static List<String> getTokenIdFromXml(String response, String tagName) throws Exception {
        Document xmlDoc = loadXMLString(response);
        NodeList nodeList = xmlDoc.getElementsByTagName(tagName);
        List<String> ids = new ArrayList<>(nodeList.getLength());
        for(int i=0;i<nodeList.getLength(); i++) {
            Node x = nodeList.item(i);
            ids.add(x.getFirstChild().getNodeValue());
            System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
        }
        return ids;
    }

    private static Log log = LogFactory.getLog(SoapClient.class);
}
