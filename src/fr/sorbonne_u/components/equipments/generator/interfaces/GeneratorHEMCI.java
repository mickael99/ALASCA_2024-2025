package fr.sorbonne_u.components.equipments.generator.interfaces;

import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface GeneratorHEMCI extends RequiredCI {
	
	public boolean isRunning() throws Exception;
	public void activate() throws Exception;
	public void stop() throws Exception;
}
