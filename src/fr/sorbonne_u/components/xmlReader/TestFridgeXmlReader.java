package fr.sorbonne_u.components.xmlReader;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class TestFridgeXmlReader {
	
	@Test
	public void testUid() throws Exception {
		XMLReader xml = new XMLReader("fridge-descriptor.xml");
		String uid = xml.getUid();
		assert uid.equals("WaterHeater");
	}
	
	@Test
	public void testOffered() throws Exception {
		XMLReader xml = new XMLReader("waterheater-descriptor.xml");
		String offered = xml.getOffered();
		assert offered.equals("fr.sorbonne_u.components.hem2023.equipements.waterHeating.interfaces.WaterHeaterExternalControlCI");
	}
	
	@Test
	public void testConsumption() throws Exception {
		XMLReader xml = new XMLReader("waterheater-descriptor.xml");
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map = xml.getConsumption();
		assert map.get("nominal") ==  50;	
		assert map.get("min") == 45;
		assert map.get("max") == 60;
	}
	
	@Test
	public void testInstanceVar() throws Exception {
		XMLReader xml = new XMLReader("waterheater-descriptor.xml");
		ArrayList<Attribute> attributes = xml.getInstanceVar();
		ArrayList<Attribute> res = new ArrayList<Attribute>();
		
		res.add(new Attribute("protected static final", "int", "MAX_MODE", "2"));
		res.add(new Attribute("protected static final", "int", "MAX_TEMPERATURE", "60"));
		res.add(new Attribute("protected static final", "int", "MIN_TEMPERATURE", "45"));
		res.add(new Attribute("protected static final", "int", "INITIALISE_TEMPERATURE", "50"));
		res.add(new Attribute("protected", "boolean", "suspended", "false"));
		res.add(new Attribute("protected", "int", "currentMode", "0"));
		
		assert res.size() == attributes.size();
				
		for(int i = 0; i < res.size(); i++)
			assert res.get(i).equals(attributes.get(i));
	}
  	
	@Test
	public void printMethode() throws Exception {
		XMLReader xml = new XMLReader("fridge-descriptor.xml");
		ArrayList<Methode> methodes = xml.getMethode();
		System.out.println(methodes.get(3));
	}
}
