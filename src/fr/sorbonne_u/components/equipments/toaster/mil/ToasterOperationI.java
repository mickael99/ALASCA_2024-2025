package fr.sorbonne_u.components.equipments.toaster.mil;

import fr.sorbonne_u.devs_simulation.models.time.Time;

public interface ToasterOperationI {
    public void setToasterState(ToasterStateModel.ToasterState state, Time t);

    public void setToasterBrowningLevel(ToasterStateModel.ToasterBrowningLevel bl, Time t);

    public ToasterStateModel.ToasterState getToasterState();

    public ToasterStateModel.ToasterBrowningLevel getToasterBrowningLevel() throws Exception;

}