/*
 * Disclaimer:
 * Copyright 2008 - Ke.S.Di.P. E.P.E - All rights reserved.
 * eof Disclaimer
 */
package com.kesdip.player.components;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;

import com.kesdip.player.DeploymentLayout.CompletionStatus;

/**
 * A simple container component.
 * 
 * @author Pafsanias Ftakas
 */
public class SimpleContainer extends AbstractComponent {
	
	protected List<Component> contents;

	public void setContents(List<Component> contents) {
		this.contents = contents;
	}

	/* TRANSIENT STATE */
	private JPanel panel;
	
	@Override
	public void init(Component parent) throws ComponentException {
		panel = new JPanel();
		panel.setLocation(x, y);
		if (backgroundColor != null)
			panel.setBackground(backgroundColor);
		else
			panel.setOpaque(false);
		panel.setSize(new Dimension(width, height));
		panel.setPreferredSize(new Dimension(width, height));
		
		for (Component component : contents) {
			component.init(this);
		}

		parent.add(this);
	}

	@Override
	public void add(Component component) throws ComponentException {
		java.awt.Component windowComponent = component.getWindowComponent();
		if (windowComponent == null)
			return;
		
		panel.add(windowComponent);
	}

	@Override
	public java.awt.Component getWindowComponent() {
		return panel;
	}

	@Override
	public void repaint() throws ComponentException {
		for (Component component : contents) {
			component.repaint();
		}
	}

	@Override
	public CompletionStatus isComplete() {
		for (Component component : contents) {
			switch (component.isComplete()) {
			case COMPLETE:
				return CompletionStatus.COMPLETE;
			case INCOMPLETE:
				return CompletionStatus.INCOMPLETE;
			case DONT_CARE:
				// Do nothing
				break;
			default:
				throw new RuntimeException("Unexpected completion " +
						"state: " + component.isComplete());
			}
		}
		
		return CompletionStatus.DONT_CARE;
	}

	@Override
	public void releaseResources() {
		for (Component component : contents) {
			component.releaseResources();
		}
	}

}
