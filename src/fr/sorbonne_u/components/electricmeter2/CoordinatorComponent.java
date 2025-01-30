package fr.sorbonne_u.components.electricmeter2;

import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;

public class			CoordinatorComponent
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static final String	REFLECTION_INBOUND_PORT_URI =
													"IRON-DRYER-COORDINATOR";

	public static boolean		VERBOSE = false;
	public static int			X_RELATIVE_POSITION = 0;
	public static int			Y_RELATIVE_POSITION = 0;

	protected			CoordinatorComponent()
	{
		super(REFLECTION_INBOUND_PORT_URI, 2, 0);

		if (VERBOSE) {
			this.tracer.get().setTitle("Global coordinator");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
}
// -----------------------------------------------------------------------------
