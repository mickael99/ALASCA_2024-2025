package fr.sorbonne_u.components.utils;

import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.devs_simulation.models.time.Duration;

public class Electricity {
	
	public static double toHours(Duration d) {
		long factor = 1;
		if (d.getTimeUnit() != TimeUnit.HOURS) 
			factor = d.getTimeUnit().convert(1, TimeUnit.HOURS);
		
		double ret = d.getSimulatedDuration()/factor;
		return ret;
	}

	public static double computeConsumption(Duration d, double i) {
		double h = toHours(d);
		return h*i/1000.0;
	}
}
