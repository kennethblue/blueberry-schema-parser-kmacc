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

import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;
import com.bluerobotics.blueberry.schema.parser.constants.Number;
/**
 *
 */
public class MessageField extends ParentField {
	public static final SymbolName MODULE_MESSAGE_KEY_FIELD_NAME = SymbolName.fromCamel("moduleMessageKey");
	public static final SymbolName MAX_ORDINAL_FIELD_NAME = SymbolName.fromCamel("maxOrdinal");
	public static final SymbolName LENGTH_FIELD_NAME = SymbolName.fromCamel("length");


	public MessageField(SymbolName name, ScopeName typeName, String comment, Coord c) {
		super(name, typeName, TypeId.MESSAGE, comment, c);
		
		//add default header fields
		BaseField moduleMessageKeyF = new BaseField(MODULE_MESSAGE_KEY_FIELD_NAME, TypeId.UINT32, "The combination of the module unique key and the message unique key.", Coord.NULL);
		BaseField lenF = new BaseField(LENGTH_FIELD_NAME, TypeId.UINT16, "The length of this message", Coord.NULL);
		BaseField fieldNumF = new BaseField(MAX_ORDINAL_FIELD_NAME, TypeId.UINT8, "The highest field ordinal in this message", Coord.NULL);
		FillerByteField fillerF = new FillerByteField();
		add(moduleMessageKeyF);
		add(lenF);
		add(fieldNumF);
		add(fillerF);
	}

	@Override
	public Field makeInstance(SymbolName name) {
		MessageField result = new MessageField(name, getTypeName(), getComment(), getCoord());
		result.copyChildrenFrom(this);
		return result;
	}

	public List<Field> getFlatFields() {
		ArrayList<Field> result = new ArrayList<>();
		scanThroughDeepFields(f -> {
			if(f.isNamed() && f.getIndex() >= 0) {
				result.add(f);
			}
		});
		
		return result;
	}

	@Override
	public int getMinAlignment() {
		return 4;
	}
	@Override
	public int getPaddedByteCount() {		
		int result = getByteCount();
		int m = result % getMinAlignment();
		result += m > 0 ? getMinAlignment() - m : 0;
		return result;
	}
	/**
	 * looks for a serialization annotation and checks for a value of CDR
	 * @return
	 */
	public boolean useCdrNotBlueberry() {
		boolean result = false;
		Annotation a = getAnnotation(Annotation.KnownAnnotation.SERIALIZATION.getName());
		if(a != null) {
			Object s = a.getParameter(Object.class);
			if(s.toString().equals("CDR")) {
				result = true;
				
			}
		}
		return result;
	}
	/**
	 * looks for a topic annotation and returns the string value of the parameter
	 * @return
	 */
	public String getTopic() {
		String result = "";
		Annotation a = getAnnotation(Annotation.KnownAnnotation.TOPIC.getName());
		if(a != null) {
			result = a.getParameter(String.class).toString();
		}
		return result;
	}
	
	public int getModuleMessageKey() {
		Annotation modka = getAnnotation(Annotation.KnownAnnotation.MODULE_KEY.getName());
		Annotation meska = getAnnotation(Annotation.KnownAnnotation.MESSAGE_KEY.getName());
		
				
		if(modka == null) {
			throw new SchemaParserException("Message does not include a module key.", getCoord());
		} else if(meska == null) {
			throw new SchemaParserException("Message is not contained in a module that includes a module key.", getCoord());
		}
		Number modkn = modka.getParameter(Number.class);
		Number meskn = meska.getParameter(Number.class);
		
		if(modkn == null) {
			throw new SchemaParserException("Message module key is not a number.", getCoord());
		} else if(meskn == null) {
			throw new SchemaParserException("Module key of module that contains this message is not a number.", getCoord());
		}
		return modkn.asInt() << 16 | meskn.asInt();
	}
	/**
	 * returns the number of 4-byte words in this message
	 * This does not include any sequence blocks or string blocks
	 * @return
	 */
	public int getPaddedWordCount() {
		return getPaddedByteCount()/4;
	}
	/**
	 * Make a list of all child fields of this message that are not part of the header and are not filler
	 * Excludes bool field fields and children of defined types
	 * @param deep - when true recurses down sequences, arrays and structs
	 * @return
	 */
	public FieldList getUsefulChildren() {
		FieldList result = new FieldList();
		getChildren().forEach(true, f -> {
			if(f.getName() != null && f.getName().equals(MessageField.MODULE_MESSAGE_KEY_FIELD_NAME)) {
			} else if(f.getName() != null && f.getName().equals(MessageField.LENGTH_FIELD_NAME)) {
			} else if(f.getName() != null && f.getName().equals(MessageField.MAX_ORDINAL_FIELD_NAME)) {
			} else if(f instanceof BoolFieldField) {
				//don't include this, only its children will be included
			} else if(f.getParent() instanceof DefinedTypeField) {
				//don't add children if their parent is a defined type
				
			} else if(f.isNotFiller()) {
				result.add(f);
			}
		});
		
		return result;
	}

	public int getMaxOrdinal() {
		Field f = getLastChild();
		if(f.getBitCount() == 1) {
			f = f.getParent();
		}
		return f.getOrdinal();
	}

}
