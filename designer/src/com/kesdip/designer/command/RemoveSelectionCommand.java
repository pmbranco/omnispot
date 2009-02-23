package com.kesdip.designer.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import com.kesdip.designer.model.ComponentModelElement;
import com.kesdip.designer.model.Deployment;
import com.kesdip.designer.model.Layout;
import com.kesdip.designer.model.ModelElement;
import com.kesdip.designer.model.Region;

public class RemoveSelectionCommand extends Command {
	/** parent elements */
	private Map<ModelElement, ModelElement> parents;
	/** Elements to delete */
	@SuppressWarnings("unchecked")
	private final List elements;
	/** True, if child was removed from its parent. */
	private boolean wasRemoved;
	
	@SuppressWarnings("unchecked")
	public RemoveSelectionCommand(List elements) {
		setLabel("group removal");
		if (elements == null || elements.size() == 0) {
			throw new IllegalArgumentException();
		}
		setLabel("element(s) deletion");
		this.elements = new ArrayList();
		for (Object o : elements) {
			EditPart editPart = (EditPart) o;
			ModelElement elem = (ModelElement) editPart.getModel();
			this.elements.add(elem);
		}
		this.wasRemoved = false;
		this.parents = new HashMap<ModelElement, ModelElement>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	public boolean canUndo() {
		return wasRemoved;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		redo();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		// remove the elements
		boolean allOK = true;
		parents.clear();
		for (Object o : elements)  {
			ModelElement child = (ModelElement) o;
			ModelElement parent = child.getDeployment().removeChild(child);
			if (parent == null) 
				allOK = false;
			else
				parents.put(child, parent);
		}
		wasRemoved = allOK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		// add the elements
		for (Object o : elements) {
			ModelElement child = (ModelElement) o;
			ModelElement parent = parents.get(child);
			if (parent instanceof Deployment)
				((Deployment) parent).addLayout((Layout) child);
			else if (parent instanceof Layout)
				((Layout) parent).addRegion((Region) child);
			else if (parent instanceof Region)
				((Region) parent).addComponent((ComponentModelElement) child);
			else
				throw new RuntimeException("Unexpected parent: " +
						parent.getClass().getName());
		}
	}

}
