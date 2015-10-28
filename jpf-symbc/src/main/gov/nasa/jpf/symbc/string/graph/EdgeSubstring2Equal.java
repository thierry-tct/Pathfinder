/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package gov.nasa.jpf.symbc.string.graph;

import gov.nasa.jpf.symbc.numeric.IntegerExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EdgeSubstring2Equal implements Edge{
	Vertex v1, v2;
	final String name;
	private int a1, a2;
	private IntegerExpression ie_a1, ie_a2;
	
	public EdgeSubstring2Equal (String name, int a1, int a2, Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
		this.name = name;
		this.a1 = a1;
		this.a2 = a2;
	}
	
	public EdgeSubstring2Equal (String name, int a1, IntegerExpression a2, Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
		this.name = name;
		this.a1 = a1;
		this.ie_a2 = a2;
	}
	
	public EdgeSubstring2Equal (String name, IntegerExpression a1, int a2, Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
		this.name = name;
		this.ie_a1 = a1;
		this.a2 = a2;
	}
	
	public int getArgument1 () {
		if (ie_a1 != null) {
			throw new RuntimeException ("Not availible");
		}
		return a1;
	}
	
	public int getArgument2 () {
		if (ie_a2 != null) {
			throw new RuntimeException ("Not availible");
		}
		return a2;
	}
	
	@Override
	public Vertex getDest() {
		return v2;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Vertex getSource() {
		return v1;
	}

	@Override
	public List<Vertex> getSources() {
		List<Vertex> result = new ArrayList<Vertex>();
		result.add (v1);
		return result;
	}

	@Override
	public boolean isHyper() {
		return false;
	}

	public String toString () {
		return v1 + " --> " + v2;
	}

	@Override
	public boolean isDirected() {
		return true;
	}
	
	public void setSource (Vertex v) {
		this.v1 = v;
	}
	public void setDest (Vertex v) {
		this.v2 = v;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a1;
		result = prime * result + a2;
		result = prime * result + ((v1 == null) ? 0 : v1.hashCode());
		result = prime * result + ((v2 == null) ? 0 : v2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdgeSubstring2Equal other = (EdgeSubstring2Equal) obj;
		if (a1 != other.a1)
			return false;
		if (a2 != other.a2)
			return false;
		if (v1 == null) {
			if (other.v1 != null)
				return false;
		} else if (!v1.equals(other.v1))
			return false;
		if (v2 == null) {
			if (other.v2 != null)
				return false;
		} else if (!v2.equals(other.v2))
			return false;
		return true;
	}
	
	@Override
	public boolean allVertecisAreConstant() {
		return v1.isConstant() && v2.isConstant();
	}
	
	public boolean hasSymbolicArgs () {
		return ie_a1 != null || ie_a2 != null;
	}
	
	public IntegerExpression getSymbolicArgument1() {
		return ie_a1;
	}
	
	public IntegerExpression getSymbolicArgument2() {
		return ie_a2;
	}

	@Override
	public Edge cloneAndSwapVertices(Map<Vertex, Vertex> oldToNew) {
		EdgeSubstring2Equal result = new EdgeSubstring2Equal(name, a1, a2, oldToNew.get(v1),
				oldToNew.get(v2));
		result.ie_a1 = this.ie_a1;
		result.ie_a2 = this.ie_a2;
		return result;
	}
}
