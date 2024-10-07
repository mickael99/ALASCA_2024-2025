package fr.sorbonne_u.components.equipments.iron;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class IronInboundPort extends AbstractInboundPort implements IronUserCI {

	private static final long serialVersionUID = 1L;

	public IronInboundPort(ComponentI owner) throws Exception
	{
		super(IronUserCI.class, owner);
		assert owner instanceof IronImplementationI;
	}
	
	public IronInboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, IronUserCI.class, owner);
		assert	owner instanceof IronImplementationI;
	}
	
	@Override
	public IronState getState() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((IronImplementationI)o).getState());
	}

	@Override
	public void turnOn() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).turnOn();
						return null;
				});
	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).turnOff();
						return null;
				});
	}

	@Override
	public IronTemperature getTemperature() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((IronImplementationI)o).getTemperature());
	}
;
	@Override
	public void setTemperature(IronTemperature t) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).setTemperature(t);
						return null;
				});
	}

	@Override
	public IronSteam getSteam() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((IronImplementationI)o).getSteam());
	}

	@Override
	public void setSteam(IronSteam s) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).setSteam(s);
						return null;
				});
	}

	@Override
	public IronEnergySavingMode getEnergySavingMode() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((IronImplementationI)o).getEnergySavingMode());
	}

	@Override
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).setEnergySavingMode(e);
						return null;
				});
	}

}
