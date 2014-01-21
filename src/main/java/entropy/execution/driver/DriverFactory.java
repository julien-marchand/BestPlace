/*
 * Copyright (c) 2010 Ecole des Mines de Nantes.
 *
 *      This file is part of Entropy.
 *
 *      Entropy is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Entropy is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.execution.driver;

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.plan.action.Action;
import entropy.plan.action.Migration;
import entropy.plan.action.Resume;
import entropy.plan.action.Run;
import entropy.plan.action.Shutdown;
import entropy.plan.action.Startup;
import entropy.plan.action.Stop;
import entropy.plan.action.Suspend;

/**
 * Fabric to create drivers associated to actions.
 * <br/>
 * Supported drivers are:
 * <ul>
 * <li>Action <b>migration</b>: {@link XenGuestMigration}, {@link SSHMigration}</li>
 * <li>Action <b>suspend</b>: {@link XenGuestSuspend}</li>
 * <li>Action <b>resume</b>: {@link XenGuestResume}</li>
 * <li>Action <b>startup</b>: {@link WoLStartup}</li>
 * <li>Action <b>shutdown</b>: {@link SSHShutdown}</li>
 * <li>Action <b>stop</b>: {@link SSHStop}</li>
 * <li>Action <b>run</b>: {@link SSHRun}</li>
 * </ul>
 * @see Driver
 * @author Fabien Hermenier
 *
 */
public class DriverFactory {
	
	/**
	 * The current properties of Entropy.
	 */
	private PropertiesHelper props;
	
	/**
	 * Create a new Factory.
	 * @param properties The properties used to create the factory
	 */
	public DriverFactory(PropertiesHelper properties) {
		this.props = properties;
	}
		
	/**
	 * Transform an action to a driver to execute it.
	 * @param action the action to transform
	 * @return a driver to perform the action
	 * @throws DriverInstantiationException if an error occured during the transformation
	 */
	public Driver transform(Action action) throws DriverInstantiationException {
		if (action instanceof Migration) {
			Migration m = (Migration) action;
			String drv = m.getHost().getMigrationDriverID();
            if ("xenapi".equals(drv)) {
                try {
				    return new XenGuestMigration((Migration) action, props);
                } catch (PropertiesHelperException e) {
                    throw new DriverInstantiationException(action, XenGuestMigration.class , e);
                }
			} else if ("sshMigration".equals(drv)) {
                try {
    				return new SSHMigration((Migration) action, props);
                } catch (PropertiesHelperException e) {
                    throw new DriverInstantiationException(action, SSHMigration.class, e);
                }
			}
		} else if (action instanceof Startup) {
			String drv = ((Startup) action).getNode().getStartupDriverID();
			if ("wakeOnLan".equals(drv)) {
				return new WoLStartup((Startup) action);
			}			
		} else if (action instanceof Shutdown) {
			String drv = ((Shutdown) action).getNode().getShutdownDriverID();
			if ("sshShutdown".equals(drv)) {
                try {
				    return new SSHShutdown((Shutdown) action, props);
                } catch (PropertiesHelperException e) {
                    throw new DriverInstantiationException(action, SSHShutdown.class , e);
                }                    
			}						
		} else if (action instanceof Suspend) {
			String drv = ((Suspend) action).getDestination().getSuspendDriverID();
			if ("xenapi".equals(drv)) {
                try {
				    return new XenGuestSuspend((Suspend) action, props);
                } catch (PropertiesHelperException e) {
                    throw new DriverInstantiationException(action, XenGuestSuspend.class, e);
                }
			}
		} else if (action instanceof Resume) {
			String drv = ((Resume) action).getDestination().getResumeDriverID();
			if ("xenapi".equals(drv)) {
                try {
				    return new XenGuestResume((Resume) action, props);
                } catch (PropertiesHelperException e) {
                    throw new DriverInstantiationException(action, XenGuestResume.class, e);
                }

			} else if ("sshResume".equals(drv)) {
                try {
				    return new SSHResume((Resume) action, props);
                } catch (PropertiesHelperException e) {
                    throw new DriverInstantiationException(action, SSHResume.class, e);
                }
			}
		} else if (action instanceof Run) {
			String drv = ((Run) action).getHost().getRunDriverID();
			if ("sshRun".equals(drv)) {
                try {
				    return new SSHRun((Run) action, props);
                } catch (PropertiesHelperException e) {
                    throw new DriverInstantiationException(action, SSHRun.class, e);
                }
			}
		} else if (action instanceof Stop) {
			String drv = ((Stop) action).getHost().getStopDriverID();
			if ("sshStop".equals(drv)) {
                try {
				    return new SSHStop((Stop) action, props);
                } catch (PropertiesHelperException e) {
                    throw new DriverInstantiationException(action, SSHStop.class, e);
                }                
			}			
		}
		throw new DriverInstantiationException(action);
	}
}
