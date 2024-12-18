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
	public boolean isTurnOn() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((IronImplementationI)o).isTurnOn());
	}

	@Override
	public void setState(IronState s) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).setState(s);
						return null;
				});
	}

	@Override
	public boolean isSteamModeEnable() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((IronImplementationI)o).isSteamModeEnable());
	}

	@Override
	public void EnableSteamMode() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).EnableSteamMode();
						return null;
				});
	}

	@Override
	public void DisableSteamMode() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).DisableSteamMode();
						return null;
				});
	}

	@Override
	public boolean isEnergySavingModeEnable() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((IronImplementationI)o).isEnergySavingModeEnable());
	}

	@Override
	public void EnableEnergySavingMode() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).EnableEnergySavingMode();
						return null;
				});
	}

	@Override
	public void DisableEnergySavingMode() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((IronImplementationI)o).DisableEnergySavingMode();
						return null;
				});
	}

}
