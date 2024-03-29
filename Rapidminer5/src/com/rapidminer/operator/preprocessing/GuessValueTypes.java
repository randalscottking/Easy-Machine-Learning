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
package com.rapidminer.operator.preprocessing;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.annotation.ResourceConsumptionEstimator;
import com.rapidminer.operator.preprocessing.filter.NominalNumbers2Numerical;
import com.rapidminer.operator.tools.AttributeSubsetSelector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorResourceConsumptionHandler;
import com.rapidminer.tools.Tools;

/**
 * This operator can be used to (re-)guess the value types of all attributes. This
 * might be useful after some preprocessing transformations and &quot;purifying&quot;
 * some of the columns, especially if columns which were nominal before can be handled
 * as numerical columns. With this operator, the value types of all attributes do not
 * have to be transformed manually with operators like {@link NominalNumbers2Numerical}.
 *
 * @author Ingo Mierswa
 */
public class GuessValueTypes extends AbstractDataProcessing {

    /** The parameter name for &quot;Character that is used as decimal point.&quot; */
    public static final String PARAMETER_DECIMAL_POINT_CHARACTER = "decimal_point_character";

    /** The parameter name for &quot;Character that is used as decimal point.&quot; */
    public static final String PARAMETER_NUMBER_GROUPING_CHARACTER = "number_grouping_character";

    private AttributeSubsetSelector attributeSelector = new AttributeSubsetSelector(this, getExampleSetInputPort());

    public GuessValueTypes(OperatorDescription description) {
        super(description);
    }

    @Override
    public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
        // init
        char decimalPointCharacter = getParameterAsString(PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0);
        Character groupingCharacter = null;
        if (isParameterSet(PARAMETER_NUMBER_GROUPING_CHARACTER))
            groupingCharacter = getParameterAsString(PARAMETER_NUMBER_GROUPING_CHARACTER).charAt(0);

        Set<Attribute> attributeSet = attributeSelector.getAttributeSubset(exampleSet, false);
        int size = attributeSet.size();

        int[] valueTypes = new int[size];

        int index = 0;
        for (Attribute attribute: attributeSet) {
            valueTypes[index++] = attribute.getValueType();
        }

        // guessing
        boolean[] checked = new boolean[valueTypes.length];
        int checkedCounter = 0;
        for (Example example : exampleSet) {
            index = 0;
            for (Attribute attribute: attributeSet) {
                if (!attribute.isNominal() && !attribute.isNumerical())
                    continue;

                double originalValue = example.getValue(attribute);
                if (!Double.isNaN(originalValue)) {
                    if (!checked[index]) {
                        try {
                            String valueString = example.getValueAsString(attribute);
                            if (!(Attribute.MISSING_NOMINAL_VALUE.equals(valueString))) {
                                if (groupingCharacter != null) {
                                    valueString = valueString.replace(groupingCharacter.toString(), "");
                                }
                                valueString = valueString.replace(decimalPointCharacter, '.');
                                double value = Double.parseDouble(valueString);
                                if (Tools.isEqual(Math.round(value), value)) {
                                    valueTypes[index] = Ontology.INTEGER;
                                } else {
                                    valueTypes[index] = Ontology.REAL;
                                }
                            }
                        } catch (NumberFormatException e) {
                            valueTypes[index] = Ontology.NOMINAL;
                            checked[index] = true;
                            checkedCounter++;
                        }
                    }
                }
                index++;
            }
            if (checkedCounter >= checked.length) {
                break;
            }
        }


        // new attributes
        List<AttributeRole> newAttributes = new LinkedList<AttributeRole>();
        index = 0;
        for (Attribute attribute: attributeSet) {
            if (!attribute.isNominal() && !attribute.isNumerical())
                continue;

            AttributeRole role = exampleSet.getAttributes().getRole(attribute);

            Attribute newAttribute = AttributeFactory.createAttribute(valueTypes[index]);
            exampleSet.getExampleTable().addAttribute(newAttribute);
            AttributeRole newRole = new AttributeRole(newAttribute);
            newRole.setSpecial(role.getSpecialName());
            newAttributes.add(newRole);

            // copy data
            for (Example e : exampleSet) {
                double oldValue = e.getValue(attribute);
                if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueTypes[index], Ontology.NUMERICAL)) {
                    if (!Double.isNaN(oldValue)) {
                        String valueString = e.getValueAsString(attribute);
                        if (Attribute.MISSING_NOMINAL_VALUE.equals(valueString)) {
                            e.setValue(newAttribute, Double.NaN);
                        } else {
                            if (groupingCharacter != null) {
                                valueString = valueString.replace(groupingCharacter.toString(), "");
                            }
                            valueString = valueString.replace(decimalPointCharacter, '.');
                            e.setValue(newAttribute, Double.parseDouble(valueString));
                        }
                    } else {
                        e.setValue(newAttribute, Double.NaN);
                    }
                } else {
                    if (!Double.isNaN(oldValue)) {
                        String value = e.getValueAsString(attribute);
                        e.setValue(newAttribute, newAttribute.getMapping().mapString(value));
                    } else {
                        e.setValue(newAttribute, Double.NaN);
                    }
                }
            }

            // delete attribute and rename the new attribute (due to deletion and data scans: no more memory used :-)
            exampleSet.getExampleTable().removeAttribute(attribute);
            exampleSet.getAttributes().remove(role);
            newAttribute.setName(attribute.getName());

            index++;
        }

        for (AttributeRole role : newAttributes) {
            if (role.isSpecial()) {
                exampleSet.getAttributes().setSpecialAttribute(role.getAttribute(), role.getSpecialName());
            } else {
                exampleSet.getAttributes().addRegular(role.getAttribute());
            }
        }

        return exampleSet;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.addAll(attributeSelector.getParameterTypes());
        types.add(new ParameterTypeString(PARAMETER_DECIMAL_POINT_CHARACTER, "Character that is used as decimal point.", ".", false));
        types.add(new ParameterTypeString(PARAMETER_NUMBER_GROUPING_CHARACTER, "Character that is used as the number grouping character, i.e. for groups of thousands.", true));
        return types;
    }

    @Override
    public boolean writesIntoExistingData() {
        //TODO: Can be changed if attribute would not be removed from table.
        return true;
    }

    @Override
    public ResourceConsumptionEstimator getResourceConsumptionEstimator() {
        return OperatorResourceConsumptionHandler.getResourceConsumptionEstimator(getInputPort(), GuessValueTypes.class, attributeSelector);
    }
}
