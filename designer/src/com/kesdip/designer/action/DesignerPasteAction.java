package com.kesdip.designer.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.kesdip.designer.editor.DesignerComponentEditPolicy;

@SuppressWarnings("restriction")
public class DesignerPasteAction extends SelectionAction {
	public static final String ID = "com.kesdip.designer.action.DesignerPasteAction";
	
	public DesignerPasteAction(IEditorPart editor) {
		this((IWorkbenchPart)editor);
	}
	
	public DesignerPasteAction(IEditorPart editor, String label) {
		this((IWorkbenchPart)editor);
		setText(label);
	}
	
	public DesignerPasteAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setLazyEnablementCalculation(false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		execute(createAddSelectionCommand((List) Clipboard.getDefault().getContents()));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean calculateEnabled() {
		Object o = Clipboard.getDefault().getContents();
		if (!(o instanceof List))
			return false;
		
		Command cmd = createAddSelectionCommand((List) o);
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}
	
	@SuppressWarnings("unchecked")
	private Command createAddSelectionCommand(List objects) {
		if (objects.isEmpty())
			return null;
		if (!(objects.get(0) instanceof EditPart))
			return null;

		GroupRequest pasteReq =
			new GroupRequest(DesignerComponentEditPolicy.REQ_PASTE);
		pasteReq.setEditParts(objects);
		Map map = new HashMap();
		map.put("context", getSelectedObjects());
		pasteReq.setExtendedData(map);

		CompoundCommand compoundCmd = new CompoundCommand("paste components");
		for (int i = 0; i < objects.size(); i++) {
			EditPart object = (EditPart) objects.get(i);
			Command cmd = object.getCommand(pasteReq);
			if (cmd != null) compoundCmd.add(cmd);
		}

		if (compoundCmd.size() != 0)
			return (Command) compoundCmd.getChildren()[0];
		return null;
	}

	/**
	 * Initializes this action's text and images.
	 */
	protected void init() {
		super.init();
		setText(GEFMessages.PasteAction_Label);
		setToolTipText(GEFMessages.PasteAction_Tooltip);
		setId(ID);
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_PASTE_DISABLED));
		setEnabled(false);
	}
}
