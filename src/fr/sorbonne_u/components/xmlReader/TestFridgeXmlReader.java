package fr.sorbonne_u.components.xmlReader;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class TestFridgeXmlReader {
	
	@Test
	public void testUid() throws Exception {
		XMLReader xml = new XMLReader("fridge-descriptor.xml");
		String uid = xml.getUid();
		System.out.println(uid);
		assert uid.equals("FridgeRegistration");
	}
	
	@Test
	public void testOffered() throws Exception {
		XMLReader xml = new XMLReader("fridge-descriptor.xml");
		String offered = xml.getOffered();
		assert offered.equals("fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeExternalControlCI");
	}
	
	@Test
	public void testConsumption() throws Exception {
		XMLReader xml = new XMLReader("fridge-descriptor.xml");
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map = xml.getConsumption();
		assert map.get("nominal") ==  500;	
		assert map.get("min") == 0;
		assert map.get("max") == 500;
	}
	
	@Test
	public void testInstanceVar() throws Exception {
		XMLReader xml = new XMLReader("fridge-descriptor.xml");
		ArrayList<Attribute> attributes = xml.getInstanceVar();
		ArrayList<Attribute> res = new ArrayList<Attribute>();
		
		res.add(new Attribute("protected static", "int", "MAX_MODE", "3.0"));
		res.add(new Attribute("protected static", "double", "MIN_ADMISSIBLE_TEMP", "0"));
		res.add(new Attribute("protected static", "double", "MAX_ADMISSIBLE_DELTA", "5.0"));
		res.add(new Attribute("protected", "int", "currentMode", "MAX_MODE"));
		res.add(new Attribute("protected", "boolean", "isSuspended", "false"));
		
		assert res.size() == attributes.size();
				
		for(int i = 0; i < res.size(); i++) 
			assert res.get(i).equals(attributes.get(i));
	}
  	
	@Test
	public void printMethode() throws Exception {
		XMLReader xml = new XMLReader("fridge-descriptor.xml");
		ArrayList<Methode> methodes = xml.getMethode();
		for(Methode m : methodes)
			System.out.println(m.getName());
	}
}
