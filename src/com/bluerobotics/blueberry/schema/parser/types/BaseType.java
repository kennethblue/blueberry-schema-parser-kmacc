/*
Copyright (c) 2024  Blue Robotics

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
package com.bluerobotics.blueberry.schema.parser.types;

import java.util.HashMap;

import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;

/**
 *
 */
public class BaseType extends AbstractType {
	private static final HashMap<TypeId,BaseType> BASE_TYPES = new HashMap<>();
	private BaseType(TypeId id) {
		super(id, SymbolName.fromSnake(id.name()),"");
	}
	public static BaseType getBaseType(TypeId id) {
		BaseType bt = BASE_TYPES.get(id);
		if(bt == null) {
			switch(id) {
			case BOOL:

			case BOOLFIELD:
			case FLOAT32:
			case FLOAT64:
			case INT16:
			case INT32:
			case INT64:
			case INT8:
			case UINT16:
			case UINT32:
			case UINT64:
			case UINT8:
				bt = new BaseType(id);
				BASE_TYPES.put(id, bt);
				break;
			case SEQUENCE:
			case STRING:
			case STRUCT:
				break;


			}
		}
		return bt;
	}










}
