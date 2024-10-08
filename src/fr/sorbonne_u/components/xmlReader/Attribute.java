package fr.sorbonne_u.components.xmlReader;

public class Attribute {
	private String modifiers;
	private String type;
	private String name;
	private String initValue;
	
	public Attribute(String modifiers, String type, String name, String initValue ) {
		this.modifiers = modifiers;
		this.type = type;
		this.name = name;
		this.initValue = initValue;
	}
	
	public String getModifiers() {
		return modifiers;
	}
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getInitValue() {
		return initValue;
	}
	
	public boolean equals(Attribute a) {
		if(a.modifiers.equals(this.modifiers) && 
			a.type.equals(this.type) && 
			a.name.equals(this.name) && 
			a.initValue.equals(this.initValue))
				return true;
		
		return false;	
	}
	
	@Override
	public String toString() {
		return "modifiers -> " + modifiers + " type -> " + type + " name -> " + name + " initValue -> " + initValue + "\n\n";
	}
}