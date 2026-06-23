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
package com.bluerobotics.blueberry.schema.parser.writers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.constants.Constant;
import com.bluerobotics.blueberry.schema.parser.constants.NumberConstant;
import com.bluerobotics.blueberry.schema.parser.constants.StringConstant;
import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.BlueModule;
import com.bluerobotics.blueberry.schema.parser.fields.DefinedTypeField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.Field;
import com.bluerobotics.blueberry.schema.parser.fields.FieldList;
import com.bluerobotics.blueberry.schema.parser.fields.MessageField;
import com.bluerobotics.blueberry.schema.parser.fields.MultipleField;
import com.bluerobotics.blueberry.schema.parser.fields.MultipleField.Index;
import com.bluerobotics.blueberry.schema.parser.fields.NameMaker;
import com.bluerobotics.blueberry.schema.parser.fields.ParentField;
import com.bluerobotics.blueberry.schema.parser.fields.ScopeName;
import com.bluerobotics.blueberry.schema.parser.fields.SequenceField;
import com.bluerobotics.blueberry.schema.parser.fields.StringField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName.Case;
import com.bluerobotics.blueberry.schema.parser.parsing.BlueberrySchemaParser;
import com.bluerobotics.blueberry.schema.parser.parsing.ParserIssueLogger;
import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;


/**
 * Autogenerates Java code stubs based on a parsed field structure
 */
public class JavaWriter extends SourceWriter {

	private final ScopeName m_packagePrefix;


	public JavaWriter(File dir, BlueberrySchemaParser parser, String header, String packagePrefix, ParserIssueLogger log) {
		super(dir, parser, header, log);
		m_packagePrefix = ScopeName.make(Case.LOWER_SNAKE, "\\.", packagePrefix);
	}

	@Override
	public void write() {
		ArrayList<BlueModule> modules = getParser().getModules();
//		m_constantsName = top.getName().append("constants").toUpperCamel();
//		m_bitIndexEnumName = top.getName().append("bit","index").toUpperCamel();
//		m_fieldIndexEnumName = top.getName().append("field","index").toUpperCamel();
//		m_packetBuilderName = top.getName().append("builder").toUpperCamel();
//		m_keyEnumName = top.getName().append("block","keys").toUpperCamel();
//		m_consumerInterfaceName = top.getName().append("consumer").toUpperCamel();
//		m_consumerManagerName = top.getName().append("consumer","manager").toUpperCamel();
//		m_packetRecieverName = top.getName().append("Receiver").toUpperCamel();
//
		modules.forEach(m -> {
			if(!m.isEmpty()) {
				writeConstantsFile(m);
				m.getMessages().forEachOfType(MessageField.class, false, msg -> {
					writeMessageFile(m, msg);
					
				});
			}
			
		});
		writeMessageLookup(modules);	
		
//		writeBlockParsers(top, headers);
//		writeParserInterface(top, headers);
//		writeConsumerManager(top, headers);
//		writePacketReceiver(top, headers);


	}
	/**
	 * makes a class that maps module/message keys to message constructors
	 * @param modules
	 */
	private void writeMessageLookup(ArrayList<BlueModule> modules) {
		// TODO Auto-generated method stub
		
	}


	private void writeInterfaceMethod(StructField bf) {
		String className = bf.getName().append("parser").toUpperCamelString();
		addDocComment("consume the "+className+" block.\n"+bf.getComment());
		addLine("public void consume("+className+" p);");
	}




	private void writeConstantsFile(BlueModule m) {
		startFile(m, getHeader());
		addLine();
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BitIndex;");
//		addLine("import com.bluerobotics.blueberry.transcoder.java.FieldIndex;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.EnumLookup;");
		addLine();

		addLine("public interface "+NameMaker.makeJavaConstantInterface(m)+" {");
		indent();
		
		writeConstants(m);
		writeTopics(m);
		addLine();
		
		
//		writeFieldIndexEnum(top);
//		writeBitIndexEnum(top);
		writeOtherEnums(m);
		closeBrace();
		writeToFile(NameMaker.makePackageName(m, m_packagePrefix).toLowerSnake("/")+"/"+NameMaker.makeJavaConstantInterface(m)+".java");



	}

	private void writeTopics(BlueModule m) {
		addSectionDivider("Topic String Constants");
		FieldList ms = m.getMessages();
		ms.forEachOfType(MessageField.class, false, msg -> {
			Annotation a = msg.getAnnotation(Annotation.KnownAnnotation.TOPIC.getName());
			String t = a.getParameter(String.class);
			addLine("public static final String "+NameMaker.makeTopicSymbol(msg)+" = \""+t+"\";");
		});
		addLine();
		
	}

	private void writeOtherEnums(BlueModule m) {
		m.getDefines().forEachOfType(EnumField.class, false, f -> {
			writeEnum(f);

		});
	
	}

