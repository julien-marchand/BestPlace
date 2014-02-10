package gipad.configuration.configuration;

import gipad.tools.ManagedElementList;

public interface Node {
	ManagedElementList<Core> getCores();
	int getId();
	Object name();
}
