package fr.sorbonne_u.components.equipments.toaster;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.toaster.mil.ToasterStateModel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class ToasterInboundPort extends AbstractInboundPort implements ToasterUserCI {
	
	private static final long serialVersionUID = 1L;

	public ToasterInboundPort(ComponentI owner) throws Exception
	{
		super(ToasterUserCI.class, owner);
		assert owner instanceof ToasterImplementationI;
	}
	
	public ToasterInboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, ToasterUserCI.class, owner);
		assert	owner instanceof ToasterImplementationI;
	}
	
	@Override
	public ToasterStateModel.ToasterState getState() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((ToasterImplementationI)o).getState());
	}

	@Override
	public ToasterStateModel.ToasterBrowningLevel getBrowningLevel() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((ToasterImplementationI)o).getBrowningLevel());
	}

	@Override
	public void turnOn() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((ToasterImplementationI)o).turnOn();
						return null;
				});
	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((ToasterImplementationI)o).turnOff();
						return null;
				});
	}

	@Override
	public void setSliceCount(int sliceCount) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((ToasterImplementationI)o).setSliceCount(sliceCount);
						return null;
				});
	}

	@Override
	public void setBrowningLevel(ToasterStateModel.ToasterBrowningLevel bl) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((ToasterImplementationI)o).setBrowningLevel(bl);
						return null;
				});
	}

	@Override
	public int getSliceCount() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((ToasterImplementationI)o).getSliceCount());
	}

}