	private void writeEnum(EnumField f) {
		List<NameValue> nvs = f.getNameValues();

		String comment = f.getComment();
		String name = NameMaker.makeEnumName(f);
		String type = lookupTypeForJavaType(f);

		addDocComment(comment);
		addLine("public enum "+name+" {");
		indent();
		for(NameValue nv : nvs) {
			String c = nv.getComment();
			if(c != null && !c.isBlank()) {
				c = "//"+c;
			} else {
				c = "";
			}
			addLine(nv.getName().toUpperSnakeString()+"(("+type+")"+nv.getValueAsHex()+"),"+c);
		}
		addLine(";");
		addLine("private static EnumLookup<"+name+"> m_lookup = new EnumLookup<"+name+">();");
		addLine("private int value;");
		addLine("private "+name+"(int v){");
		indent();
		addLine("value = v;");
		closeBrace();
		addLine("public int getValue(){");
		indent();
		addLine("return value;");
		closeBrace();
		addLine("public static "+name+" lookup(int i){");
		indent();
		addLine("if(m_lookup.size() == 0) {");
		indent();
		addLine("for("+name+" e : values()) {");
		indent();
		addLine("m_lookup.add(e.getValue(), e);");
		closeBrace();
		closeBrace();
		addLine("return m_lookup.lookup(i);");
		closeBrace();
		closeBrace();

	}

//	private String lookupTypeForFuncName(BaseField f) {
//		String result = "";
//		switch(f.getType()) {
//		case ARRAY:
//			break;
//		case BLOCK:
//			break;
//		case BOOL:
//			result = "bool";
//			break;
//		case BOOLFIELD:
//			result = "byte";
//			break;
//		case COMPOUND:
//			result = "int";
//			break;
//		case FLOAT32:
//			result = "float";
//			break;
//		case INT16:
//			result = "short";
//			break;
//		case INT32:
//			result = "int";
//			break;
//		case INT8:
//			result = "byte";
//			break;
//		case UINT16:m_bitIndexEnumName
//			result = "short";
//			break;
//		case UINT32:
//			result = "int";
//			break;
//		case UINT8:
//			result = "byte";
//			break;
//
//		}
//		return result;
//	}
	/**
	 * looks up an object type to represent the type of the specified base field
	 * This will not return a primitive type, instead it looks up the equivalent class type
	 * @param f
	 * @return
	 */
	private String lookupObjectTypeForJavaVars(Field f){
		String result = "";
		if(f instanceof EnumField) {
			result = NameMaker.makeEnumName((EnumField)f);
		} else {
			switch(f.getTypeId()) {
			case ARRAY:
				break;

			case BOOL:
				result = "Boolean";
				break;
			case FLOAT32:
			case FLOAT64:

				result = "Double";
				break;
			case BOOLFIELD:
			case INT16:
			case INT32:
			case INT8:
			case UINT16:
			case UINT32:
			case UINT8:
				result = "Integer";
				break;
			case DEFERRED:
				break;
			case DEFINED:
				break;
			case FILLER:
				break;
			case MESSAGE:
				break;
			case SEQUENCE:
				break;
			case STRING:
				result = "String";
				break;
			case STRUCT:
				break;
			case UINT64:
			case INT64:
				result = "Long";
				break;
		

			}
		}
		return result;
	}
	private String lookupTypeForJavaVars(Field f) {
		String result = "";
		if(f instanceof EnumField) {
			result = NameMaker.makeEnumName((EnumField)f);
		} else {
			result = lookupTypeForJavaVars(f.getTypeId());
			
		}
		return result;
	}
	private String lookupTypeForJavaVars(TypeId tid) {
		String result = "";
		switch(tid) {
		case ARRAY:
			break;
		
		case BOOL:
			result = "boolean";
			break;
		case BOOLFIELD:
			result = "int";
			break;
		
		case FLOAT32:
			result = "double";
			break;
		case INT16:
			result = "int";
			break;
		case INT32:
			result = "int";
			break;
		case INT8:
			result = "int";
			break;
		case UINT16:
			result = "int";
			break;
		case UINT32:
			result = "int";
			break;
		case UINT8:
			result = "int";
			break;
		case DEFERRED:
			break;
		case DEFINED:
			break;
		case FILLER:
			break;
		case FLOAT64:
			result = "double";
			break;
		case INT64:
			result = "long";
			break;
		case MESSAGE:
			break;
		case SEQUENCE:
			break;
		case STRING:
			break;
		case STRUCT:
			break;
		case UINT64:
			result = "long";//TODO:this is not strictly true
			break;
		

		}
		return result;
	}

	private String lookupTypeForJavaType(EnumField f) {
		String result = "";

		switch(f.getTypeId()) {
		case ARRAY:
			break;
		case BOOL:
			result = "boolean";
			break;
		case BOOLFIELD:
			result = "int";
			break;
		case FLOAT32:
			result = "double";
			break;
		case INT16:
			result = "short";
			break;
		case INT32:
			result = "int";
			break;
		case INT8:
			result = "byte";
			break;
		case UINT16:
			result = "short";
			break;
		case UINT32:
			result = "int";
			break;
		case UINT8:
			result = "byte";
			break;
		case DEFERRED:
			break;
		case DEFINED:
			break;
		case FILLER:
			break;
		case FLOAT64:
			result = "double";
			break;
		case INT64:
			result = "long";
			break;
		case MESSAGE:
			break;
		case SEQUENCE:
			break;
		case STRING:
			result = "String";
			break;
		case STRUCT:
			break;
		case UINT64:
			result = "long";//TODO:this is not strictly true
			break;
		default:
			break;


		}
		return result;
	}


