package fr.sorbonne_u.components.xmlReader;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLReader {
	protected String xmlFilePath;
	private Element rootElement;
	
	public XMLReader(String xmlFilePath) throws Exception {
		this.xmlFilePath = xmlFilePath;
		
		//chargement du fichier
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFilePath);
        this.rootElement = doc.getDocumentElement();
	}
	
	public String getUid() {
		return rootElement.getAttribute("uid");
	}
	
	public String getOffered() {
		return rootElement.getAttribute("offered");
	}
	
	public HashMap<String, Integer> getConsumption() {
		HashMap<String, Integer> res = new HashMap<String, Integer>();
		NodeList consumptionList = rootElement.getElementsByTagName("consumption");
        Element consumption = (Element)consumptionList.item(0);

        res.put("nominal", Integer.parseInt(consumption.getAttribute("nominal")));
        res.put("min", Integer.parseInt(consumption.getAttribute("min")));
        res.put("max", Integer.parseInt(consumption.getAttribute("max")));
        
        return res;
	}
	
	public ArrayList<Attribute> getInstanceVar() {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		
		NodeList attributesList = rootElement.getElementsByTagName("instance-var");
		for(int i = 0; i < attributesList.getLength(); i++) {
			Element attribut = (Element)attributesList.item(i);
			attributes.add(
					new Attribute(attribut.getAttribute("modifiers"), 
									attribut.getAttribute("type"),
									attribut.getAttribute("name"),
									attribut.getAttribute("static-init")));
		}
		
		return attributes;
	}
	
	protected ArrayList<Methode> getInternalMethodes() {
		ArrayList<Methode> internalMethodes = new ArrayList<Methode>();
		
		NodeList methodesList = rootElement.getElementsByTagName("internal");
		
		for(int i = 0; i < methodesList.getLength(); i++) {
			Element methode = (Element)methodesList.item(i);
			
			//il est possible que la methode lance une exception
			NodeList thrownElements = methode.getElementsByTagName("thrown");
			String thrownContent = "";
		    if (thrownElements.getLength() > 0) 
		        thrownContent = thrownElements.item(0).getTextContent();
		    
		    //il est possible qu'il y ait des parametres
		    ArrayList<Parameter> parameterArray = new ArrayList<Parameter>();
		    NodeList parametersElement = methode.getElementsByTagName("parameter");
		    for(int j = 0; j < parametersElement.getLength(); j++) {
		    	Element parameter = (Element)parametersElement.item(j);
		    	parameterArray.add(new Parameter(parameter.getAttribute("type"),
		    			parameter.getAttribute("name")));
		    }
		    internalMethodes.add(
					new Methode(methode.getAttribute("modifiers"), 
									methode.getAttribute("type"),
									methode.getAttribute("name"),
									thrownContent,
									methode.getElementsByTagName("body").item(0).getTextContent(),
									parameterArray));
		}
		
		return internalMethodes;
	}
	
	protected ArrayList<Methode> getServiceMethodes() {
		ArrayList<Methode> serviceMethodes = new ArrayList<Methode>();
		
		String[] methodTags = {"upMode", "downMode", "setMode", "currentMode", "suspended", "suspend", "resume", "emergency"};
	    
	    for(String tag : methodTags) {
	        NodeList methodesList = rootElement.getElementsByTagName(tag);
	        
	        for(int i = 0; i < methodesList.getLength(); i++) {
	            Element methode = (Element)methodesList.item(i);
	            
	            NodeList thrownElements = methode.getElementsByTagName("thrown");
	            String thrownContent = "";
	            if (thrownElements.getLength() > 0) 
	                thrownContent = thrownElements.item(0).getTextContent();
	            
	            ArrayList<Parameter> parameterArray = new ArrayList<Parameter>();
	            NodeList parametersElement = methode.getElementsByTagName("parameter");
	            for(int j = 0; j < parametersElement.getLength(); j++) {
	                Element parameter = (Element)parametersElement.item(j);
	                parameterArray.add(new Parameter(parameter.getAttribute("type"),
	                        parameter.getAttribute("name")));
	            }
	            
	            serviceMethodes.add(
	                    new Methode(
	                        "protected", 
	                        methode.getAttribute("type"),
	                        tag,
	                        thrownContent,
	                        methode.getElementsByTagName("body").item(0).getTextContent(),
	                        parameterArray
	                    )
	            );
	        }
	    }
		
		return serviceMethodes;
	}

	public ArrayList<Methode> getMethode() {
		ArrayList<Methode> methodes = new ArrayList<Methode>();
		
		methodes = this.getInternalMethodes();
		methodes.addAll(this.getServiceMethodes());
		
		return methodes;
	}
}
