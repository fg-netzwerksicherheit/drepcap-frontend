/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.eclipse.swt.widgets.Composite;

/**
 * 
 * Interface for a tab in the main window that required JMS connectivity.
 * 
 * @author Ruediger Gad
 *
 */
public abstract class TabWithJmsConnection extends Composite {

	public TabWithJmsConnection(Composite parent, int style) {
		super(parent, style);
	}
	
	public abstract void connectToJmsSensor(Connection connection) throws JMSException;

}
