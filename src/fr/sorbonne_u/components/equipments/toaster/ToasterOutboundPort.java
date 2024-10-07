package fr.sorbonne_u.components.equipments.toaster;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class ToasterOutboundPort extends AbstractOutboundPort implements ToasterUserCI {

	private static final long serialVersionUID = 1L;

	public	ToasterOutboundPort(ComponentI owner) throws Exception {
		super(ToasterUserCI.class, owner);
	}

	public ToasterOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ToasterUserCI.class, owner);
	}
			
	@Override
	public ToasterState getState() throws Exception {
		return ((ToasterUserCI)this.getConnector()).getState();
	}
	
	@Override
	public int getSliceCount() throws Exception {
		return ((ToasterUserCI)this.getConnector()).getSliceCount();
	}

	@Override
	public ToasterBrowningLevel getBrowningLevel() throws Exception {
		return ((ToasterUserCI)this.getConnector()).getBrowningLevel();
	}

	@Override
	public void turnOn() throws Exception {
		((ToasterUserCI)this.getConnector()).turnOn();
	}

	@Override
	public void turnOff() throws Exception {
		((ToasterUserCI)this.getConnector()).turnOff();
	}

	@Override
	public void setSliceCount(int sliceCount) throws Exception {
		((ToasterUserCI)this.getConnector()).setSliceCount(sliceCount);
	}

	@Override
	public void setBrowningLevel(ToasterBrowningLevel bl) throws Exception {
		((ToasterUserCI)this.getConnector()).setBrowningLevel(bl);
	}

}
