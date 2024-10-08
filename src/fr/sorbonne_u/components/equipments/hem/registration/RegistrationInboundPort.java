package fr.sorbonne_u.components.equipments.hem.registration;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class RegistrationInboundPort extends AbstractInboundPort implements RegistrationCI {

	private static final long serialVersionUID = 1L;

	public RegistrationInboundPort(ComponentI owner) throws Exception{
		super(RegistrationCI.class, owner);	
		assert owner instanceof RegistrationI;
	}
	
	public RegistrationInboundPort(String uri,ComponentI owner) throws Exception{
		super(uri, RegistrationCI.class, owner);
		assert owner instanceof RegistrationI;
	}
	
	@Override
	public boolean registered(String uid) throws Exception {
		return this.getOwner().handleRequest(
				o -> ((RegistrationI)o).registered(uid));
	}

	@Override
	public boolean register(String uid, String controlPortURI, String path2xmlControlAdapter) throws Exception {
		return this.getOwner().handleRequest(
				o -> ((RegistrationI)o).register(uid, controlPortURI, path2xmlControlAdapter));
	}

	@Override
	public void unregister(String uid) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((RegistrationI)o).unregister(uid);
						return null;
				});
	}

}
