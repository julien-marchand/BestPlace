package gipad.configuration.configuration;

import java.util.ArrayList;

import gipad.configuration.ManagedElementList;
import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;


/**New Interface for a configuration
 * no more Set
 * @author Pocman
 *
 */
public interface Configuration {
	/**
     * Set a virtual machine running on a node. The node must already be online.
     * If the virtual machine is already in a other location or state in the configuration, it is updated
     *
     * @param vm   the virtual machine
     * @param node the node that will host the virtual machine. Must be considered as online.
     * @return true if the vm is assigned on the node. False otherwise
     */
    boolean setRunOn(VirtualMachine vm, Node node);

    /**
     * Set a virtual machine sleeping on a node.
     * If the virtual machine is already in a other location or state in the configuration, it is updated
     *
     * @param vm   the virtual machine
     * @param node the node that will host the virtual machine. Must be considered as online.
     * @return false if the hosting node is offline or unknown
     */
    boolean setSleepOn(VirtualMachine vm, Node node);

    /**
     * Set a virtual machine waiting.
     * If the virtual machine is already in a other location or state in the configuration, it is updated
     *
     * @param vm the virtual machine
     */
    void addWaiting(VirtualMachine vm);

    /**
     * Remove a virtual machine.
     *
     * @param vm the virtual machine to remove
     */
    void remove(VirtualMachine vm);

    /**
     * Remove a node. The node must not host any virtual machines
     *
     * @param n the node to remove
     * @return {@code true} if the node was removed. {@code false} otherwise
     */
    boolean remove(Node n);

    /**
     * Get the list of nodes that are online.
     *
     * @return a list, may be empty
     */
    ManagedElementList<Node> getOnlines();

    /**
     * Set a node online. If the node is already in the configuration but in an another state, it is updated.
     *
     * @param node the node to add
     */
    void addOnline(Node node);

	void addOnline(ManagedElementList<Node> nodes);

    /**
     * Set a node offline. If the node is already in the configuration but in an another state, it is updated.
     * The node must not host any virtual machines
     *
     * @param node the node
     * @return true if the node is offline. False otherwise
     */
    boolean addOffline(Node node);

    /**
     * Get the nodes that are offline.
     *
     * @return a list of nodes, may be empty
     */
    ManagedElementList<Node> getOfflines();


    /**
     * Get the virtual machines that are running.
     *
     * @return a list of VirtualMachines, may be empty
     */
    ManagedElementList<VirtualMachine> getRunnings();

    /**
     * Get the virtual machines that are sleeping.
     *
     * @return a list of virtual machines, may be empty
     */
    ManagedElementList<VirtualMachine> getSleepings();

    /**
     * Get the virtual machines that are sleeping on a node.
     *
     * @param n the node
     * @return a list of virtual machines, may be empty
     */
    ManagedElementList<VirtualMachine> getSleepings(Node n);

    /**
     * Get the virtual machines that are running on a node.
     *
     * @param n the node
     * @return a list of virtual machines, may be empty
     */
    ManagedElementList<VirtualMachine> getRunnings(Node n);

    /**
     * Get the location of a sleeping virtual machine.
     *
     * @param vm the virtual machine
     * @return its host, or null if the virtual machine is not defined as sleeping
     * @deprecated use {@link Configuration#getLocation(VirtualMachine vm)} instead
     */
    @Deprecated
    Node getSleepingLocation(VirtualMachine vm);

    /**
     * Get the virtual machines that are waiting.
     *
     * @return a list, may be empty
     */
    ManagedElementList<VirtualMachine> getWaitings();

    /**
     * Get all the virtual machines involved in the configuration.
     *
     * @return a list, may be empty
     */
    ManagedElementList<VirtualMachine> getAllVirtualMachines();

    /**
     * Get all the nodes involved in the configuration.
     *
     * @return a list, may be empty
     */
    ManagedElementList<Node> getAllNodes();

    /**
     * Return the node that host a running virtual machine.
     *
     * @param vm The VirtualMachine
     * @return The node that host the VirtualMachine or null if
     *         the virtual machine is not defined as running
     * @deprecated use {@link Configuration#getLocation(VirtualMachine vm)} instead
     */
    @Deprecated
    Node getRunningLocation(VirtualMachine vm);


    /**
     * Get the location of a  running or a sleeping virtual machine.
     *
     * @param vm the virtual machine
     * @return the node hosting the virtual machine or {@code null} is the virtual machine
     *         is not in the sleeping state nor the running state
     */
    Node getLocation(VirtualMachine vm);

    /**
     * Test if a node is online.
     *
     * @param n the node
     * @return true if the node is online
     */
    boolean isOnline(Node n);

    /**
     * Test if a node is offline.
     *
     * @param n the node
     * @return true if the node is offline
     */
    boolean isOffline(Node n);

    /**
     * Test if a virtual machine is running.
     *
     * @param vm the virtual machine
     * @return true if the virtual machine is running
     */
    boolean isRunning(VirtualMachine vm);

    /**
     * Test if a virtual machine is waiting.
     *
     * @param vm the virtual machine
     * @return true if the virtual machine is waiting
     */
    boolean isWaiting(VirtualMachine vm);

    /**
     * Test if a virtual machine is sleeping.
     *
     * @param vm the virtual machine
     * @return true if the virtual machine is sleeping
     */
    boolean isSleeping(VirtualMachine vm);

    /**
     * Get all the virtual machines running on a set of nodes.
     *
     * @param ns the set of nodes
     * @return a list of virtual machines, may be empty
     */
    ManagedElementList<VirtualMachine> getRunnings(ManagedElementList<Node> ns);

    /**
     * Shallow copy of the configuration.
     * The state and the assignment of the element are copied but elements are not duplicated.
     *
     * @return a copy of the configuration
     */
    Configuration clone();

    /**
     * Indicates if a node is included in the configuration.
     *
     * @param n the node
     * @return {@code true} if the node is in the configuration. {code false} otherwise
     */
    boolean contains(Node n);

    /**
     * Indicates if a virtual machine is included in the configuration.
     *
     * @param vm the virtual machine
     * @return {@code true} if the virtual machine is in the configuration. {code false} otherwise
     */
    boolean contains(VirtualMachine vm);

}
