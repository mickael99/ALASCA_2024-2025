package fr.sorbonne_u.components.equipments.windTurbine;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface WindTurbineCI extends OfferedCI, RequiredCI, WindTurbineI {

	public boolean isActivate() throws Exception;
	
	public void activate() throws Exception;
	public void stop() throws Exception;
}
