package fr.sorbonne_u.components.xmlReader;

public class TestFridgeClassCreator {
	
	protected static final String PATH_XML = "fridge-descriptor.xml";
	
	public TestFridgeClassCreator() throws Exception {
		ClassCreator classCreator = new ClassCreator(PATH_XML);
		Class<?> classConnector = classCreator.createClass();
	}
	
	public static void main(String[] args) {
		try {
			new TestFridgeClassCreator();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
