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

import java.util.ArrayList;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.fields.MultipleField.Index;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;
/**
 *
 */
public class SequenceField extends ParentField implements MultipleField {
	private int m_limit;
	
	public SequenceField(SymbolName name, ScopeName typeName, String comment, Coord c) {
		super(name, typeName, TypeId.SEQUENCE, comment, c);
	}
	
	@Override
	public Field makeInstance(SymbolName name) {
		SequenceField result = new SequenceField(name, getTypeName(), getComment(), getCoord());
		result.copyChildrenFrom(this);
		return result;
	}
	
	
	
	public void setLimit(int n) {
		m_limit = n;
	}
	public int getLimit() {
		return m_limit;
	}

	@Override
	public int getMinAlignment() {	
		return 4;
	}
	@Override
	public int getPaddedByteCount() {
		return 4;
	}
	
	/**
	 * compute the number of bytes for each element of this sequence
	 * @return
	 */
	public int getBytesPerElement(){
		int result = super.getBitCount()/8;
		return result;
	}
	
	

	@Override
	public int getBitCount() {
		return 32;//sequence placeholder is 32 bits long
	}

	@Override
	public List<Index> getIndeces() {
		ArrayList<Index> result = new ArrayList<>();

		result.add(new Index(this, 0, 1, -1, getPaddedByteCount()));
		return result;
	}

	@Override
	public int[] getNumber() {
		return new int[] {-1};
	}

	@Override
	public boolean isFixed() {
		return false;//sequences are by definition not fixed in size
	}
	
}
