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
package com.bluerobotics.blueberry.schema.parser.constants;

import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;
import com.bluerobotics.blueberry.schema.parser.types.Type;

/**
 *
 */
public abstract class AbstractConstant<T> implements Constant<T> {
	private final SymbolName m_name;
	private final String m_comment;
	private final Type m_type;
	private String m_fileName = null;
	
	protected AbstractConstant(Type type, SymbolName name, String comment) {
		m_type = type;
		m_name = name;
		m_comment = comment;
	}
	@Override
	public SymbolName getName() {
		return m_name;
	}
	@Override
	public String getComment() {
		return m_comment;
	}
	@Override
	public Type getType() {
		return m_type;
	}
	
	@Override
	public void setFileName(String name) {
		m_fileName = name;
	}
	@Override
	public String getFileName() {
		return m_fileName;
	}
	@Override
	public String toString() {
		return getClass().getSimpleName()+"("+getName()+" = "+getValue()+")";
	}
	
	
}
