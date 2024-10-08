package fr.sorbonne_u.components.xmlReader;

import java.util.ArrayList;

public class Methode {
	private String modifiers;
	private String type;
	private String name;
	private ArrayList<Parameter> parameterArray;
	private String thrown;
	private String body;
	
	public Methode(String modifiers, String type, String name, String thrown, String body, ArrayList<Parameter> parameterArray) {
		this.modifiers = modifiers;
		this.type = type;
		this.name = name;
		this.thrown = thrown;
		this.body = body;
		this.parameterArray = new ArrayList<Parameter>(parameterArray);
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
	
	public String getThrown() {
		return thrown;
	}
	
	public String getBody() {
		return body;
	}
	
	public ArrayList<Parameter> getParameters() {
		return parameterArray;
	}
	
	public boolean equals(Methode m) {
		if(m.modifiers.equals(this.modifiers) &&
			m.type.equals(this.type) && 
			m.name.equals(this.name) && 
			m.thrown.equals(this.thrown) && 
			m.body.equals(this.body))
				return true;
		
		return false;
	}
	
	public String toString() {
		String res = new String();
		if(parameterArray.size() > 0) {
			res = "parametres -> ";
			for(Parameter p : parameterArray)
				res += p.toString();
			res += "\n";
		}
		return res + "modifiers -> \"" + modifiers + "\" type -> \"" + type + "\" name -> \"" + 
				name + "\" thrown -> \"" + thrown + "\" body ->\n\"" + body + "\"\n\n";
	}
}
