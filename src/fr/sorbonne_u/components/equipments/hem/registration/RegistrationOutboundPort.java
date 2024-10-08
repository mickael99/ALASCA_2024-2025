package fr.sorbonne_u.components.equipments.hem.registration;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class RegistrationOutboundPort extends AbstractOutboundPort implements RegistrationCI {

	private static final long serialVersionUID = 1L;
	
	public RegistrationOutboundPort(ComponentI owner) throws Exception {
		super(RegistrationCI.class, owner);
	}
	
	public RegistrationOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RegistrationCI.class, owner);
	}
	
	@Override
	public boolean registered(String uid) throws Exception {
		return ((RegistrationCI)this.getConnector()).registered(uid);
	}

	@Override
	public boolean register(String uid, String controlPortURI, String path2xmlControlAdapter) throws Exception {
		return ((RegistrationCI)this.getConnector()).register(uid, controlPortURI, path2xmlControlAdapter);
	}

	@Override
	public void unregister(String uid) throws Exception {
		((RegistrationCI)this.getConnector()).unregister(uid);
	}

}