	private void writeConstants(BlueModule m) {

		m.getMessages().forEachOfType(MessageField.class, false, mf -> {
			

			
			addDocComment(mf.getComment());
			addLine("public static final int "+ NameMaker.makeRelativeMessageKeyName(mf)+" = "+makeFullMessageKey(mf)+";");
		
		});
		m.getConstants().forEach(c -> {
			String ct = c.getComment();
			if(ct != null && !ct.isBlank()) {
				addBlockComment(ct);
			}
			SymbolName name = c.getName();
			if(c instanceof StringConstant) {
				StringConstant sc = (StringConstant)c;
				addLine("public static final String "+sc.getName()+" = \"" + sc.getValue()+"\";");
			} else if(c instanceof NumberConstant) {
				NumberConstant nc = (NumberConstant)c;
				TypeId tid = nc.getType().getTypeId();
				String type = lookupTypeForJavaVars(tid);
				String val = "";
				switch(tid) {
				
				case BOOL:
					val = "false";//TODO: don't have these yet
					break;
				case FLOAT32:
				case FLOAT64:
					val = nc.getValue().toString();
					break;
				case INT16:
				case INT32:
				case INT64:
				case INT8:
				case UINT16:
				case UINT32:
				case UINT64:
				case UINT8:
					val = nc.getValue().toString();
					break;
				case STRING:
				
				case BOOLFIELD:
				case DEFERRED:
				case DEFINED:
				case FILLER:

				case ARRAY:
				case STRUCT:
				case SEQUENCE:
				case MESSAGE:
					val = "";
					break;
				
				}
				addLine("public static final "+type+" "+nc.getName().toUpperSnakeString()+" = "+val+";");
			}
			
			//test if c is a Constant<Number>
			//also Constant<String>
			//maybe also Constant<boolean>
		});
	}

	@Override
	protected void startFile(BlueModule m, String... hs) {
		super.startFile(m, hs);
		addLine("package " + NameMaker.makePackageName(m, m_packagePrefix).toLowerSnake(".")+";");


	}

