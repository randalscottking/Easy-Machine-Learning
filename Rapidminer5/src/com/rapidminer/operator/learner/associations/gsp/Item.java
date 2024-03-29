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
package com.rapidminer.operator.learner.associations.gsp;

import java.io.Serializable;

/**
 * @author Sebastian Land
 *
 */
public class Item implements Comparable<Item>, Serializable {

	private static final long serialVersionUID = 34234L;
	private String name;
	private int index;
	
	public Item(String name, int i) {
		this.name = name;
		this.index = i;
	}
	
	@Override
	public int hashCode() {
		return index;
	}

	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Item o) {
		return Integer.signum(index - o.index);
	}
}
