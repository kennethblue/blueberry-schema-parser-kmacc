/*
Copyright (c) 2025  Blue Robotics

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.bluerobotics.blueberry.schema.parser.fields;

import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * An interface for a deferred field. That means the type must be looked up later
 */
public class DeferredField extends AbstractField {
	private final ScopeName[] m_imports;
	/**
	 * Creates a new deferred field
	 * @param name - the name of the field
	 * @param type - the name of the type of the field
	 * @param imports - a list of possible places to import from
	 * @param comment - the field comment
	 * @param c - the location in the source schema file that defined this field
	 */
	public DeferredField(SymbolName name, ScopeName type, ScopeName[] imports, String comment, Coord c) {
		super(name, type, TypeId.DEFERRED, comment, c);
		m_imports = imports;
	}

	public ScopeName[] getImports(){
		return m_imports;
	}
	

	@Override
	public Field makeInstance(SymbolName name) {
		return new DeferredField(getName(), getTypeName(), getImports(), getComment(), getCoord());
	}
	
	@Override
	public String toString() {
		String result = getClass().getSimpleName();
		result += "(";
		result += getTypeName().toLowerSnakeString();
		if(getName() != null) {
			result += " ";
			result += getName().toLowerCamel();
		} else {
			result += " ?";
		}
		result += ")";
		return result;
	}

	@Override
	public int getMinAlignment() {
		throw new RuntimeException("Cannot align a deferred field.");
	}

	@Override
	public int getPaddedByteCount() {
		throw new RuntimeException("Cannot align or pack a deferred field.");

	}

	
	
	
}
