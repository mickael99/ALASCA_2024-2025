package fr.sorbonne_u.components.equipments.toaster;

import fr.sorbonne_u.components.connectors.AbstractConnector;

public class ToasterConnector extends AbstractConnector implements ToasterUserCI {

	@Override
	public ToasterState getState() throws Exception {
		return ((ToasterUserCI)this.offering).getState();
	}

	@Override
	public ToasterBrowningLevel getBrowningLevel() throws Exception {
		return ((ToasterUserCI)this.offering).getBrowningLevel();
	}
	
	@Override
	public int getSliceCount() throws Exception {
		return ((ToasterUserCI)this.offering).getSliceCount();
	}

	@Override
	public void turnOn() throws Exception {
		((ToasterUserCI)this.offering).turnOn();
	}

	@Override
	public void turnOff() throws Exception {
		((ToasterUserCI)this.offering).turnOff();
	}

	@Override
	public void setSliceCount(int sliceCount) throws Exception {
		((ToasterUserCI)this.offering).setSliceCount(sliceCount);
	}

	@Override
	public void setBrowningLevel(ToasterBrowningLevel bl) throws Exception {
		((ToasterUserCI)this.offering).setBrowningLevel(bl);
	}

}
