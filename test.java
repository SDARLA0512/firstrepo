package com.example.demo.controller;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
// import java.net.URLClassLoader;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@RestController
public class DataController {
    
    public final String SITE_IS_UP ="Site is up";
    public final String SITE_IS_DOWN ="Site is down";
    public final String INCORRECT_URL ="Incorrect URL";

    @GetMapping("/check")
    public String getURLStatusMessage(@RequestParam String Url){
        String returnMessage = "";
        try {
            URL UrlObject = new URL(Url);
            HttpURLConnection connection = (HttpURLConnection) UrlObject.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() > 301)
                returnMessage = SITE_IS_DOWN;
            else
                returnMessage = SITE_IS_UP;    
        } catch (MalformedURLException e) {
            returnMessage = INCORRECT_URL;            
        } catch (IOException e) {
            returnMessage = SITE_IS_DOWN;
        }        
        return returnMessage;
    }

    public static Document loadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public ResponseEntity<String> getAPI(String processkey){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl = "http://localhost:8080/engine-rest/process-definition/key/";
        ResponseEntity<String> response= restTemplate.getForEntity(fooResourceUrl + "/"+processkey+"/xml", String.class);
        return response;
    }

    @GetMapping("/processdata")
    public ResponseEntity<List<String>> getAll(@RequestParam String processkey) throws Exception {
        // RestTemplate restTemplate = new RestTemplate();
        // String fooResourceUrl = "http://localhost:8080/engine-rest/process-definition/key/";
        // ResponseEntity<String> response= restTemplate.getForEntity(fooResourceUrl + "/"+processkey+"/xml", String.class);
        ResponseEntity<String> response = getAPI(processkey);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode name = root.path("bpmn20Xml");
        String content  = name.textValue();
        Document bpmn = loadXMLFromString(content);
        bpmn.getDocumentElement().normalize();
        NodeList nodeList = bpmn.getElementsByTagName("bpmn:userTask");
        ArrayList<String> UserTaskList = new ArrayList<String>();
        for (int temp = 0; temp < nodeList.getLength(); temp++) {
            Node nNode = nodeList.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                UserTaskList.add(eElement.getAttribute("name"));
            }
        }
        // System.out.println(UserTaskList);
        return ResponseEntity.ok(UserTaskList);
    }
}
