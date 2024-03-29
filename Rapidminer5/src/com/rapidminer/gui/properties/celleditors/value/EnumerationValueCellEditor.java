/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by RapidMiner and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapidminer.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.properties.celleditors.value;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;

import com.rapidminer.gui.properties.EnumerationPropertyDialog;
import com.rapidminer.gui.properties.ListPropertyDialog;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeEnumeration;


/**
 * A cell editor with a button that opens a {@link ListPropertyDialog}. Values
 * generated by this operator are Lists of String pairs.
 * 
 * @see com.rapidminer.gui.properties.ListPropertyDialog
 * @author Simon Fischer, Ingo Mierswa
 */
public class EnumerationValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

	private static final long serialVersionUID = -4429790999365057931L;

	private ParameterTypeEnumeration type;

	private JButton button;

	private List<String> valuesList = new LinkedList<String>();

	public EnumerationValueCellEditor(ParameterTypeEnumeration type) {
		this.type = type;
	}

	public void setOperator(final Operator operator) {
		button = new JButton(new ResourceAction(true, "enumeration") {
			private static final long serialVersionUID = 3163983383358779866L;

			public void actionPerformed(ActionEvent e) {
				EnumerationPropertyDialog dialog = new EnumerationPropertyDialog(type, valuesList, operator);
				dialog.setVisible(true);
				if (dialog.isOk()) {                    
					fireEditingStopped();
					setButtonText();
				} else {
					fireEditingCanceled();
				}
			}			
		});
		button.setMargin(new Insets(0, 0, 0, 0));
//		button.setToolTipText(type.getDescription());
		setButtonText();
	}

	public Object getCellEditorValue() {
		return ParameterTypeEnumeration.transformEnumeration2String(valuesList);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) { 
		if (value != null) {
			LinkedList<String> values = new LinkedList<String>();
			for (String string: ParameterTypeEnumeration.transformString2Enumeration((String)value)) {
				values.add(string);
			}
			this.valuesList = values;
		} else
			this.valuesList = new LinkedList<String>();
		setButtonText();
		return button;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	public boolean useEditorAsRenderer() {
		return true;
	}

	// TODO: externalize button text
	private void setButtonText() {
		button.setText("Edit Enumeration (" + valuesList.size() + ")...");
	}

	@Override
	public boolean rendersLabel() {
		return false;
	}

}