	private void writeMessageFile(BlueModule m, MessageField msg) {
		String messageName = NameMaker.makeJavaMessageClass(msg).toString();
		startFile(m, getHeader());
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryMessage;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBuffer;");
//		addLine("import com.bluerobotics.blueberry.transcoder.java.FieldIndex;");


		addLine();
		addBlockComment("A class to read and write a "+messageName);
		addLine("public class "+messageName+" extends BlueberryMessage implements "+NameMaker.makeJavaConstantInterface(m)+" {");
		indent();
		
		//add field indeces
		
		
	
		


//		addLineComment("This is the unique key to identify this type of message");
//		addLine("private static final int "+NameMaker.makeMessageModuleMessageConstant(msg)+" = "+WriterUtils.formatAsHex(msg.getModuleMessageKey())+";");
//		addLine();
		addLine("private static final int "+NameMaker.makeMessageMaxOrdinalName(msg)+" = "+msg.getMaxOrdinal()+";");
		addLine();
		addLine("private static final int "+NameMaker.makeMessageLengthName(msg)+" = "+msg.getPaddedByteCount()+";");
		addLine();
		
		
		
		FieldList fs = msg.getUsefulChildren().makeListSortedByName();
		FieldList fs2 = fs.duplicate();
		
		fs.forEach(f1 -> {
//			fs.forEach(f2 -> {
//				if(f1 == f2) {
//				} else if(NameMaker.makeFieldIndexName(f1).equals(NameMaker.makeFieldIndexName(f2))) {
//					fs2.remove(f2);
//				}
//			});
			if(f1 instanceof SequenceField) {
			} else if(f1.getName() == null || f1.getName().isEmpty()) {
				fs2.remove(f1);
			}
		});
		addLineComment("These values are used to index into this message to access the various fields.");
		ArrayList<String> lines = new ArrayList<>();
		fs2.forEach(f -> {
			if(getType(f) != null || f instanceof ArrayField || f instanceof SequenceField) {
			
				int fi = f.getIndex();
				if(f.getBitCount() == 1) {
					fi = f.getParent().getIndex();
				}
				lines.add("private static final int " + NameMaker.makeFieldIndexName(f) + " = "+fi+";");
					
			} else {
				System.out.println("JavaWriter.writeMessageFile not sure how to do index constant for --> "+f);
			}
		});
		Collections.sort(lines);
		addLines(lines);
		
		addLine();
		addLineComment("The following values represent the ordinals of the fields of this message.");
		addLineComment("This corresponds to the order that they were defined in the schema");
		lines.clear();
		fs2.forEach(f -> {
			List<Field> pis = f.getAncestors(MessageField.class);
			boolean inSeq = false;
			for(Field pi : pis) {
				if(pi instanceof SequenceField) {// || pi instanceof StructField) {
					inSeq = true;
					break;
				}
			}
			if(!inSeq) {
				int fi = f.getOrdinal();
				if(f.getBitCount() == 1) {
					fi = f.getParent().getOrdinal();
				}
				lines.add("private static final int " + NameMaker.makeFieldOrdinalName(f) + " = "+fi+";");
			}
		});
		Collections.sort(lines);
		addLines(lines);
		
		//bit num stuff
		addLine();
		addLineComment("Bit Nums");
		lines.clear();
		fs.forEachOfType(BaseField.class, false, f -> {
			if(f.getBitCount() == 1) {
				lines.add("private static final int " + NameMaker.makeBooleanBitNumName(f) + " = " +f.getIndex()+";");
			}
		});
		Collections.sort(lines);
		addLines(lines);
	
		//sequence stuff
		if(fs.isChildrenOfType(SequenceField.class, false)) {
			addLine();
			addLineComment("Sequence Element Byte Counts");
			lines.clear();
			fs.forEachOfType(SequenceField.class, false, sf -> {
				lines.add("private static final int " + NameMaker.makeMultipleFieldElementByteCountName(sf.getIndeces().getFirst()) + " = "+sf.getBytesPerElement()+";");
			});
			Collections.sort(lines);
			addLines(lines);
		}
		//string stuff
		if(fs.isChildrenOfType(StringField.class, false)) {
			addLine();
			addLineComment("String max length constants");
			lines.clear();
			fs.forEachOfType(StringField.class, false, sf -> {
				lines.add("private static final int " + NameMaker.makeStringMaxLengthName(sf) + " = "+sf.getMaxSize()+";");
			});
			Collections.sort(lines);
			addLines(lines);
		}
		//array stuff
		if(fs.isChildrenOfType(ArrayField.class, false)) {
			addLine();
			addLineComment("Add array sizes and element byte count");
			lines.clear();
			fs.forEachOfType(ArrayField.class, false, af -> {
				List<Index> is = af.getIndeces();
				int n = is.size();
				if(n == 1) {
					Index pi = is.get(0);
					lines.add("private static final int " + NameMaker.makeArraySizeName(pi) + " = "+pi.n+";");
					lines.add("private static final int " + NameMaker.makeMultipleFieldElementByteCountName(pi) + " = "+pi.bytesPerElement+";");
				} else {
					for(Index pi : is) { 
						lines.add("private static final int " + NameMaker.makeArraySizeName(pi) + " = "+pi.n+";");
						lines.add("private static final int " + NameMaker.makeMultipleFieldElementByteCountName(pi) + " = "+pi.bytesPerElement+";");
					}
				}
			});
			Collections.sort(lines);
			addLines(lines);
		}
		
		
		
		addMessageConstructor(msg);
		addTxMessageMaker(msg, true);
		addTxMessageMaker(msg, false);
		addRxMessageWrapper(msg);
		makeMessageFullTester(msg);
		
		msg.getUsefulChildren().forEach(false, f -> {
			if(NameMaker.makeFieldSetterName(f, false).equals("setTest10")) {
				System.out.println("JavaWriter.writeMessageFile blah");
			}
			makeMessageGetterSetter(f, true);
			makeMessageGetterSetter(f, false);
			makeMessagePresenceTester(f);
			
		});
		msg.getUsefulChildren().forEachOfType(SequenceField.class, false, f -> {
			makeSequenceInit(f);
			makeSequenceLengthGetter(f);

		});
		msg.getUsefulChildren().forEachOfType(StringField.class, false, f -> {
			makeStringCopier(f, true);
			makeStringCopier(f, false);
		});

		
		
		closeBrace();
		writeToFile(NameMaker.makePackageName(m, m_packagePrefix).toLowerSnake("/")+"/"+messageName+".java");
		
	}
	/**
	 * makes a function to test if a message contains no fields or if it contains all fields defined in this version of the schema
	 * This is computed using the max ordinal field
	 * @param mf
	 */
	private void makeMessageFullTester(MessageField mf) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("Tests if the current message ha all defined fields present.");

		if(mf.getComment() != null) {
			comments.add(mf.getComment());
		}
		
		SymbolName functionName = SymbolName.fromCamel("isFull");
		
		addDocComment(comments);
		addLine("public boolean "+functionName.toLowerCamel()+"(){");
		
		
		indent();

		
		addLine("return getMaxOrdinal() >= "+NameMaker.makeMessageMaxOrdinalName(mf)+";");
		
		
		
