/*
Copyright (c) 2024  Blue Robotics North Inc.

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

import java.util.ArrayList;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.fields.MultipleField.Index;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * This defines an array field
 * the contents of the array will be specified by the child of this array
 */
public class ArrayField extends ParentField implements MultipleField {
	@Override
	public List<Index> getIndeces() {
		List<Index> result = new ArrayList<>();
		int byteCount = getPaddedByteCount();
		int ni = getNumber().length;
		for(int i = 0; i < ni; ++i) {
			int n = getNumber()[i];
			byteCount /= n;
			
			result.add(new Index(this, i, ni, n,  byteCount));
		}
		return result;
	}
	private final int[] m_number;
	private final boolean m_fixed;
	/**
	 * makes an array field with the specified name, typeName, typeId, etc
	 * 
	 * @param name - the name of the field
	 * @param typeName - if this defines a type then this field specifies the name
	 * @param number - the number of elements if fixed or bounded, 0 or less if unbounded
	 * @param fixed - true means fixed. false means either bounded or unbounded
	 * @param comment - the description of this field derived from the comments
	 * @param c - the starting location in the schema files that defined this field
	 */
	public ArrayField(SymbolName name, ScopeName typeName,   int[] number, boolean fixed, String comment, Coord c) {
		super(name, typeName, TypeId.ARRAY, comment, c);
		m_number = number;
		m_fixed = fixed;
	}
	@Override
	public int[] getNumber() {
		return m_number;
	}


	
	@Override
	public int getBitCount() {

		int n = super.getBitCount();
		
		for(int i : getNumber()) {
			n *= i;
		}
		return n;
	}
	
	@Override
	public void add(Field f) {
		if(size() > 0) {
			throw new RuntimeException("Cannot add more than one child field to an array.");
		}
		super.add(f);
	}
	@Override
	public Field makeInstance(SymbolName name) {
		ArrayField f = new ArrayField(name, getTypeName(), getTypeId(), getNumber(), isFixed(), getComment(), getCoord());
		f.copyChildrenFrom(this);
		return f;
	}
	@Override
	public int getMinAlignment() {
		return getFirstChild().getMinAlignment();
	}
	@Override
	public int getPaddedByteCount() {
		return getBitCount()/8;
	}
	@Override
	public boolean isFixed() {
		return m_fixed;
	}



}