		closeBrace();
		
	}

	/**
	 * make getter or setter for all base types except strings
	 * Note that this won't make a setter for a simple field in a message. Those are set as part of the message adder
	 * @param f
	 * @param getNotSet
	 * @param protoNotDef
	 */
	private void makeMessageGetterSetter(Field f, boolean getNotSet) {
		String tf = getType(f);
		if(tf == null || f.getTypeId() == TypeId.STRING) {
			return;
		}
		List<Index> pis = MultipleField.getIndeces(f);
		
		//don't need a setter if it's a simple field not in an array
		if((!getNotSet) && pis.size() == 0) {
			return;
		}
		ArrayList<String> comments = new ArrayList<>();
		
		SymbolName fn = f.getName();
		if(fn == null) {
			fn = f.getParent().getName();
		}
		comments.add("A "+(getNotSet ? "g" : "s") + "etter for the "+fn.toLowerCamel()+" field");

		if(f.getComment() != null) {
			comments.add(f.getComment());
		}
		

		String paramList = "";
		addIndecesComments(pis, comments);
		paramList += makeIndecesParamList(pis);
		
		String val = "";
		
		
	
		if(!getNotSet) {
			val = tf + " "+fn.toLowerCamel();
			if(paramList.length() > 0) {
				paramList += ", ";
			}
			paramList += val;
			
			comments.add("@param "+fn.toLowerCamel()+prependHyphen(f.getComment()));
			
		}
		

	
		
		
		addDocComment(comments);
		
	
		
		String line;
		if(getNotSet) {
			line =  "public "+ tf + " " + NameMaker.makeFieldGetterName(f, false);
		} else {
			line =  "public void " + NameMaker.makeFieldSetterName(f, false);
		}
		line += "("+paramList+"){";
		addLine(line);
		
		indent();
		
		if(addLinesForFieldIndexCalc(pis, f)) {
			//only do this if there was a sequence as an index
			if(!getNotSet) {
				addLine("if(i < 0){");
				indent();
				addLine("return;//bail because a sequence was not initialized");
				closeBrace();
			}
		}
		//TODO: what to do if the index is invalid for a getter?
		
		String boolStuff = "";
		if(f.getBitCount() == 1) {
			//this is a bool so it needs another field for the bit num
			boolStuff = ", " + NameMaker.makeBooleanBitNumName(f);
		}
		boolean isEnum = f instanceof EnumField;
		if(getNotSet) {
			if(isEnum) {
				addLine("return " +tf+".lookup(m_buf."+lookupGetSetName(f, true)+"(i " + boolStuff + "));");
			} else {
				addLine("return " +"m_buf."+lookupGetSetName(f, true)+"(i " + boolStuff + ");");

			}
			
		} else {
			if(isEnum) {
				addLine("m_buf."+lookupGetSetName(f, false)+"(i " + boolStuff + ", "+ fn.toLowerCamel() + ".getValue());");
			} else {
				addLine("m_buf."+lookupGetSetName(f, false)+"(i " + boolStuff + ", "+ fn.toLowerCamel() + ");");				
			}
		}
		
		
		closeBrace();
	}
	

	/**
	 * makes a protected message constructor, either for tx or rx
	 * doesn't set up header
	 * @param mf
	 * @param txNotRx
	 */
	private void addMessageConstructor(MessageField mf) {
		String messageName = NameMaker.makeJavaMessageClass(mf).toString();
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A constructor to create a "+messageName+".");
		comments.add("This does the bare minimum: it just wraps a buffer in a message.");
		comments.add("This is private so it should never be called outside of this class.");
		comments.add("The intent is that the static factory methods would be used instead.");
		comments.add(mf.getComment());
		
		comments.add("@param buf - the message buffer to add the message to");
		String paramList = "BlueberryBuffer buf";
		ArrayList<Field> fs = new ArrayList<>();
		ArrayList<Field> ss = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		
		
		
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		addLine("private "+messageName+"("+paramList+") {");
		
		//now do contents of function
		indent();
	
		addLine("super(buf);");
		
		
		closeBrace();
	}
	/**
	 * makes a protected message constructor, either for tx or rx
	 * doesn't set up header
	 * @param mf
	 * @param txNotRx
	 */
	private void addRxMessageWrapper(MessageField mf) {
		String messageName = NameMaker.makeJavaMessageClass(mf).toString();
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A method to wrap a buffer of received data in a "+messageName+".");
		comments.add(mf.getComment());
		
		comments.add("@param buf - the message buffer to add the message to");
		String paramList = "BlueberryBuffer buf";
		ArrayList<Field> fs = new ArrayList<>();
		ArrayList<Field> ss = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		
		
		
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		addLine("public static "+messageName +" wrap"+"("+paramList+") {");
		
		//now do contents of function
		indent();
		addLine("if(!isModuleMessageKeyCorrect(buf, "+NameMaker.makeRelativeMessageKeyName(mf)+")){");
		indent();
		addLine("return null;");
		closeBrace();
		addLine(messageName + " msg = new "+messageName+"(buf);");
		//TODO: add stuff to check the module/message key and stuff
		
		addLine("return msg;");
		closeBrace();
	}
	private void addTxMessageMaker(MessageField mf, boolean params) {
		String messageName = NameMaker.makeJavaMessageClass(mf).toString();
		if(messageName.toLowerCase().startsWith("flash")) {
			System.out.println("Blah");
		}
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A constructor to create " + (params ? "a " : "an empty ") +messageName+" for transmission.");
		comments.add(mf.getComment());
		
		comments.add("@param buf - the message buffer to add the message to");
		String paramList = "BlueberryBuffer buf";
		ArrayList<Field> fs = new ArrayList<>();
		ArrayList<Field> ss = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		if(params) {
			mf.getUsefulChildren().forEach(false, f -> {
				
				String tp = getType(f);
				
				
				
				if(tp != null && f.getTypeId() != TypeId.STRING && (MultipleField.getIndeces(f)).size() == 0) {
					fs.add(f);
					
				} else if(f.getTypeId() == TypeId.STRING || f.getTypeId() == TypeId.SEQUENCE) {
					//build a list of all sequences and strings that are not in sequences
					boolean notInSequence = true;
					for(Index i : MultipleField.getIndeces(f)) {
						if(!i.arrayNotSequence) {
							notInSequence = false;
							break;
						}
					}
						
						
					if(notInSequence) {
						ss.add(f);
					}
				}
			});
		}
		
		for(Field f : fs) {
			String stuff = "";
			
			paramList += ", ";
			
			
			SymbolName paramName = NameMaker.makeParamName(f);
			String type = getType(f);
			
			paramList += type+" " + paramName+stuff;
			
			comments.add("@param " + paramName + prependHyphen( getFieldComment(f)));
		}
	
		
		
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		String prefix = params ? "" : "Empty";
		addLine("public static " + messageName + " make"+prefix+"("+paramList+") {");
		
		//now do contents of function
		indent();
		String maxOrd = params ? NameMaker.makeMessageMaxOrdinalName(mf) : "MIN_MAX_ORDINAL";
		String mLen = params ? NameMaker.makeMessageLengthName(mf) : "MIN_MESSAGE_LENGTH";
		addLine(messageName + " msg = new "+messageName+"(buf);");

		addLine("msg.setupHeader("+NameMaker.makeRelativeMessageKeyName(mf)+", "+maxOrd+", "+mLen+");");
		
		for(Field f : fs) {
				
			SymbolName paramName = NameMaker.makeParamName(f);
	
			
	
			
			String fn = paramName.toString();
			if(f instanceof EnumField) {
				fn += ".getValue()";
			}
			String booly = "";
			if(f.getBitCount() == 1) {
				booly = ", "+NameMaker.makeBooleanBitNumName(f);
			}
		
			addLine("msg.m_buf."+makeBbGetSet(f, true)+"("+NameMaker.makeFieldIndexName(f)+booly+", "+fn+");");

			
			
			
		}
		for(Field f : ss) {
			List<Index> pis = MultipleField.getIndeces(f);
			if(pis.size() == 0) {
				//zero the index field of each string and sequence
				String s = (f.getTypeId() == TypeId.SEQUENCE) ? "sequence" : "string";
				addLine("msg.m_buf.writeUint16("+NameMaker.makeFieldIndexName(f)+", INVALID_BLOCK);//clear "+s+" header");
			} else {
				//TODO: cycle through all the permutations of indeces and zero all the sequence headers
				//TODO: this is not right yet
				int[] ii = new int[pis.size()];
				int n = 1;
				for(Index pi : pis) {
					n *= pi.n;
				}
				int i = 0;
				int m = 1;
				while(i < n) {
					boolean carry = true;
					int offset = 0;
					for(int j = pis.size() - 1; j >= 0; --j) {
						Index pi = pis.get(j);
						offset += ii[j] * pi.bytesPerElement;
				
					}				
					String s = (f.getTypeId() == TypeId.SEQUENCE) ? "sequence" : "string";
					addLine("msg.m_buf.writeUint16("+NameMaker.makeFieldIndexName(f)+" + "+offset+", INVALID_BLOCK);//clear "+s+" header.");
					for(int j = pis.size() - 1; j >= 0; --j) {
						if(carry) {
							++ii[j];
							if(ii[j] >= pis.get(j).n) {
								ii[j] = 0;
								carry = true;
							} else {
								carry = false;
							}
						}
					}
					
					++i;
				}
				
				
			}
			
		}
		
		addLine("return msg;");
		closeBrace();
	}
	private String makeBbGetSet(Field f, boolean setNotGet) {
		SymbolName result = SymbolName.fromCamel(setNotGet ? "write" : "read");
		result = result.append(getTypeString(f));
		
		
		return result.toLowerCamelString();
	}

	private String getType(Field f) {
		String result = null;
		if(f instanceof EnumField) {
			result = NameMaker.makeEnumName((EnumField)f);
		} else {
		
		
			switch(f.getTypeId()) {
			case CHAR:
				result = "char";
				break;
			case BOOL:
				result = "boolean";
				break;
			case FLOAT32:
				result = "double";
				break;
			case FLOAT64:
				result = "double";
				break;
			case INT16:
				result = "int";
				break;
			case INT32:
				result = "int";
				break;
			case INT64:
				result = "long";
				break;
			case INT8:
				result = "int";
				break;
			case STRING:
				result = "String";
				break;
			case UINT16:
				result = "int";
				break;
			case UINT32:
				result = "long";
				break;
			case UINT64:
				result = "long";
				break;
			case UINT8:
				result = "int";
				break;
			case DEFINED:
				//check if it's a defined type of a base type
				Field f2 = f;
				while(f2 instanceof DefinedTypeField) {
					f2 = ((DefinedTypeField)f2).getFirstChild();
				}
				result = getType(f2);
				break;
			case ARRAY:
			case BOOLFIELD:
			case FILLER:
			case DEFERRED:
			case MESSAGE:
			case SEQUENCE:
			case STRUCT:
				result = null;
				break;
			
			}
		}
		return result;
	}
	
	/**
	 * adds lines to the output file for looking up the index of the specified field
	 * This takes into account the various array and sequence indeces required
	 * @param pis
	 * @param f
	 * @return true if there was a sequence in the list
	 */
	private boolean addLinesForFieldIndexCalc(List<Index> pis, Field f) {
		if(f.getName() == null && f.getParent().getName().toLowerCamelString().equals("floats")) {
			System.out.println("JavaWriter.addLinesForFieldIndexCalc test.");
		}
		boolean bail = false;
		boolean result = false;
		addLine("int i = 0;");
		if(pis.size() == 0) {
			
			
		} else {
			
			
			for(Index pi : pis) {
				addLine("i = i + "+NameMaker.makeFieldIndexName(pi.p)+";" );
				if(pi.p instanceof ArrayField) {
//					addLine("i += "+NameMaker.makeMultipleFieldElementByteCountName(pi) + " * " + name + ";");
					addLine("i = getArrayElementBlock(i, "+pi.paramName+", "+NameMaker.makeMultipleFieldElementByteCountName(pi)+", "+NameMaker.makeArraySizeName(pi)+");");
				} else if(pi.p instanceof SequenceField) {
					result = true;
					addLine("i = getSequenceElementBlock(i, "+pi.paramName+");");

				
				}
				
			}
			
		}
		if(f.getParent() instanceof SequenceField) {
		} else if(f.getParent() instanceof ArrayField) {
			
		} else {
			addLine("i = i + "+NameMaker.makeFieldIndexName(f)+";");
		}
		return result;
		
	}
	
	
	/**
	 * adds details of the index parameters to the specified comments list
	 * @param pis
	 * @param comments
	 */
	private void addIndecesComments(List<Index> pis, ArrayList<String> comments) {
		for(Index pi : pis) {
			
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+pi.paramName+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+pi.paramName+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}

		}
	}

	/**
	 * makes string list of index parameters to append to the paramater list of a function declaration
	 * for fields that must be accessed by specfying these indeces
	 * @param pis
	 * @return
	 */
	private String makeIndecesParamList(List<Index> pis) {
		String result = "";
		boolean firstTime = true;
		for(Index pi : pis) {
			if(!firstTime) {
				result += ", ";
			}
			firstTime  = false;
			result += "int " + pi.paramName;
		}
		
		return result;
	}
	/**
	 * looks up the name of the bb transcoder function 
	 * @param f
	 * @return
	 */
	private String lookupGetSetName(Field f, boolean getNotSet) {
		String result = getNotSet ? "read" : "write";
		result += getTypeString(f);
		return result;
	}
	private String getTypeString(Field f) {
		String result = "";
		switch(f.getTypeId()) {
		
		case BOOL:
			result = "Bit";
			break;
		case FLOAT32:
			result = "Float32";
			break;
		case FLOAT64:
			result = "Float64";
			break;
		case INT16:
			result = "Int16";
			break;
		case INT32:
			result = "Int32";
			break;
		case INT64:
			result = "Int64";
			break;
		case INT8:
			result = "Int8";
			break;
		case UINT16:
			result = "Uint16";
			break;
		case UINT32:
			result = "Uint32";
			break;
		case UINT64:
			result = "Uint64";
			break;
		case UINT8:
			result = "Uint8";
			break;
		case CHAR:
			result = "Char";
			break;
		case FILLER:
		case BOOLFIELD:
		case STRUCT:
		case ARRAY:
		case MESSAGE:
		case SEQUENCE:
		case STRING:
		case DEFERRED:
			throw new RuntimeException("I don't think this should have happened.");
	
		case DEFINED:
			Field cf = ((DefinedTypeField)f).getFirstChild();
			result = getTypeString(cf);
			break;
		
		default:
			break;
		}
		return result;
	}

	/**
	 * makes a function to test if a message has the specified field or not
	 * this uses the field number field to compare against the field's ordinal
	 * @param f
	 */
	private void makeMessagePresenceTester(Field f) {
		String tf = getType(f);
		if(tf == null || f.getTypeId() == TypeId.STRING) {
			return;
		}
		List<Index> pis = MultipleField.getIndeces(f);
		if(pis.size() > 0) {//only need this for top level message fields
			return;
		}
		
		
		ArrayList<String> comments = new ArrayList<>();
		SymbolName fn = f.getName();
		if(fn == null) {
			fn = f.getParent().getName();
		}
		comments.add("Tests if the current message containts the "+fn.toLowerCamel()+" field");

		if(f.getComment() != null) {
			comments.add(f.getComment());
		}

		
		
		String val = "";
		
	
		
	
		
		
		addDocComment(comments);
		String line = ("boolean ")+NameMaker.makeJavaFieldPresenceTesterName(f, false)+"(){";
		addLine(line);
		
		indent();
		
	
			
			addLine("return "+ NameMaker.makeFieldOrdinalName(f) + " <= (getMaxOrdinal());");
			
				
			closeBrace();
		return;		
	}
	/**
	 * makes the sequence initialize function
	 * this allocates bytes in the buffer for the contents of the sequence
	 * this must be called on all sequences before any of the contained fields can be assigned values
	 * TODO: init any child sequence placeholders to 0xffff
	 * @param sf
	 */
	private void makeSequenceInit(SequenceField sf) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A function to initialize a "+sf.getTypeName().deScope().toTitle());
		comments.add(sf.getComment());
		
		List<Index> pis = MultipleField.getIndeces(sf);
		
		
		comments.add("@param n - the number of elements of this sequence");
		String paramList = "";
				
		addIndecesComments(pis, comments);
		paramList += makeIndecesParamList(pis);
		if(paramList.length() > 0) {
			paramList += ", ";
		}
		paramList += "int n";
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		
		
		
		addLine("public void "+NameMaker.makeSequenceInitName(sf, false)+"("+paramList+"){");
		
		//now do contents of function
		indent();
		if(addLinesForFieldIndexCalc(pis, sf)) {
			//only do this test if there was a sequence in the index list
	
	
			addLine("if(i < 0){");
			indent();
			addLine("return;//bail because a sequence was not initialized");
			closeBrace();
		}
		
		addLineComment("i is now the index of this sequence field header");
		
		
		
		
		addLine("int bs = "+NameMaker.makeMultipleFieldElementByteCountName(sf.getIndeces().getFirst())+";");
		addLine("initSequenceBlock(i,  bs, n);");
		
		outdent();
		addLine("}");
	}



	private void makeSequenceLengthGetter(SequenceField sf) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("Gets the defined length of a sequence "+sf.getTypeName().deScope().toTitle());
		comments.add(sf.getComment());
		
		List<Index> pis = MultipleField.getIndeces(sf);
		
		

		String paramList = "";
				
		
		addIndecesComments(pis, comments);
		
		paramList += makeIndecesParamList(pis);
		
		comments.add("@return - the number of elements in the sequence");
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		
		
	
		
		addLine("public int get"+NameMaker.makeSequenceLengthGetterName(sf, false)+ "("+paramList+"){");
		
		//now do contents of function
		indent();

		
		
		
		
		
		if(addLinesForFieldIndexCalc(pis, sf)) {

			//only do this test if there was a sequence in the index list


			addLine("if(i < 0){");
			indent();
			addLine("return 0;//bail because a sequence was not initialized");
			closeBrace();
		}
		addLineComment("i is now the index of this sequence field header");
		
		addLine("return getSequenceLength(i);");
		
		
		
		
		
		outdent();
		addLine("}");
	}

	private void makeStringCopier(StringField f, boolean toNotFrom) {
		List<Index> pis = MultipleField.getIndeces(f);
		ArrayList<String> comments = new ArrayList<>();
		SymbolName fn = f.getName();
		if(fn == null) {
			fn = f.getParent().getName();
		}
		comments.add("A function to copy a string "+(toNotFrom ? "to" : "from") + " a message.");
		

		if(f.getComment() != null) {
			comments.add(f.getComment());
		}
		
		
		String paramList = "";
	
		
		addIndecesComments(pis, comments);
		paramList += makeIndecesParamList(pis);

		
		if(toNotFrom) {
			if(paramList.length() > 0) {
				paramList += ", ";
			}
			paramList += "String s";
			comments.add("@param s - the string to copy to the message");
		}
		
		
	
		
			
			
	
		

	
		
		
		addDocComment(comments);
		
		addLine((toNotFrom ? "public void " : "public String ")+NameMaker.makeStringCopierName(f, toNotFrom, false)+"("+paramList+"){");
		
	
		
		indent();
		if(addLinesForFieldIndexCalc(pis, f)) {
			
			//only do this test if there was a sequence in the index list
			addLine("if(i < 0){");
			indent();
			addLine("return"+(toNotFrom ? "" : " null")+";//bail because a sequence was not initialized");
			closeBrace();
		}
		
		addLineComment("i is now the index of this string field header");

		
		if(toNotFrom) {
			addLine("addString(i, s);");
		} else {
			addLine("return getString(i);");
		}
			
			
	
		

		closeBrace();
		
	}


	
}
