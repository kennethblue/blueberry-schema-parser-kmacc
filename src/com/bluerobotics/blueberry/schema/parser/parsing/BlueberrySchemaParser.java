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
package com.bluerobotics.blueberry.schema.parser.parsing;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.regex.Pattern;

import com.bluerobotics.blueberry.schema.parser.constants.BooleanConstant;
import com.bluerobotics.blueberry.schema.parser.constants.Constant;
import com.bluerobotics.blueberry.schema.parser.constants.Number;
import com.bluerobotics.blueberry.schema.parser.constants.NumberConstant;
import com.bluerobotics.blueberry.schema.parser.constants.StringConstant;
import com.bluerobotics.blueberry.schema.parser.fields.AnnotationOwner;
import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.BlueModule;
import com.bluerobotics.blueberry.schema.parser.fields.BlueberryFieldPacker;
import com.bluerobotics.blueberry.schema.parser.fields.BoolFieldField;
import com.bluerobotics.blueberry.schema.parser.fields.DeferredField;
import com.bluerobotics.blueberry.schema.parser.fields.DefinedTypeField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.Field;
import com.bluerobotics.blueberry.schema.parser.fields.FieldList;
import com.bluerobotics.blueberry.schema.parser.fields.MessageField;
import com.bluerobotics.blueberry.schema.parser.fields.ParentField;
import com.bluerobotics.blueberry.schema.parser.fields.ScopeName;
import com.bluerobotics.blueberry.schema.parser.fields.SequenceField;
import com.bluerobotics.blueberry.schema.parser.fields.StringField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName.Case;
import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CharToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CommentToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.tokens.EolToken;
import com.bluerobotics.blueberry.schema.parser.tokens.FilePathToken;
import com.bluerobotics.blueberry.schema.parser.tokens.IdentifierToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NameValueToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NumberToken;
import com.bluerobotics.blueberry.schema.parser.tokens.ScopeNameToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SingleWordToken;
import com.bluerobotics.blueberry.schema.parser.tokens.StringToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SymbolNameToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Token;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenList;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants.TokenIdentifier;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;
import com.bluerobotics.blueberry.schema.parser.writers.WriterUtils;
import com.starfishmedical.comms.Crc1021;

/**
 * This class implements the token parsing algorithm
 * This reads the schema file, tokenizes and then builds a data structure of fields to represent the schema
 */
public class BlueberrySchemaParser implements Constants, TokenConstants {
	private static final TokenIdentifier[] VERSION_3_IDENTIFIERS = {
		TokenIdentifier.DOUBLE_COLON,
		TokenIdentifier.ANNOTATION_START,
		TokenIdentifier.CHAR,
		TokenIdentifier.COLON,
		TokenIdentifier.COMMA,
		TokenIdentifier.HASH,

		TokenIdentifier.LESS_THAN_OR_EQUALS,
		TokenIdentifier.EQUALS,
		TokenIdentifier.SEMICOLON,
		TokenIdentifier.SPACE,
		TokenIdentifier.TAB,

		TokenIdentifier.BRACE_START,
		TokenIdentifier.BRACE_END,
		TokenIdentifier.BRACKET_START,
		TokenIdentifier.BRACKET_END,
		TokenIdentifier.SQUARE_BRACKET_START,
		TokenIdentifier.SQUARE_BRACKET_END,
		TokenIdentifier.ANGLE_BRACKET_START,
		TokenIdentifier.ANGLE_BRACKET_END,

		TokenIdentifier.BOOLEAN,
		TokenIdentifier.BYTE,
		TokenIdentifier.SHORT,
		TokenIdentifier.LONG_LONG,
		TokenIdentifier.LONG,
		TokenIdentifier.INT,
		TokenIdentifier.INT16,
		TokenIdentifier.INT32,
		TokenIdentifier.INT64,
		TokenIdentifier.INT8,
		TokenIdentifier.UINT16,
		TokenIdentifier.UINT32,
		TokenIdentifier.UINT64,
		TokenIdentifier.UINT8,
		TokenIdentifier.UNSIGNED,
		TokenIdentifier.FLOAT,
		TokenIdentifier.FLOAT32,
		TokenIdentifier.FLOAT64,
		TokenIdentifier.STRING,
		TokenIdentifier.WSTRING,
		TokenIdentifier.DOUBLE,

		TokenIdentifier.TRUE,
		TokenIdentifier.FALSE,
		TokenIdentifier.CONST,
		TokenIdentifier.ENUM,
		TokenIdentifier.IMPORT,
		TokenIdentifier.MESSAGE,
		TokenIdentifier.MODULE,
		TokenIdentifier.STRUCT,
		TokenIdentifier.TYPEDEF_SEQUENCE,
		TokenIdentifier.TYPEDEF,
		TokenIdentifier.SEQUENCE			
	};
	private static final TokenIdentifier[] VERSION_3_SEPARATORS = {
		TokenIdentifier.SPACE,
		TokenIdentifier.TAB,
		TokenIdentifier.BRACE_START,
		TokenIdentifier.BRACE_END,
		TokenIdentifier.BRACKET_START,
		TokenIdentifier.BRACKET_END,
		TokenIdentifier.SQUARE_BRACKET_START,
		TokenIdentifier.SQUARE_BRACKET_END,
		TokenIdentifier.ANGLE_BRACKET_START,
		TokenIdentifier.ANGLE_BRACKET_END,
		TokenIdentifier.DOUBLE_COLON,
		TokenIdentifier.COLON,
		TokenIdentifier.EQUALS,
		TokenIdentifier.ANNOTATION_START,
		TokenIdentifier.SEMICOLON,
		TokenIdentifier.COMMA
	};

	
	private static final TokenIdentifier[] VERSION_3_BASE_TYPES = {
		TokenIdentifier.BOOLEAN,
		TokenIdentifier.CHAR,
		TokenIdentifier.BYTE,
		TokenIdentifier.DOUBLE,
		TokenIdentifier.FLOAT,
		TokenIdentifier.INT,
		TokenIdentifier.INT16,
		TokenIdentifier.INT32,
		TokenIdentifier.INT64,
		TokenIdentifier.INT8,
		TokenIdentifier.UINT16,
		TokenIdentifier.UINT32,
		TokenIdentifier.UINT64,
		TokenIdentifier.UINT8,
		TokenIdentifier.LONG,
		TokenIdentifier.LONG_LONG,
		TokenIdentifier.SHORT,
		TokenIdentifier.UNSIGNED,
		TokenIdentifier.STRING,
	};
	private static final TokenIdentifier[] VERSION_4_IDENTIFIERS = {
//			TokenIdentifier.ANNOTATION_START,
//			TokenIdentifier.COLON,
		TokenIdentifier.COMMA,
		TokenIdentifier.HASH,

		TokenIdentifier.LESS_THAN_OR_EQUALS,
		TokenIdentifier.EQUALS,
		TokenIdentifier.SEMICOLON,
		TokenIdentifier.SPACE,
		TokenIdentifier.TAB,

//			TokenIdentifier.BRACE_START,
//			TokenIdentifier.BRACE_END,
//			TokenIdentifier.BRACKET_START,
//			TokenIdentifier.BRACKET_END,
		TokenIdentifier.SQUARE_BRACKET_START,
		TokenIdentifier.SQUARE_BRACKET_END,
//			TokenIdentifier.ANGLE_BRACKET_START,
//			TokenIdentifier.ANGLE_BRACKET_END,

		TokenIdentifier.BOOL,
		TokenIdentifier.CHAR,
		TokenIdentifier.BYTE,
		TokenIdentifier.INT16,
		TokenIdentifier.INT32,
		TokenIdentifier.INT64,
		TokenIdentifier.INT8,
		TokenIdentifier.UINT16,
		TokenIdentifier.UINT32,
		TokenIdentifier.UINT64,
		TokenIdentifier.UINT8,
		TokenIdentifier.FLOAT32,
		TokenIdentifier.FLOAT64,
		TokenIdentifier.STRING,
		TokenIdentifier.WSTRING,

		TokenIdentifier.TRUE,
		TokenIdentifier.FALSE,
	};
	private static final TokenIdentifier[] VERSION_4_SEPARATORS = {
		TokenIdentifier.SPACE,
		TokenIdentifier.TAB,
		TokenIdentifier.SQUARE_BRACKET_START,
		TokenIdentifier.SQUARE_BRACKET_END,
		TokenIdentifier.LESS_THAN_OR_EQUALS,
		TokenIdentifier.EQUALS,
		TokenIdentifier.COMMA
	};
	private static final TokenIdentifier[] VERSION_4_BASE_TYPES = {
		TokenIdentifier.BOOL,
		TokenIdentifier.CHAR,
		TokenIdentifier.BYTE,
		TokenIdentifier.FLOAT32,
		TokenIdentifier.FLOAT64,
		TokenIdentifier.INT,
		TokenIdentifier.INT16,
		TokenIdentifier.INT32,
		TokenIdentifier.INT64,
		TokenIdentifier.INT8,
		TokenIdentifier.UINT16,
		TokenIdentifier.UINT32,
		TokenIdentifier.UINT64,
		TokenIdentifier.UINT8,
		TokenIdentifier.STRING,
	};
//	private static final String SEP = TokenIdentifier.SCOPE_SEPARATOR.id();

	private final TokenList m_tokens = new TokenList();//this is where all tokens get assembled while parsing
	private final FieldList m_defines = new FieldList();//all defines end up here
//	private final ArrayList<Constant<?>> m_constants = new ArrayList<>();//all parsed constants will be stored here
	private final FieldList m_messages = new FieldList();//all parsed messages will be stored here
	
	private final ArrayList<ScopeName> m_imports = new ArrayList<>();//temporary storage of imported module names
	private final ArrayList<Annotation> m_annotations = new ArrayList<>();//temporary storage of annotations
	private final ArrayList<BlueModule> m_moduleStack = new ArrayList<>();//keeps track of the current module that the token being currently processed is within.
	private final ArrayList<BlueModule> m_modules = new ArrayList<>();
	private MessageField m_message = null;
	private final ArrayList<IdentifierToken> m_moduleEnd = new ArrayList<>();
	private String m_fileName = null;//indicates the filename that the present tokens are from
	private String m_lastComment = null;//temporary storage for the last processed comment

	private final ParserIssueLogger m_log;
	
	public static final int VERSION_3 = 3;
	public static final int VERSION_4 = 4;
	private int m_ver = VERSION_4;
	
	
	
	
	public BlueberrySchemaParser(ParserIssueLogger log) {

		m_log = log;
	}
	
	/**
	 * Clear this parser's state in preparation for a new parsing session
	 */
	public void clear() {
		m_tokens.clear();
		m_defines.clear();
		m_log.clear();
		m_messages.clear();
		
		m_imports.clear();
		m_annotations.clear();
		
		m_moduleStack.clear();
		m_moduleStack.add(BlueModule.ROOT);
		m_modules.add(BlueModule.ROOT);
		m_moduleEnd.clear();
		m_fileName = null;
		m_lastComment = null;
		

	}
	public void append(String filePath, String[] schemaLines) {
		if(m_ver == VERSION_3) {
			appendV3(filePath, schemaLines);
		} else if(m_ver == VERSION_4) {
			appendV4(filePath, schemaLines);
		}
	}
	/**
	 * this reads through the contents of a file and adds tokens to the current token list
	 * @param filePath - a string of the file path of this file
	 * @param schema - a string containing the schema to parse
	 */
	private void appendV3(String filePath, String[] schemaLines) {
		//split into lines


		FilePathToken fpt = new FilePathToken(filePath, schemaLines);
		
		Coord c = m_tokens.add(fpt).getStart();
						

		while(c != null) {
			c = processV3BlockComment(c);
			c = processV3LineComment(c);
			
			c = processStrings(c, STRING_DELIMITER);
			c = processStrings(c, "'");//for v4 strings are valid to be enclosed in single quotes too
			c = processChars(c);
			c = processNextWordToken(c, VERSION_3_IDENTIFIERS, VERSION_3_SEPARATORS);
//			
			c = processEol(c);
			

		}
	
	}
	/**
	 * this reads through the contents of a file and adds tokens to the current token list
	 * @param filePath - a string of the file path of this file
	 * @param schema - a string containing the schema to parse
	 */
	private void appendV4(String filePath, String[] schemaLines) {
		//split into lines


		FilePathToken fpt = new FilePathToken(filePath, schemaLines);
		
		Coord c = m_tokens.add(fpt).getStart();
		if(!fpt.getPathElementFromEnd(1).toLowerCase().equals("msg")) {
			m_log.issueError("Expecting messge files to be in a directory called \"msg\"", fpt.getStart());
		}
						

		while(c != null) {
			
				
			c = processV4Comment(c);
			
			c = processStrings(c, TokenIdentifier.DOUBLE_QUOTE.id());
			c = processStrings(c, TokenIdentifier.SINGLE_QUOTE.id());//for v4 strings are valid to be enclosed in single quotes too
//			c = processChars(c);
			c = processNextWordToken(c, VERSION_4_IDENTIFIERS, VERSION_4_SEPARATORS);

			c = processEol(c);
			

		}
	
	}

	

	/**
	 * scans through the token list and generates a full set of constants, defines and message fields from it.
	 */
	public void parse() {
		try {
			if(m_ver == VERSION_3) {
				parseV3();
			} else if(m_ver == VERSION_4) {
				parseV4();
			}
		} catch (SchemaParserException e) {
			e.printStackTrace();
		}
		System.out.println("************* Messages ***************");
		m_messages.forEach(f -> {
			
			System.out.println(f.toString());
		});


		System.out.println("BlueberrySchemaParser.parse done.");
	}
	private void parseV4() {
//		collapseIdentifiers(VERSION_4_IDENTIFIERS);
//		collapseLessThanOrEqualTo();
		collapseNumbers();
		collapseBaseTypes(VERSION_4_BASE_TYPES);
		collapseSymbolNames();
		//need to convert any symbol names to scope names if they contain a "/"
		
		collapseWhiteSpace();
		collapseComments();
		removeSemicolons();//there shouldn't be semicolons but remove just in case
		collapseEolsV4();
		
//		collapseNameValues();
		
		//check all brackets of all kinds to be sure they all match
		matchBracket(null);
		
		assembleFieldsV4();

		processDeferredFields(m_messages.getIterator(), m_messages);
		
		
	}
	
	

	private void parseV3() {
		
		collapseIdentifiers(VERSION_3_IDENTIFIERS);
		collapseNumbers();
		collapseSymbolNames();
		collapseLongLong();
		collapseBaseTypes(VERSION_3_BASE_TYPES);
		collapseScope();
		collapseWhiteSpace();
		collapseUnsigned();
		collapseComments();
		

		collapseTypedefs();
		collapseEolsV3();
		
		collapseNameValues();
//			collapseSemicolons();
		
		//check all brackets of all kinds to be sure they all match
		matchBracket(null);

		assembleFieldsV3();
		fillInMissingEnumValues();
		checkForDuplicateEnumValues();
		checkForDuplicateAnnotations();
		fillInMissingMessageKeyValues();
		checkForMessagesInTopModuleLevel();
		
		fillInMissingModuleKeyValues();
		propagateModuleAnnotations();
		assignModuleAnnotations();
		
		
		checkForDuplicateMessageKeys();
		checkForMessageKeys();
		checkForMessageTopics();
		
		processDeferredFields(m_defines.getIterator(), m_defines);	
		processDeferredFields(m_messages.getIterator(), m_defines);
		
		applyDeferredParameters(m_defines);
		applyDeferredParameters(m_messages);
		
		computeOrder(m_messages);
	
		organizeBools(m_messages);

		
		computeParents(m_defines, null);
		computeParents(m_messages, null);
		
		
		computeIndeces();
		
		sortFields();
			

		

	}
	/**
	 * sort of the field lists so the output files will have a pleasant order
	 */
	private void sortFields() {
		for(BlueModule bm : m_modules) {
			//sort constants
			Collections.sort(bm.getConstants(), (a, b) -> {
				String an = a.getName().toDotString();
				String bn = b.getName().toDotString();
				
				return an.compareTo(bn);
			});
			//sort messages
			bm.getMessages().sortByTypeName();
			
		}
		
	}

	/**
	 * Messages should only be in 2nd or greater level below root module
	 */
	private void checkForMessagesInTopModuleLevel() {
		m_modules.forEach(m -> {
			if(m.getName().getNumberOfLevels() < 2) {
				if(m.getMessages().size() > 0) {
					m_log.issueError("Modules within one level of root should not contain messages.", m.getCoord());
				}
			}
		});
	}

	private void checkForMessageKeys() {
		for(BlueModule mod : m_modules) {
			mod.getMessages().forEach(msg -> {
				Annotation a = msg.getAnnotation(Annotation.KnownAnnotation.MODULE_KEY.getName());
				if(a == null) {
					m_log.issueError("No module key propagated to message "+msg.getName(), null);
				}
			});
		}
	}

	/**
	 * checks each annotation owner for duplicate annotations
	 */
	private void checkForDuplicateAnnotations() {
		for(BlueModule mod : m_modules) {
			checkForDuplicateAnnotations(mod);
			mod.getDefines().forEach(true, f -> {
				checkForDuplicateAnnotations(f);
			});
			mod.getMessages().forEach(true, f -> {
				checkForDuplicateAnnotations(f);
			});
		}
	}

	private void checkForDuplicateAnnotations(AnnotationOwner ao) {
		ao.scanAnnotations(a1 -> {
			ao.scanAnnotations(a2 -> {
				if(a1 != a2 && a1.matchesByName(a2)) {
					if(a1.matchesByParameters(a2)) {
						m_log.issueWarning("Duplicate annotation found with same parameters.", a1.getSource(), a2.getSource());
					} else {
						m_log.issueError("Duplicate annotation found with unmatching parameters.",a1.getSource(), a2.getSource());
 
					}
				}
			});
		});
		
	}

	/**
	 * any instance of two longs in a row should be collapsed to a long long
	 */
	private void collapseLongLong() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken long1 = m_tokens.gotoNextId(TokenIdentifier.LONG);
			IdentifierToken long2 = m_tokens.relativeId(1, TokenIdentifier.LONG);
			if(long1 != null && long2 != null) {
				
				m_tokens.remove(long2);
				IdentifierToken longLong = new IdentifierToken(long1.getStart(), long2.getEnd(), TokenIdentifier.LONG_LONG);
				m_tokens.replace(long1, longLong);
				
			}
			m_tokens.next();
		}
		
	}
	/**
	 * copy annotations from any two level module to any child module
	 */
	private void propagateModuleAnnotations() {
		ArrayList<Annotation> as = new ArrayList<>();
		m_modules.forEach(m -> {
			ScopeName sn = m.getName();
			if(sn.getNumberOfLevels() == 2) {
				as.clear();
				m.scanAnnotations(a -> {
					as.add(a);
				});
				Annotation[] aa = as.toArray(new Annotation[as.size()]);
				m_modules.forEach(m2 -> {
					if(m2 != m) {
						ScopeName sn2 = m2.getName();
						if(sn2.isChildOf(sn)){
							m2.addAnnotation(aa);
						}
					}
				});
			}
		});
	}
	/**
	 * put a reference to all module annotations into every message member of that module
	 */
	private void assignModuleAnnotations() {
		ArrayList<Annotation> as = new ArrayList<>();
		m_modules.forEach(m -> {
			as.clear();
			m.scanAnnotations(a -> {
				as.add(a);
			});
			Annotation[] aa = as.toArray(new Annotation[as.size()]);
//			m.getDefines().forEach(f -> {
//				f.addAnnotation(aa);
//			});
			m.getMessages().forEach(mess -> {
				mess.addAnnotation(aa);
			});
		});
		
	}
	/**
	 * scan through all messages and compute the field order
	 * any children of message fields will be assigned the same value as their parent
	 * @param ms - the field list to scan through
	 */
	private void computeOrder(FieldList ms) {
		ms.forEachOfType(MessageField.class, false, m -> {
			Iterator<Field> fs = m.getChildren().getIterator();
			int order = 0;
			while(fs.hasNext()) {
				Field f = fs.next();
				if(f.isNotFiller()) {//don't include any filler because it was not in the original definition
					f.setOrdinal(order);
					if(f instanceof ParentField) {
						ParentField pf = (ParentField)f;
						int o = order;
						pf.getChildren().forEach(true, ft -> {
							ft.setOrdinal(o);
						});
					}
					++order;
				}
			}
		});
		
	}
	private void organizeBools(FieldList ms) {
		ms.forEachOfType(MessageField.class, false, mf -> {
			organizeBools(mf);
		});
	}
	
	/**
	 * recurse through the specified parent and make sure all one bit fields are contained within bool field fields
	 * @param pf
	 */
	private void organizeBools(ParentField pf) {
		ListIterator<Field> li = pf.getChildren().getIterator();
		BoolFieldField bff = null;
		while(li.hasNext()) {
			Field f = li.next();
			if(f instanceof BoolFieldField) {
				bff = (BoolFieldField)f;
				if(bff.getChildren().size() >= 8) {
					bff = null;
				}
				
			} else if(f instanceof BaseField) {
				BaseField bf = (BaseField)f;
				if(bf.getBitCount() == 1) {
					if(bff != null) {
						bff.add(bf);
						li.remove();
					} else {
						bff = new BoolFieldField(bf.getCoord());
						bff.add(bf);
						li.set(bff);
					}
				}
			} else if(f instanceof ParentField) {
				ParentField pf2 = (ParentField)f;
				organizeBools(pf2);
			}
		}
	}
	/**
	 * Traverse the higherarchy and set parents.
	 * @param fl
	 * @param pf
	 */
	private void computeParents(FieldList fl, ParentField pf) {
		fl.forEach(f -> {
			f.setParent(pf);
			if(f instanceof ParentField) {
				ParentField pf2 = (ParentField)f;
				computeParents(pf2.getChildren(), pf2);
			}
		});
	}
	/**
	 * scans all messages and checks for any duplicate message keys
	 * this should be called after fillInMessageKeyValues()
	 */
	private void checkForDuplicateMessageKeys() {
		final ArrayList<Annotation> keys = new ArrayList<>();
		m_modules.forEach(mod -> {
			keys.clear();
			mod.getMessages().forEachOfType(MessageField.class, false, msg -> {
				Annotation a = msg.getAnnotation(Annotation.KnownAnnotation.MESSAGE_KEY.getName());
				
				if(a != null) {
					for(Annotation at : keys) {
						if(at.matchesByParameters(a)) {
							m_log.issueError("Duplicate message key detected in "+msg.getName(), a.getSource(), at.getSource());
						}
					}
					keys.add(a);
				}
			});
		});

	}
	
	private void checkForMessageTopics() {
		m_modules.forEach(mod -> {
			mod.getMessages().forEachOfType(MessageField.class, false, msg -> {
				Annotation a = msg.getAnnotation(Annotation.KnownAnnotation.TOPIC.getName());
				
				if(a == null) {
					m_log.issueError("No topic annotation found for message"+msg.getName(), msg.getCoord());
				}
			});
		});
	}
	/**
	 * Calculates the field indeces for messages, based on the word packing rules
	 */
	private void computeIndeces() {
		
		m_messages.forEachOfType(MessageField.class, false, mf -> {
			
			
			
			if(mf.useCdrNotBlueberry()) {
				m_log.issueSkipped("CDR serialization not supported yet", mf.getCoord());
				//TODO: add a field packer for CDR packing
			} else {
				BlueberryFieldPacker.pack(mf);
			}
		});
		
	}
	
	/**
	 * scan through all messages and assign any moduie keys that have not been assigned.
	 * Only the second from the top level gets a key, every child module assumes the same key
	 */
	private void fillInMissingModuleKeyValues() {
		ListIterator<BlueModule> ms = m_modules.listIterator();
		while(ms.hasNext()) {
			BlueModule m = ms.next();
			ScopeName sn = m.getName();
			ScopeName sn2 = sn.getAncestorOfLevels(2);
			int n = sn.getNumberOfLevels();
			if(n > 2) {
				//we're over two levels so need to:
				//check for 2-level parent - if it exists
				BlueModule match2 = null;
				for(BlueModule mt : m_modules) {
					if(mt.equals(sn2)) {
						//we found a match
						match2 = mt;
						break;
					}
				}
				if(match2 == null) {
					//there was no match so add one
					ms.add(new BlueModule(sn2, null));
					ms.previous();//step back in the iterator so the new element will get processed
				}
			} else if(n == 2) {
				//we're a two level module so make sure it has a module_key
				Annotation a = m.getAnnotation(Annotation.KnownAnnotation.MODULE_KEY.getName());
				if(a == null) {
					//ok, so it doesn't have a module key
					a = new Annotation(Annotation.KnownAnnotation.MODULE_KEY.getName(),null);
					long k = getNextModuleKey();
					a.addParameter(new Number(k));
					m.addAnnotation(a);
					m_log.issueNote("Adding module key for "+sn.toUpperCamel("::")+" --> "+WriterUtils.formatAsHex(k), m.getCoord());
				}
			} else {
				//we're less than two levels so don't care
			}
		}
	}
	/**
	 * scan through all messages and assign any message keys that have not been assigned.
	 */
	private void fillInMissingMessageKeyValues() {
		for(BlueModule m : m_modules) {
			m.getMessages().forEachOfType(MessageField.class, false, mf -> {
				Annotation a = mf.getAnnotation(Annotation.KnownAnnotation.MESSAGE_KEY.getName());
				if(a == null || a.getParameter(Number.class) == null) {
					 a = new Annotation(Annotation.KnownAnnotation.MESSAGE_KEY.getName(), null);
					 long h = makeHashKey(mf);
//					 long i = getNextMessageKey(m);
					 
					 while(doesMessageKeyExist(m, h)) {
						 ++h;
					 }
					 
					a.addParameter(new Number(h));
					mf.addAnnotation(a);
					m_log.issueNote("Adding message_key for "+mf.getTypeName()+" message  -> "+WriterUtils.formatAsHex(h), mf.getCoord());
				}
					
			});
		}
		
	}
	private boolean doesModuleKeyExist(long i) {
		boolean result = false;
		for(BlueModule m : m_modules) {
			Annotation a = m.getAnnotation(Annotation.KnownAnnotation.MODULE_KEY.getName());
			if(a != null) {
				Number n = a.getParameter(Number.class);
				if(n != null && n.asLong() == i) {
					result = true;
					break;
				}
			}
		
		}
		return result;
	}
	private boolean doesMessageKeyExist(BlueModule m, long h) {
		boolean result = false;
		int n = m.getMessages().size();
		for(int i = 0; i < n; ++i) {
			MessageField msg = (MessageField)m.getMessages().get(i);
			Annotation a = msg.getAnnotation(Annotation.KnownAnnotation.MODULE_KEY.getName());
			if(a != null) {
				Number nt = a.getParameter(Number.class);
				if(nt != null && nt.asLong() == h) {
					result = true;
					break;
				}
			}
		
		}
		return result;
	}

	/**
	 * Makes a key for a specified module
	 * Does this by first making a string from the absolute scoped module name with "::" separator, in lower-case
	 * @param m - the module to compute the hash for
	 * @return - the 16 bit hash
	 */
	private int makeHashKey(BlueModule m) {
		return makeHashKey(m.getName().toLowerCamel("::").toLowerCase());
	}
	/**
	 * Makes a key for the specified message field
	 * Does this by first making a string from the message name (without module scope) in lower case
	 * @param msg - the message to compute the hash for
	 * @return - the 16 bit hash
	 */
	private int makeHashKey(MessageField msg) {
		return makeHashKey(msg.getTypeName().toSymbolName().toLowerCamelString().toLowerCase());
	}
	private int makeHashKey(String n) {
		Crc1021 crc = new Crc1021();
		try {
			crc.addBytes(n.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return crc.getCrc();
	}

	/**
	 * picks the next available message key value
	 * @return
	 */
	private long getNextMessageKey(BlueModule m) {
		final ArrayList<Integer> keys = new ArrayList<>();
		m.getMessages().forEachOfType(MessageField.class, false, mf -> {
			Annotation a = mf.getAnnotation(Annotation.KnownAnnotation.MESSAGE_KEY.getName());
			if(a != null) {
				Number an = a.getParameter(Number.class);
				if(an != null) {
					keys.add(an.asInt());
				}
			}
			
		});
		Collections.sort(keys);
		int i = 0;
		//this assumes that the lowest value module should be the lowest possible value
		if(keys.size() > 0) {
			i = keys.get(0);
		}
		//find the first value that does not exist
		boolean done = false;
		while(!done) {
			done = true;
			for(Integer k : keys) {
				if(i == k) {
					++i;
					done = false;
					break;
				} else if(k > i){
					done = true;
				}
			}
		}
		
		return i;
	}
	/**
	 * picks the next available message key value
	 * @return
	 */
	private long getNextModuleKey() {
		final ArrayList<Integer> keys = new ArrayList<>();
		m_modules.forEach(m -> {
			Annotation a = m.getAnnotation(Annotation.KnownAnnotation.MODULE_KEY.getName());
			if(a != null) {
				Number an = a.getParameter(Number.class);
				if(an != null) {
					keys.add(an.asInt());
				}
			}
			
		});
		Collections.sort(keys);
		int i = 0;
		//this assumes that the lowest value module should be the lowest possible value
		if(keys.size() > 0) {
			i = keys.get(0);
		}
		//find the first value that does not exist
		boolean done = false;
		while(!done) {
			done = true;
			for(Integer k : keys) {
				if(i == k) {
					++i;
					done = false;
					break;
				} else if(k > i){
					done = true;
				}
			}
		}
		
		return i;
	}
	/**
	 * scans through all SingleWordTokens and converts them to SymbolNameTokens
	 * Note that this should be called after scanning for numbers
	 */
	private void collapseSymbolNames() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			SingleWordToken swt = m_tokens.gotoNext(SingleWordToken.class);
			if(swt != null) {
				SymbolName sn = SymbolName.guess(swt.getName());
				SymbolNameToken snt = new SymbolNameToken(swt.getStart(), swt.getEnd(), sn);
				m_tokens.replace(swt, snt);
			}
		}
	}
	/**
	 * remove all white space tokens except if they precede or follow a scope separator
	 */
	private void collapseWhiteSpace() {
		//remove all spaces followed by tokens that should not be a problem
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken w = m_tokens.relativeId(0,TokenIdentifier.SPACE, TokenIdentifier.TAB);
			if(w != null) {
				IdentifierToken tNext = m_tokens.relativeId(1, TokenIdentifier.DOUBLE_COLON);
				IdentifierToken tPrev = m_tokens.relativeId(-1, TokenIdentifier.DOUBLE_COLON);
				if(tNext != null || tPrev != null) {
					m_tokens.next();
				} else {
					m_tokens.remove(w);
				}
			} else {
				m_tokens.next();
			}
		}
	}
	private void removeSemicolons() {
		//remove all spaces followed by tokens that should not be a problem
				m_tokens.resetIndex();
				while(m_tokens.isMore()) {
					IdentifierToken w = m_tokens.relativeId(0,TokenIdentifier.SEMICOLON);
					if(w != null) {
						
						m_tokens.remove(w);
						
					} else {
						m_tokens.next();
					}
				}
	}
	/**
	 * scan through tokens looking for a scope separator beside a single word token
	 */
	private void collapseScope() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken s = m_tokens.gotoNextId(TokenIdentifier.DOUBLE_COLON);
			if(s != null) {
				SymbolNameToken tName = m_tokens.relative(1, SymbolNameToken.class);
				ScopeNameToken tScopeBefore = m_tokens.relative(-1, ScopeNameToken.class);
				SymbolNameToken tSymbolBefore = m_tokens.relative(-1, SymbolNameToken.class);
				Token tBefore = m_tokens.relative(-1);
				ScopeName scopeBefore = null;
				SymbolName scopeAfter = tName != null ? tName.getSymbolName() : null;
				if(tScopeBefore != null) {
					scopeBefore = tScopeBefore.getScopeName();
				} else if(tSymbolBefore != null) {
					scopeBefore = ScopeName.wrap(tSymbolBefore.getSymbolName());
				}
				if(scopeBefore != null && scopeAfter != null) {
					//this should be a scoped name
					
					ScopeNameToken swt = new ScopeNameToken(tBefore.getStart(), tName.getEnd(), scopeBefore.addLevelBelow(scopeAfter));
					m_tokens.replace(tBefore, swt);
					m_tokens.remove(s);
					m_tokens.remove(tName);
				} else if(scopeBefore == null && scopeAfter != null){
					ScopeName sn = ScopeName.ROOT.addLevelBelow(scopeAfter);
					ScopeNameToken swt = new ScopeNameToken(s.getStart(), tName.getEnd(), sn);

					m_tokens.replace(s, swt);
					m_tokens.remove(tName);

				} else if(tName == null) {
					//this must be a root separator
					ScopeNameToken swt = new ScopeNameToken(s.getStart(), s.getEnd(), ScopeName.ROOT);

					m_tokens.replace(s, swt);
				}
			}
		}
	}
	/**
	 * check all annotations in the specified field list and replace any constants
	 */
	private void applyDeferredParameters(FieldList fs) {
		fs.forEach(f -> {
			f.scanAnnotations(a -> {
				a.replaceDeferredParameters(dp -> {
					Constant<?> c = lookupConstant(dp.name, dp.imports);
					if(c == null) {
						m_log.issueError("Cannot find constant of name: \""+dp.name.toLowerSnakeString()+"\"", a.getSource());
					}
					Object result = null;
					if(c != null) {
						result = c.getValue();
					}
					return result;
				});
			});
		});
	}

	/**
	 * method that returns a replacement field for a deferred field
	 * @param fs - this list to check for deferred fields
	 * @param defines - the defines that hopefully contains the definition for those fields
	 */
	private <T extends Field> void processDeferredFields(ListIterator<Field> fi, FieldList defines) {	
		while(fi.hasNext()) {
			Field f = fi.next();
			if(f instanceof DeferredField) {
				ScopeName[] imports = ((DeferredField) f).getImports();
				ScopeName typeName = f.getTypeName();
				Field dft = null;
				if(typeName == null && f.getTypeId() == null) {
					m_log.issueError("BlueberrySchemaParser.processDeferredFields type is somehow not defined.", f.getCoord());
				} else if(typeName != null) {
				
				
					for(Field df : defines.getList()) {
						if(df.getTypeName() == null) {
							m_log.issueError("Somehow there's a defined type without a type name.", df.getCoord());
						}
						
						if(df.getTypeName().isMatch(imports, typeName)) {
							if(dft != null) {
								m_log.issueWarning("Type shadowing: "+typeName.toUpperCamelString(), df.getCoord(), dft.getCoord());
							} else {
								dft = df;
							}	
						}
					}
					if(dft == null) {
						m_log.issueError("Could not find a type definition for \""+typeName+"\"", f.getCoord());
					} else {
						fi.set(dft.makeInstance(f.getName()));
					}
				}
			} else if(f instanceof ParentField) {
				processDeferredFields(((ParentField)f).getChildren().getIterator(), defines);
			}
		}
		
	}
	/**
	 * typedef keyword is not required before enum, sequence and struct keywords so remove it
	 */
	private void collapseTypedefs() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken typedef = m_tokens.gotoNextId(TokenIdentifier.TYPEDEF);
			if(typedef != null) {
				IdentifierToken id = m_tokens.relative(1, IdentifierToken.class);
				if(id != null) {
					switch(id.getKeyword()) {
					case SEQUENCE:
						m_tokens.next();
						m_tokens.remove(typedef);
						IdentifierToken id2 = new IdentifierToken(typedef.getStart(), id.getEnd(), TokenIdentifier.TYPEDEF_SEQUENCE);
						m_tokens.replace(id, id2);
						break;
					case ENUM:
					case STRUCT:
						m_tokens.next();
						m_tokens.remove(typedef);
						break;
					default:
						m_tokens.next();
						break;
					}
				} else {
					m_tokens.next();
				}
			}

		}
	}
	/**
	 * replace integer types preceded with the unsigned keyword with the appropriate unsigned type
	 */
	private void collapseUnsigned() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			BaseTypeToken it = m_tokens.gotoNext(BaseTypeToken.class);
			if(it != null && it.getKeyword() == TokenIdentifier.UNSIGNED) {
				BaseTypeToken btt = m_tokens.relative(1, BaseTypeToken.class);
				TokenIdentifier newTi = null;
				if(btt == null) {
					m_log.issueError("Unsigned keyword must be followed by a base type.", it.getEnd());
					m_tokens.gotoSemi();
				} else {
					switch(btt.getKeyword()) {
					case BYTE:
					case INT8:
						newTi = TokenIdentifier.UINT8;
						break;
					case SHORT:
					case INT16:
						newTi = TokenIdentifier.UINT16;
						break;
					case LONG:
					case INT:
					case INT32:
						newTi = TokenIdentifier.UINT32;
						break;
					case INT64:
					case LONG_LONG:
						newTi = TokenIdentifier.UINT64;
						break;
					default:
						m_log.issueError("Unsigned keyword makes no sense combined with "+btt.getName(), btt.getStart());
					}
					BaseTypeToken newBtt = BaseTypeToken.makeNew(new IdentifierToken(it.getStart(), btt.getEnd(), newTi));
					m_tokens.replace(btt, newBtt);
					m_tokens.remove(it);
				}
			}
			m_tokens.next();
		}

	}
	/**
	 * scans through the whole token list and creates all the fields
	 */
	private void assembleFieldsV4() {
		m_moduleStack.clear();
		m_moduleEnd.clear();
		m_modules.clear();
		m_lastComment = null;
		m_tokens.resetIndex();

		while(m_tokens.isMore()) {
			Token t = m_tokens.getCurrent();
			BaseTypeToken baseType = m_tokens.getCurrentAs(BaseTypeToken.class);
			SymbolNameToken typeName = m_tokens.getCurrentAs(SymbolNameToken.class);
			
			
			EolToken eol = m_tokens.findNext(t, EolToken.class);//this is the end of the line
			IdentifierToken eq = m_tokens.findNextId(t, eol, TokenIdentifier.EQUALS);
			
			IdentifierToken ob = m_tokens.findNextId(t, eol, TokenIdentifier.SQUARE_BRACKET_START);
			
			//look for square brackets indicating an array
			IdentifierToken cb = ob == null ? null : m_tokens.matchBracket(ob);
			IdentifierToken lte = cb == null ? null : m_tokens.findNextId(ob, cb, TokenIdentifier.LESS_THAN_OR_EQUALS);
			NumberToken arraySizeNumber = cb == null ? null : m_tokens.findNext(ob, cb, NumberToken.class);
			
			
			SymbolNameToken fieldName = m_tokens.findNext(m_tokens.getLastNonNull(baseType, cb),m_tokens.getFirstNonNull(eol, eq), SymbolNameToken.class);
			
			
			//look for a value that indicates either a default or constant value
			NumberToken valueNumber = m_tokens.findNext(m_tokens.getLastNonNull(eq, cb), eol, NumberToken.class);
			SymbolNameToken valueName = m_tokens.findNext(m_tokens.getLastNonNull(eq, cb), eol, SymbolNameToken.class);
			//this is an array of base types
 			boolean fixed = arraySizeNumber != null && lte == null;
			int n = 0;
			if(arraySizeNumber != null) {
				n = arraySizeNumber.getNumber().asInt();
			
			}
			ArrayField af = (cb == null || fieldName == null || baseType == null) ? null :new ArrayField(fieldName.getSymbolName(), null, new int[] {n}, fixed, m_lastComment, baseType.getStart());

			
			if(baseType != null) {
				
				 if(baseType.check(TokenIdentifier.STRING)) {
					//it's a string or an array of strings
					//the line will look like this
					//[base type string][symbol name][<=][number][=][string]
	
					//check for a default or constant value
					StringToken s = m_tokens.findNext(baseType, eol, StringToken.class);
					//check for bounding on the string size
					Token backstop = m_tokens.getFirstNonNull(eol, s, ob);
					IdentifierToken slte = m_tokens.findNextId(fieldName, backstop,TokenIdentifier.LESS_THAN_OR_EQUALS);
					NumberToken ss = slte == null ? null : m_tokens.findNext(slte, backstop, NumberToken.class);
					int m = 0;
					if(ss != null) {
						m = ss.getNumber().asInt();
					}
					//this is just a string
					
					if(eq != null) {
						//this is a string constant
						//TODO: do this part
					} else if(af != null) {
						//this is an array of strings
						
						
						StringField sf = new StringField(null, null, m, m_lastComment, baseType.getStart());
						af.add(sf);
						m_message.add(af);
						m_tokens.gotoEol();
					} else {
						
						StringField sf = new StringField(fieldName.getSymbolName(), null, m, m_lastComment, baseType.getStart());
						m_message.add(sf);
						m_tokens.gotoEol();
					}
				//this is either a base type field or constant, or perhaps an array or sequence
				 } else if(eq != null) {
					//it's a constant
					if(af != null) {
						//there's an array bracket and that should not be a constant
						m_log.issueError("Can't have a constant array, sorry.", ob.getStart());
					} else {
						Number num = valueNumber.getNumber();
						NumberConstant nc = new NumberConstant(lookupBaseType(baseType.getKeyword()), fieldName.getSymbolName(), num, m_lastComment);
						m_modules.getLast().addConstant(nc);
					}
					m_tokens.gotoEol();

		 		} else if(af != null) {
					BaseField bf = new BaseField(null, lookupBaseType(baseType.getKeyword()), m_lastComment, baseType.getStart());
					af.add(bf);
					m_message.add(af);
					m_tokens.gotoEol();

				} else {
					//this a normal base type
					BaseField bf = new BaseField(fieldName.getSymbolName(), lookupBaseType(baseType.getKeyword()), m_lastComment, baseType.getStart());
					m_message.add(bf);
					m_tokens.gotoEol();

				}
				m_lastComment = null;
			} else if(typeName != null) {
				//this is either a struct field or a struct array, or struct sequence
				//in v4 this means the type is defined by another message
				DeferredField df = new DeferredField(fieldName.getSymbolName(), m_modules.getLast().scope(typeName.getSymbolName()), null, m_lastComment, typeName.getStart());
				if(af != null) {
					//it's an array of structs (messages)
					af.add(df);
					m_message.add(af);
					m_tokens.gotoEol();

				} else {
					//it's just a struct
					m_message.add(df);
					m_tokens.gotoEol();

				}
				m_lastComment = null;
				
			} else if(t instanceof FilePathToken) {
				FilePathToken fpt = (FilePathToken)t;
				m_fileName = fpt.getString();
				m_imports.clear();
				m_moduleStack.clear();
				m_moduleStack.add(BlueModule.ROOT);
				String msgName = fpt.getPathElementFromEnd(0).replace(".msg", "");//this should strip off the msg file extension
				//for now assume that the name of the directory containing the msg folder is the package (or module as I'm calling it)
				//also, the file name is the message name

				BlueModule m = new BlueModule(ScopeName.make(SymbolName.Case.LOWER_SNAKE, "/", fpt.getPathElementFromEnd(2)), fpt.getStart());
				int i = m_modules.indexOf(m);
				if(i >= 0) {
					m = m_modules.get(i);
				} else {
					
					m_modules.add(m);
				}
				
				m_moduleStack.add(m);
				ScopeName sn = m_moduleStack.getLast().scope(SymbolName.parse(Case.UPPER_CAMEL, msgName));
				m_message = new MessageField(SymbolName.EMPTY, sn, m_lastComment, t.getStart());
				m_moduleStack.getLast().getMessages().add(m_message);
				m_messages.add(m_message);
				m_moduleEnd.clear();
			} else if(t instanceof CommentToken) {
				CommentToken ct = (CommentToken)t;
				m_lastComment = ct.combineLines();
			}
			m_tokens.next();

		}
	}


	/**
	 * scans through the whole token list and creates all the fields
	 */
	private void assembleFieldsV3() {
		m_moduleStack.clear();
		m_moduleEnd.clear();
		m_modules.clear();
		m_lastComment = null;
		m_tokens.resetIndex();

		while(m_tokens.isMore()) {
			Token t = m_tokens.getCurrent();
			//check if we've hit the closing brace of the current module - if there is one
			if(!m_moduleEnd.isEmpty() && !m_tokens.isCurrentBefore(m_moduleEnd.getLast())) {
				m_moduleStack.removeLast();
				m_moduleEnd.removeLast();
				m_tokens.next();//skip past last brace
			}
			IdentifierToken it = m_tokens.relative(0,IdentifierToken.class);
			if(it != null) {
				switch(it.getKeyword()) {
				case CONST:
					assembleConst(it);
					break;
				case ENUM:
					assembleEnum(it);
					break;
				case IMPORT:
					assembleImport(it);
					break;
				case MESSAGE:
					assembleMessage(it);
					break;
				case MODULE:
					assembleModule(it);
					break;
				case TYPEDEF_SEQUENCE:
					assembleSequence(it);
					break;
				case STRUCT:
					assembleStructs(it);
					break;
				case TYPEDEF:
					assembleTypedef(it);
					break;
				case ANNOTATION_START:
					assembleAnnotation(it);
					break;
				case SEMICOLON:
					//nothing to do
					break;
				default:
					m_log.issueNote("Did not process "+it+". This is probably not right.", it.getStart());
					break;
				}


			} else if(t instanceof FilePathToken) {
				FilePathToken fpt = (FilePathToken)t;
				m_fileName = fpt.getString();
				m_imports.clear();
				m_moduleStack.clear();
				m_moduleStack.add(BlueModule.ROOT);
				m_moduleEnd.clear();
			} else if(t instanceof CommentToken) {
				CommentToken ct = (CommentToken)t;
				m_lastComment = ct.combineLines();
			}
			m_tokens.next();

		}
	}

	private void assembleImport(IdentifierToken it) {
		m_lastComment = null;//clear this here. 
		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		if(nameToken == null) {
			m_log.issueError("Import statement does not have a name specified", it.getStart());
			m_tokens.gotoSemi();
		}
		ScopeName scope = ScopeName.wrap(nameToken.getSymbolName());
		if(!scope.isAbsolute()) {
			scope = scope.addRoot();
		}
		m_imports.add(scope);
		m_tokens.setIndex(nameToken);
	}
	/**
	 * process a struct statement and all the fields in the following braces
	 * The fields are actually processed by a helper method
	 * @param it
	 */
	private void assembleStructs(IdentifierToken it) {

		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = matchBracket(braceStart);
		if(braceStart == null) {
			m_log.issueError("Struct has no opening brace. Skipping rest of file.", it.getEnd());
			m_tokens.gotoNextFile();
		} else if(nameToken == null) {
			m_log.issueError("Struct has no specified name. Skipping rest of file.", it.getEnd());
			m_tokens.gotoNextFile();
		} else {
			ScopeName name = m_moduleStack.getLast().scope(nameToken.getSymbolName());
	
	
			StructField m = new StructField(SymbolName.EMPTY, name, m_lastComment, it.getStart());
			m.setFileName(m_fileName);
			m_lastComment = null;
	
			m_defines.add(m);
			m_moduleStack.getLast().getDefines().add(m);
			m.addAnnotation(m_annotations);
			m_annotations.clear();
			processMessageOrStructFields(m, braceStart, braceEnd);
		}
		
	}
	/**
	 * process a message statement and all the fields in the following braces
	 * The fields are actually processed by a helper method
	 * A message is like a strut whose purpose is to be sent over the network
	 * @param it
	 */
	private void assembleMessage(IdentifierToken it) {
		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = matchBracket(braceStart);
		if(nameToken == null) {
			m_log.issueError("Message has no specified name. Skipping rest of file.", it.getEnd());
			m_tokens.gotoNextFile();
		} else if(braceStart == null) {
			m_log.issueError("Message statement has no opening brace. Skipping rest of file.", it.getEnd());
			m_tokens.gotoNextFile();
		} else if(braceEnd == null) {
			m_log.issueError("Message statement has no closing brace.", braceStart.getEnd());
			m_tokens.gotoNextFile();

		} else {
			ScopeName name = m_moduleStack.getLast().scope(nameToken.getSymbolName());
	
	
			MessageField m = new MessageField(SymbolName.EMPTY, name, m_lastComment, it.getEnd());
			m.setFileName(m_fileName);
			m_lastComment = null;
	
			m_messages.add(m);
			m_moduleStack.getLast().getMessages().add(m);
			m.addAnnotation(m_annotations);
			m_annotations.clear();
			processMessageOrStructFields(m, braceStart, braceEnd);
		}
	}
	private void processMessageOrStructFields(ParentField m, IdentifierToken braceStart, IdentifierToken braceEnd) {
		m_tokens.setIndex(braceStart);
		m_tokens.next();
		while(m_tokens.isCurrentBefore(braceEnd)) {
			if(IdentifierToken.check(m_tokens.getCurrent(), TokenIdentifier.ANNOTATION_START)) {
				assembleAnnotation((IdentifierToken)m_tokens.getCurrent());
				 
			} else if(m_tokens.getCurrent() instanceof CommentToken) {
				
			} else if(m_tokens.getCurrent() instanceof EolToken) {
			} else if(IdentifierToken.check(m_tokens.getCurrent(), TokenIdentifier.SEMICOLON)){
			
			} else {
			
				//reuse a bunch of fields from above for the sub-field
				CommentToken ct = m_tokens.relative(-1, CommentToken.class);
				String comment = ct != null ? ct.combineLines() : null;
				SymbolNameToken typeNameToken = m_tokens.relative(0, SymbolNameToken.class);//or this
				ScopeNameToken typeScopeNameToken = m_tokens.relative(0, ScopeNameToken.class);
				ScopeName tn = typeScopeNameToken != null ? typeScopeNameToken.getScopeName() : typeNameToken != null ? ScopeName.wrap(typeNameToken.getSymbolName()) : null;

				BaseTypeToken btt = m_tokens.relative(0, BaseTypeToken.class);//or this
				if(btt == null && tn == null) {
					m_log.issueError("Expecting a type name.", m_tokens.getCurrent().getStart());
				} else if(btt != null && btt.getKeyword() == TokenIdentifier.STRING) {
					//it's a string
					m.add(processString(btt, true, null, ct));
					
				} else {
					SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
					if(nameToken == null) {
						m_log.issueError("No name specified for field", m_tokens.getCurrent().getEnd());
					} else if(btt != null) {
						//there's a base type identifier so we're either adding a string or a numerical base type
						
					
						
						//add a base type field
						m.add(new BaseField(nameToken.getSymbolName(), lookupBaseType(btt.getKeyword()), comment, nameToken.getEnd()));
						
						
					} else {//must be a defined type field
						//we have to defer looking this up for now
						DeferredField df = new DeferredField(nameToken.getSymbolName(), tn, getImports(true), comment, nameToken.getStart());
						m.add(df);
							
					}
				}
				
				m_tokens.gotoSemi();
			}
			m_tokens.next();
		}
		m_tokens.setIndex(braceEnd);
		
	}
	/**
	 * of the form <comment?><typdef><sequence><angle bracket start><constituentTypeName><comma?><number?><angle bracket end><sequenceTypeName>
	 * except this method assumes that the <typedef> has already been removed
	 * @param it - the sequence token
	 */
	private void assembleSequence(IdentifierToken it) {

		IdentifierToken angleBracketStart = m_tokens.relativeId(1, TokenIdentifier.ANGLE_BRACKET_START);
		BaseTypeToken btt = m_tokens.relative(2, BaseTypeToken.class);
		SymbolNameToken snt = m_tokens.relative(2, SymbolNameToken.class);
		Field cf = null;
		if(angleBracketStart == null) {
			m_log.issueError("Sequence keyword should be followed by an open angle bracket.", it.getEnd());
			
		} else if(btt != null) {
			TypeId tid = lookupBaseType(btt.getKeyword());
			if(btt.getKeyword() == TokenIdentifier.STRING) {
				cf = processString(btt, false, null, null);
				
			} else {
				cf = new BaseField(null, tid, null, btt.getEnd());
			}
		} else if(snt != null) {
			cf = new DeferredField(null, ScopeName.wrap(snt.getSymbolName()), getImports(true), null, snt.getStart());
		} else {
			
			m_log.issueError("Sequence must be defined with a type for its elements.", it.getEnd());
			
		}
		if(cf != null) {
			IdentifierToken commaT = m_tokens.relativeId(3, TokenIdentifier.COMMA);
			NumberToken nt = m_tokens.relative(4, NumberToken.class);
			int n = -1;
			if(commaT != null && nt != null) {
				n = nt.getNumber().asInt();
			}
			IdentifierToken angleBracketEnd = matchBracket(angleBracketStart);
			if(angleBracketEnd == null) {
				m_log.issueError("Starting angle brackets must have closing bracket too. Skipping rest of file.", angleBracketStart.getEnd());
				m_tokens.gotoNextFile();
			} else {
			
				m_tokens.setIndex(angleBracketEnd);
				SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);
				if(nameToken == null) {
					m_log.issueError("Sequence needs a type name specified.", angleBracketEnd.getEnd());
				} else {
					m_tokens.setIndex(angleBracketEnd);
					ScopeName name = m_moduleStack.getLast().scope(nameToken.getSymbolName());
					SequenceField sf = new SequenceField(null, name, m_lastComment, it.getEnd());
					sf.setFileName(m_fileName);
					sf.add(cf);
					sf.setLimit(n);
					m_lastComment = null;
					m_defines.add(sf);
					m_moduleStack.getLast().getDefines().add(sf);
				}
			}
			
		
		}
		m_tokens.gotoSemi();
	}


	/**
	 * processes a string token at the current token list position
	 * @param it
	 * @param findName - if true then expects a field name to occur after
	 * @return
	 */
	private StringField processString(IdentifierToken it, boolean findName, ScopeName typeName, CommentToken ct)  {
		StringField result = null;
		IdentifierToken angleBracketStart = m_tokens.relativeId(it, 1, TokenIdentifier.ANGLE_BRACKET_START);
		int maxSize = 65536;
		SymbolNameToken nameT = null;
		if(angleBracketStart != null) {
			//todo trap number token
			NumberToken nt = m_tokens.relative(2,NumberToken.class);
			IdentifierToken angleBracketEnd = matchBracket(angleBracketStart);
			if(angleBracketEnd == null) {
				m_log.issueError("Starting angle brackets must have closing bracket too.", angleBracketStart.getEnd());
				m_tokens.gotoNextFile();
			} else if(nt == null) {
				m_log.issueError("Angle brackets in a string definition should contain a number.", angleBracketStart.getEnd());
				m_tokens.gotoSemi();
			} else {
			maxSize = nt.getNumber().asInt();
			
			nameT = m_tokens.relative(angleBracketEnd, 1, SymbolNameToken.class);
			}
		} else {
			nameT = m_tokens.relative(it, 1, SymbolNameToken.class);
			
		}
		
		
		
		if(nameT != null) {
		
	//		SymbolName name = m_module.getLast().addLevel(nameToken.getSymbolName());
			SymbolName name = (findName && nameT != null) ? nameT.getSymbolName() : null;
			result = new StringField(name, typeName, maxSize, ct == null ? "" : ct.combineLines(), it.getEnd());
			result.setFileName(m_fileName);
		
		}
		return result;
	}


	/**
	 * of the form <comment?><typedef><constituentTypeName><typeName><square bracket start?><number?><square bracket end>
	 * @param it
	 */
	private void assembleTypedef(IdentifierToken it) {

		SymbolNameToken typeName = m_tokens.relative(1, SymbolNameToken.class);//this is the original type that this typedef is based on 
		BaseTypeToken btt = m_tokens.relative(1, BaseTypeToken.class);//this could also be the original type depending on whether it's a base type or not
		SymbolNameToken name = m_tokens.relative(2, SymbolNameToken.class);//name of new type
		
		//check for valid name and type
		if(name == null) {
			m_log.issueError("No type name specified for this typedef.",it.getEnd());
			m_tokens.gotoSemi();
		} else if(btt == null && typeName == null) {
			m_log.issueError("No type specified for this typedef.",it.getEnd());
			m_tokens.gotoSemi();
		} else {
			ScopeName scopedName = m_moduleStack.getLast().scope(name.getSymbolName());
			
			
			//check for arrays and keep track of the dimensions
			ArrayList<Integer> dims = new ArrayList<>();
			boolean keepGoing = true;
			
			IdentifierToken squareBracketStart  = null;	
			NumberToken arraySize = null;
			SymbolNameToken arraySizeConst = null;	
			IdentifierToken squareBracketEnd = null;
			Token lastValidEnd = name;
			
			int i = 3;
			
			while(keepGoing) {
				squareBracketStart = m_tokens.relativeId(i, TokenIdentifier.SQUARE_BRACKET_START);
				arraySize = m_tokens.relative(i + 1, NumberToken.class);
				arraySizeConst = m_tokens.relative(i + 1,SymbolNameToken.class);
				squareBracketEnd = m_tokens.relativeId(i + 2, TokenIdentifier.SQUARE_BRACKET_END);
				i += 3;
				
				
				if(squareBracketStart != null) {
					if(squareBracketEnd == null) {
						m_log.issueError("Starting square bracket does not have a closing bracket.", squareBracketStart.getEnd());
						m_tokens.gotoNextFile();
						break;
					} else if(arraySize == null && arraySizeConst == null) {
						m_log.issueError("Array definition needs a size specified.", squareBracketStart.getEnd());
						m_tokens.gotoSemi();
						break;
					} else {
					
					
						int n = arraySize != null ? arraySize.getNumber().asInt() : lookupConstInt(arraySizeConst.getSymbolName(), getImports(true));
						dims.add(n);
						lastValidEnd = squareBracketEnd;
					}
				} else {
					keepGoing = false;
				}
			}
			
			
			
			if(btt != null && btt.getKeyword() == TokenIdentifier.STRING) {
				m_log.issueSkipped("Typedef doesn't work with Strings yet.", btt.getStart());
				m_tokens.gotoSemi();
			} else {
			
				TypeId id = (btt != null) ? lookupBaseType(btt.getKeyword()) : TypeId.DEFERRED;
				
				ParentField pf;
				if(dims.size() == 0) {
				
					//this is a normal base type
					pf = new DefinedTypeField(null, scopedName, m_lastComment, it.getEnd());
		//			TypeDefField tdf = new TypeDefField(SymbolName.EMPTY, scopedName, id, m_lastComment);
				
					pf.setFileName(m_fileName);
					
					
		
		
				} else {
					
					//this is an array type
		
				
					int[] ds = new int[dims.size()];
					for(int j = 0; j < dims.size(); ++j) {
						ds[j] = dims.get(j);
					}
					pf = new ArrayField(null, scopedName, ds, true, m_lastComment, name.getEnd());
					
					pf.setFileName(m_fileName);
					
		
		
				}
				m_defines.add(pf);
				m_moduleStack.getLast().getDefines().add(pf);
				//now add a field to contain the target type of this define
				if(btt != null) {
					pf.add(new BaseField(null, lookupBaseType(btt.getKeyword()), null, btt.getEnd()));
		
				} else {
					//we must defer this
					pf.add(new DeferredField(null, ScopeName.wrap(typeName.getSymbolName()), getImports(true), m_lastComment, typeName.getEnd()));
				}
		
				m_lastComment = null;
				m_tokens.setIndex(lastValidEnd);
			}
		}
	}
	/**
	 * Looks up a symbol name in the constant list and returns its integer value if it exists
	 * TODO: make it look through the imports
	 * @param name
	 * @return
	 */
	private int lookupConstInt(SymbolName name, ScopeName[] imports) {
		NumberConstant result = null;
		Constant<?> c = lookupConstant(name, imports);
		if(c instanceof NumberConstant) {
			result = (NumberConstant)c;
		}
		return result != null ? result.getValue().asInt() : -1;
	}
	
	/**
	 * scans through all known modules to find a constant with the specified name, given the list of imports
	 * @param n
	 * @param imports
	 * @return
	 */
	private Constant<?> lookupConstant(SymbolName n, ScopeName[] imports){
		Constant<?> result = null;
		for(ScopeName sn : imports) {
			BlueModule found = null;
			for(int i = 0; i < m_modules.size(); ++i) {
				BlueModule m = m_modules.get(i);
				if(m.getName().equals(sn)) {
					found = m;
					break;
				}
			}
			if(found != null) {
				result = found.getConstant(n);
				break;
			}
		}
		
		return result;
	}
	private void assembleEnum(IdentifierToken enumT) {


		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);
		IdentifierToken colon = m_tokens.relativeId(2, TokenIdentifier.COLON);
		BaseTypeToken btt = m_tokens.relative(3, BaseTypeToken.class);
		IdentifierToken braceStart = m_tokens.relativeId(colon == null ? 2 : 4, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = matchBracket(braceStart);
		if(braceStart == null){
			m_log.issueError("Enum needs a body with members. Skipping to next file.", enumT.getEnd());
			m_tokens.gotoNextFile();
		} else if(braceEnd == null) {
			m_log.issueError("Enum body has no closing brace. Skipping to next file.", enumT.getEnd());
			m_tokens.gotoNextFile();
		} else if(nameToken == null) {
			m_log.issueError("Enum name not specified.", enumT.getEnd());
			m_tokens.setIndex(braceEnd);
		} else {

			TypeId bt = (btt != null) ? lookupBaseType(btt.getKeyword()) : TypeId.UINT32;
			ScopeName name = m_moduleStack.getLast().scope(nameToken.getSymbolName());
			EnumField et = new EnumField(SymbolName.EMPTY, name, bt, m_lastComment, enumT.getEnd());
			et.setFileName(m_fileName);
			m_lastComment = null;
			m_defines.add(et);
			m_moduleStack.getLast().getDefines().add(et);
	
			m_tokens.setIndex(braceStart);
	
			while(m_tokens.isCurrentBefore(braceEnd)) {
				Token t = m_tokens.gotoNextOfThese(braceEnd, SymbolNameToken.class, NameValueToken.class, braceEnd.getClass());
				if(t == null || t == braceEnd) {
					break;
				}
				//reuse a bunch of fields from above for the sub-field
				CommentToken ct = m_tokens.relative(-1, CommentToken.class);
				String comment = ct != null ? ct.combineLines() : null;
				nameToken = m_tokens.relative(0, SymbolNameToken.class);
				NameValueToken<?> nvt = m_tokens.relative(0, NameValueToken.class);
				Number n = null;
				if(nvt != null && nvt.getValue() instanceof Number) {
					n = (Number)nvt.getValue();
				}
				if(nameToken != null) {
					et.addNameValue(nameToken.getSymbolName(), Number.NAN, comment);
				} else if(n != null) {
					try {
						et.addNameValue(nvt.getSymbolName(), n, comment);
					} catch(RuntimeException e) {
						System.out.println("BlueberrySchemaParser.processEnum ");
					}
					
				}
	
	
	
				m_tokens.setIndex(m_tokens.relative(1));
	
			}
		}

	}
	/**
	 *
	 * @param it
	 */
	private void assembleConst(IdentifierToken it) {
//		CommentToken ct = m_tokens.relative(-1, CommentToken.class);
//		String comment = ct != null ? ct.combineLines() : null;

		BaseTypeToken btt = m_tokens.relative(1, BaseTypeToken.class);
		NameValueToken<?> nvt = m_tokens.relative(2, NameValueToken.class);
		boolean nvtGood = nvt != null;
		SymbolName name = nvtGood ? m_tokens.relative(2, NameValueToken.class).getSymbolName() : null;
		Object val = nvtGood ? m_tokens.relative(2, NameValueToken.class).getValue() : null;
		Number nVal = val instanceof Number ? (Number)val : null;
		String sVal = val instanceof String ? (String)val : null;
		
		TokenIdentifier idVal = val instanceof TokenIdentifier ? (TokenIdentifier)val : null;
		
		TokenIdentifier ti = btt != null ? btt.getKeyword() : null;
		if(btt == null) {
			m_log.issueSkipped("Only base types and Strings can be declared const so far.", it.getStart());
			m_tokens.gotoSemi();
		} else if(ti == TokenIdentifier.STRING || ti == TokenIdentifier.CHAR) {
			if(sVal == null) {
				m_log.issueError("Const string must include a string value", it.getStart());
				m_tokens.gotoSemi();
			} else {
				StringConstant c = new StringConstant(name, sVal, m_lastComment);
				c.setFileName(m_fileName);
				m_lastComment = null;
				m_moduleStack.getLast().addConstant(c);
			
			}
			
			


		
		} else if(btt.getKeyword() == TokenIdentifier.BOOLEAN) {
			//check for boolean
			Boolean b  = null;
			
			if(idVal != null){
				if(idVal == TokenIdentifier.TRUE) {
					b = Boolean.TRUE;
				} else if(idVal == TokenIdentifier.FALSE) {
					b = Boolean.FALSE;
				} else {
					m_log.issueError("Valid values are either true or false, not "+idVal, btt.getStart());
					
				}
			} else if(nVal != null){
				if(nVal.asFloat() > 0.0) {
					b = Boolean.TRUE; 
				} else if(nVal.asFloat() < 0.0) {
					m_log.issueError("Negative value is inappropriate for boolean type.", btt.getStart());
					
				}
			} else {
				m_log.issueError("Const must include a boolean value", btt.getStart());
				
			}
			
			if(b != null) {
			
				BooleanConstant c = new BooleanConstant(name, b, m_lastComment);
				c.setFileName(m_fileName);
				m_lastComment = null;
				
				m_moduleStack.getLast().addConstant(c);
			}
			
		} else {
				
			if(nVal == null) {
				Coord c = nvtGood ? nvt.getStart() : it.getEnd();
				String moreText = "";
				if(val instanceof SymbolName) {
					SymbolName sn = (SymbolName)val;
					moreText = sn.toLowerCaseString() + " could not be interpreted as a number."; 
					
				}
				m_log.issueSkipped("Const must include a name and value. "+moreText, c);
				
			} else {

				TypeId typeId = lookupBaseType(btt.getKeyword());
				if(typeId == null) {
					m_log.issueError("Something wrong with base type \""+btt.getKeyword()+"\"", btt.getStart());
				} else {
				
			
			
				
					NumberConstant c = new NumberConstant(typeId, name, nVal, m_lastComment);
					c.setFileName(m_fileName);
					m_lastComment = null;
				
			
					m_moduleStack.getLast().addConstant(c);
				}
			}
		
	
		}

		m_tokens.gotoSemi();


	}
	private void assembleModule(IdentifierToken it) {
		m_lastComment = null;
		SymbolNameToken moduleName = m_tokens.relative(1, SymbolNameToken.class);
		if(moduleName == null) {
			moduleName = m_tokens.relative(1, ScopeNameToken.class);
		}
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		if(braceStart == null) {
			m_log.issueError("Module should start with opening brace", it.getEnd());
			m_tokens.gotoSemi();
		} else if(moduleName == null){
			m_log.issueError("Module name is ill-formed.",it.getEnd());
			m_tokens.gotoSemi();
		} else {
			IdentifierToken braceEnd = matchBracket(braceStart);//this should never be null I think
			if(braceEnd == null) {
				m_log.issueError("Module statement open brace is never closed.", braceStart.getEnd());
				m_tokens.setIndex(m_tokens.getLast());
			}
			BlueModule m = m_moduleStack.getLast().makeChild(moduleName.getSymbolName(), moduleName.getStart());
			int i = m_modules.indexOf(m);
			if(i >= 0) {
				m = m_modules.get(i);
			} else {
				
				m_modules.add(m);
			}
			m.addAnnotation(m_annotations);
			m_annotations.clear();
			m_moduleStack.add(m);
			m_moduleEnd.add(braceEnd);
			
			m_tokens.setIndex(braceStart);

		}

	}









	/**
	 * Scans enum tokens and checks for duplicate values
	 */
	private void checkForDuplicateEnumValues() {
		m_defines.forEachOfType(EnumField.class, true, ef -> {
			for(NameValue nv1 : ef.getNameValues()) {
				for(NameValue nv2 : ef.getNameValues()) {
					if(nv1 == nv2) {
						//don't do anything, they're the same item
					} else if(nv1.getName().equals(nv2.getName())){
						//they have the same name
						m_log.issueError("Duplicate enum item names: "+nv1.getName().toUpperSnakeString(), ef.getCoord());
					} else if(nv1.getValue().equals(nv2.getValue())) {
						//they have the same value
						m_log.issueError("Duplicate enum item values: "+nv1.getName().toUpperSnakeString()+" = "+nv1.getValue(), ef.getCoord());
					}
				}
			}
		});
	}
	/**
	 * Scans for enum tokens that are missing values for their elements.
	 * Fills them in with the smallest, unused, positive, integer value.
	 */
	private void fillInMissingEnumValues() {
		m_defines.forEachOfType(EnumField.class, true, ef -> {
			ef.fillInMissingValues();
		});
	}


	/**
	 * converts from a token base type to a field type
	 * @param bt
	 * @return
	 */
	TypeId lookupBaseType(TokenIdentifier bt) {
		TypeId result = null;
		switch(bt) {
		case BOOL:
		case BOOLEAN:
			result = TypeId.BOOL;
			break;
		case FLOAT:
		case FLOAT32:
			result = TypeId.FLOAT32;
			break;
		case FLOAT64:
		case DOUBLE:
			result = TypeId.FLOAT64;
			break;
		case INT16:
			result = TypeId.INT16;
			break;
		case LONG:
		case INT32:
			result = TypeId.INT32;
			break;
		case INT8:
			result = TypeId.INT8;
			break;
		case UINT16:
			result = TypeId.UINT16;
			break;
		case UINT32:
			result = TypeId.UINT32;
			break;
		case UINT8:
			result = TypeId.UINT8;
			break;
		case UINT64:
		case LONG_LONG:
			result = TypeId.UINT64;
			break;
		case INT64:
			result = TypeId.INT64;
			break;
		case CHAR:
			result = TypeId.CHAR;
			break;
		default:
			break;

		}
		return result;

	}





	/**
	 * now collapse any token sequences of form CommentToken, SymbolNameToken, IdentifierToken (equals sign), NumberToken to a NameValueToken
	 */
	private void collapseNameValues() {

		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			Token equalsT = m_tokens.gotoNextId(TokenIdentifier.EQUALS);

			if(equalsT != null) {
				CommentToken commentT = m_tokens.relative(-2, CommentToken.class);
				SymbolNameToken nameT = m_tokens.relative(-1, SymbolNameToken.class);
				NumberToken numberT = m_tokens.relative(+1, NumberToken.class);
				StringToken stringT = m_tokens.relative(+1, StringToken.class);
				CharToken charT = m_tokens.relative(+1, CharToken.class);
				SymbolNameToken constT = m_tokens.relative(+1, SymbolNameToken.class);
				ScopeNameToken scopedConstT = m_tokens.relative(+1, ScopeNameToken.class);
				IdentifierToken iConstT = m_tokens.relative(+1, IdentifierToken.class);
				String comment = commentT != null ? commentT.combineLines() : null;
				if(nameT != null && numberT != null) {
					//this should be for a number
					NameValueToken<Number> nvt = new NameValueToken<Number>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), numberT.getNumber(), comment);
					m_tokens.replace(equalsT, nvt);
					m_tokens.remove(numberT);
					m_tokens.remove(nameT);
					m_tokens.remove(commentT);

				} else if(nameT != null && stringT != null){
					//this should be for a String
					NameValueToken<String> nvt = new NameValueToken<String>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), stringT.getString(), comment);

					m_tokens.remove(nameT);
					m_tokens.remove(commentT);
					m_tokens.remove(stringT);
					m_tokens.replace(equalsT, nvt);
				} else if(nameT != null && scopedConstT != null) {
					NameValueToken<ScopeName> nvt = new NameValueToken<ScopeName>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), scopedConstT.getScopeName(), comment);

					m_tokens.remove(nameT);
					m_tokens.remove(commentT);
					m_tokens.remove(scopedConstT);
					m_tokens.replace(equalsT, nvt);
				} else if(nameT != null && constT != null) {
					NameValueToken<SymbolName> nvt = new NameValueToken<SymbolName>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), constT.getSymbolName(), comment);
					m_tokens.remove(nameT);
					m_tokens.remove(commentT);
					m_tokens.remove(constT);
					m_tokens.replace(equalsT, nvt);
				} else if(nameT != null && iConstT != null) {
					NameValueToken<TokenIdentifier> nvt = new NameValueToken<TokenIdentifier>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), iConstT.getKeyword(), comment);
					m_tokens.remove(nameT);
					m_tokens.remove(commentT);
					m_tokens.remove(iConstT);
					m_tokens.replace(equalsT, nvt);
				} else {
					m_log.issueError("Incorrect tokens around equals.", equalsT.getStart());
				}
			} else {
				break;
			}
		}

	}
	/**
	 * replace any single word tokens that match token identifiers with identifier tokens
	 */
	private void collapseIdentifiers(TokenIdentifier...ids) {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			SingleWordToken swt = m_tokens.gotoNext(SingleWordToken.class);
			if(swt != null) {
				for(TokenIdentifier ti : ids) {
					if(ti.id().toLowerCase().equals(swt.getName().toLowerCase())) {
						IdentifierToken it = new IdentifierToken(swt.getStart(), swt.getEnd(), ti);
						m_tokens.replace(swt, it);
						
						break;
					}
					
				}
				m_tokens.next();
				
			}

		}
	}
	
	
	/**
	 * Tests all single word tokens and replaces with a Number token if it's formatted like a number
	 */
	private void collapseNumbers() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			SingleWordToken swt = m_tokens.gotoNext(SingleWordToken.class);
			if(swt == null) {
				break;
			} else {
				NumberToken nt = NumberToken.wrap(swt);
				if(nt == null) {
					m_tokens.next();
				} else {
					m_tokens.replace(swt, nt);
				}
			}

		}
	}
	
	
	/**
	 * Tests all single word tokens and replaces with a Number token if it's formatted like a number
	 */
	private void collapseLessThanOrEqualTo() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			IdentifierToken id1 = m_tokens.gotoNextId(TokenIdentifier.ANGLE_BRACKET_START);
			if(id1 == null) {
				break;
			}
			Token t = m_tokens.next(id1);
			if(t instanceof IdentifierToken) {
				IdentifierToken id2 = (IdentifierToken)t;
				if(id2.getKeyword() == TokenIdentifier.EQUALS) {
					IdentifierToken nid = new IdentifierToken(id1.getStart(), id2.getEnd(), TokenIdentifier.LESS_THAN_OR_EQUALS);
					m_tokens.replace(id1, nid);
					m_tokens.remove(id2);
				}
			}
		}
	}
	/**
	 * scans for annotation starting tokens and makes annotation tokens of the next single word token
	 */
	private void assembleAnnotation(IdentifierToken at) {

		if(at != null) {
			ScopeNameToken snt = m_tokens.relative(1, ScopeNameToken.class);
			SymbolNameToken swt = m_tokens.relative(1, SymbolNameToken.class);
			IdentifierToken bracketStart = m_tokens.relativeId(2, TokenIdentifier.BRACKET_START);
			IdentifierToken bracketEnd = matchBracket(bracketStart);
			boolean noBrackets = false;

			if(swt == null && snt == null) {
				m_log.issueError("Annotation symbol should be followed by a name", at.getEnd());
				m_tokens.gotoSemi();
			} else if(bracketStart == null) {
				noBrackets = true;
			} else if(bracketEnd == null) {
				//this should never happen because the match bracket will throw its own exception
			}
			
			ScopeName sn = snt == null ? ScopeName.wrap(swt.getSymbolName()) : snt.getScopeName();
			Coord c = snt == null ? swt.getStart() : snt.getStart();
			Annotation a = new Annotation(sn, c);
			
			

			if(noBrackets) {
				m_tokens.setIndex(swt);
				m_tokens.next();
			} else {
				m_tokens.setIndex(bracketStart);
				m_tokens.next();
				while(m_tokens.isCurrentBefore( bracketEnd)) {
					Token t = m_tokens.getCurrent();
					if(t instanceof StringToken) {
						
						a.addParameter(t.getName());
					} else if(t instanceof SymbolNameToken) {
						//this is likely a constant
						a.addDeferredParameter(((SymbolNameToken)t).getSymbolName(), getImports(true));
					} else if(t instanceof NumberToken) {
						NumberToken nt = (NumberToken)t;
						a.addParameter(nt.getNumber());
					} else if(t instanceof CommentToken) {
						//ignore comments
					} else if(t instanceof IdentifierToken) {
						IdentifierToken comma = (IdentifierToken)t;
						if(comma.getKeyword() != TokenIdentifier.COMMA){
							m_log.issueError("Parameters should be comma separated "+t, t.getStart());
						}
					} else if(t instanceof NameValueToken) {
						NameValueToken<?> nvt = (NameValueToken<?>)t;
						a.addParameter(nvt.getValue());
					} else {
						m_log.issueError("This doesn't seem to be a valid annotation parameter "+t, t.getStart());
					}
					m_tokens.next();
	
				}
//				if(m_tokens.getCurrent() == bracketEnd) {
//					m_tokens.next();
//				}
			}
			if(a.getName().equals(Annotation.KnownAnnotation.FILE_PATH.getName())) {
				String fn = a.getParameter(String.class);
				if(fn == null) {
					m_log.issueError("File path annotation should have a string parameter.");
				} else {
					m_fileName = fn;
				}

			} else {
				m_annotations.add(a);
			}
		}
	}
	private IdentifierToken matchBracket(IdentifierToken bracketStart) {
		IdentifierToken result = null;
		try {
			result = m_tokens.matchBracket(bracketStart);
		} catch (SchemaParserException e) {
			m_log.issueError(e.getMessage(), e.getLocaation());
		}
		return result;
	}

	/**
	 * builds an array of all names of modules, from any import statements and if specified, from the current module too
	 * @param includeModule
	 * @return
	 */
	private ScopeName[] getImports(boolean includeModule) {
		ArrayList<ScopeName> result = new ArrayList<ScopeName>();
		result.addAll(m_imports);
		if(includeModule) {
			result.add(m_moduleStack.getLast().getName());
		}
		result.add(0, m_moduleStack.getFirst().getName());
		return result.toArray(new ScopeName[result.size()]);
	}
	/**
	 * remove all end of line tokens that are preceded or followed by a token that no longer needs an EOL token
	 * This likely now removes all EOL tokens
	 */
	private void collapseEolsV3() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			EolToken et = m_tokens.gotoNext(EolToken.class);
			Token t = m_tokens.relative(-1);
			if(t instanceof EolToken || t instanceof CommentToken) {
				m_tokens.remove(et);
			} else {
				IdentifierToken it = m_tokens.relative(-1, IdentifierToken.class);

				if(it != null) {
					if(it.check(TokenIdentifier.BRACE_END,
							TokenIdentifier.BRACE_START,
							TokenIdentifier.BRACKET_END,
							TokenIdentifier.BRACKET_START,
							TokenIdentifier.COMMA,
							TokenIdentifier.SQUARE_BRACKET_END,
							TokenIdentifier.SQUARE_BRACKET_START,
							TokenIdentifier.SEMICOLON
					)) {
						m_tokens.remove(et);
					} else {
						m_tokens.next();
					}
				} else {
					m_tokens.next();
				}
			}
		}
	}
	/**
	 * remove all end of line tokens that are preceded or followed by a token that no longer needs an EOL token
	 * This likely now removes all EOL tokens
	 */
	private void collapseEolsV4() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			EolToken et = m_tokens.gotoNext(EolToken.class);
			Token t = m_tokens.relative(-1);
			if(t instanceof EolToken || t instanceof CommentToken) {
				m_tokens.remove(et);
			} else {
				IdentifierToken it = m_tokens.relative(-1, IdentifierToken.class);

				if(it != null) {
					if(it.check(
//							TokenIdentifier.BRACE_END,
//							TokenIdentifier.BRACE_START,
//							TokenIdentifier.BRACKET_END,
//							TokenIdentifier.BRACKET_START,
							TokenIdentifier.COMMA,
							TokenIdentifier.SQUARE_BRACKET_END,
							TokenIdentifier.SQUARE_BRACKET_START
					)) {
						m_tokens.remove(et);
					} else {
						m_tokens.next();
					}
				} else {
					m_tokens.next();
				}
			}
		}
	}
	/**
	 * removes any semicolon tokens that occur either right right after  the following tokens:
	 * NameValueToken, CommmentToken, FilePathToken, closing brace, closing bracket, closing square bracket
	 * Also removes if the token right after the semicolon is:
	 * NameValueToken, CommentToken, FilePathToken, BraceEnd, BracketEnd, SquareBracketEnd
	 */
	private void collapseSemicolons() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			IdentifierToken et = m_tokens.gotoNextId(TokenIdentifier.SEMICOLON);
			Token tPrev = m_tokens.relative(-1);
			IdentifierToken itPrev = m_tokens.relative(-1, IdentifierToken.class);
			Token tNext = m_tokens.relative(1);
			IdentifierToken itNext = m_tokens.relative(1, IdentifierToken.class);
			if(tPrev instanceof NameValueToken || tPrev instanceof CommentToken || tPrev instanceof FilePathToken
					|| (itPrev != null && itPrev.check(TokenIdentifier.BRACE_END, TokenIdentifier.BRACKET_END, TokenIdentifier.SQUARE_BRACKET_END))) {
				m_tokens.remove(et);
			} else if(tNext instanceof NameValueToken || tNext instanceof CommentToken || tNext instanceof FilePathToken 
					|| (itNext != null && itNext.check(TokenIdentifier.BRACE_END, TokenIdentifier.BRACKET_END, TokenIdentifier.SQUARE_BRACKET_END))) {
				m_tokens.remove(et);
			} else {
				m_tokens.next();
			}
		}
	}
	/**
	 * Combine comment tokens if there are consecutive ones
	 * Block comments will not be collapsed. Consecutive line comments will collapse and become blocks.
	 * Comments separated by emptylines will not be collapsed
	 */
	private void collapseComments() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			CommentToken ct = m_tokens.gotoNext(CommentToken.class);


			if(ct == null) {
				break;
			} else if(!ct.isLineComment()) {
					//don't do anything here. We don't collapse block comments
				m_tokens.next();
			} else {
				CommentToken ct2 = m_tokens.relative(1, CommentToken.class);
				if(ct2 != null) {
					if(ct2.isLineComment()) {
						CommentToken ctn = ct.combine(ct2);
						m_tokens.replace(ct, ctn);
						m_tokens.remove(ct2);
					}
				} else {
					//check for eol
					EolToken et = m_tokens.relative(1, EolToken.class);
					if(et != null) {
						m_tokens.remove(et);
					} else {
						m_tokens.next();
					}
				}
			}
		}

	}




	/**
	 * Scan through all tokens and those Id tokens whose IDs represent base types re converted to base types
	 */
	private void collapseBaseTypes(TokenIdentifier[] ids) {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken it = m_tokens.gotoNextId(ids);
			if(it != null) {

				BaseTypeToken btt = BaseTypeToken.makeNew(it);
				
				if(btt == null) {
				} else {
					m_tokens.replace(it, btt);
				}

				m_tokens.next();
			}
		}
	}

	private Coord processEol(Coord c) {
		Coord result = c;
		if(result.remainingString().isBlank()) {
			m_tokens.add(new EolToken(result));
			result = result.nextLine();
		}
		return result;
	}
	/**
	 * Scans to the next eol or specified token identifier
	 * adds everything in between as a single word token or an identifier token
	 * This assumes we are current not at an id token
	 * @param c - the starting coordingate
	 * @param ids - the total list of identifier tokens that we want to look for
	 * @param separator - an array of token identifiers that would follow a single word token or token identifier
	 * @return
	 */
	private Coord processNextWordToken(Coord c, TokenIdentifier[] ids, TokenIdentifier[] separators) {
		Coord result = c;
		Token t = null;
		while(true) {
			
			TokenIdentifier sep = result.findMatch(separators);
			if(sep != null || result.isEol()) {
				if(sep != null && result == c) {
					//the very first element is a separator so add it
					t = new IdentifierToken(c, sep);
				} else {
					//we've got a token worth of stuff
					//now see if it's an it or swt
					TokenIdentifier mid = c.findMatch(result, ids);
					if(mid != null) {
						t = new IdentifierToken(c, mid);
					} else if(c != result){
						t = new SingleWordToken(c, result);
					}
					
				}
				if(t != null) {
					m_tokens.add(t);
					result = t.getEnd();
					break;
				} else if(result.isEol()) {
					break;
				}
				
			}
			result = result.incrementIndex(1);
		}
		return result;
	}
	/**
	 * we know that the current coordinate is not a comment, string or character
	 * If this line starts with a valid token identifier then add it
	 * @param c
	 * @param ids
	 * @return
	 */
	private Coord processTokenIdentifier(Coord c, TokenIdentifier[] ids) {
		Coord result = c;
		
		TokenIdentifier id = c.findMatch(ids);
		if(id != null) {
			IdentifierToken it = new IdentifierToken(c, id);
			m_tokens.add(it);
			result = it.getEnd();
		}
		
		return result;
	}
	/**
	 * advances to the next token identifier
	 * @param c
	 * @param ids
	 * @return
	 */
	private Coord processNextToken(Coord c, TokenIdentifier[] ids) {
		if(c.isEol()) {
			return c;
		}
		Coord start = c;//c.trim();
		//now advance to the next interesting token
		Coord result = start.advanceToNext(ids);
		Coord end = result;
		
		
		
		
		
		String s = start.fromThisToThatString(end);
		if(s.isBlank()) {
			//this should allow whitespace to be tokenized
		} else {
			end = end.trimEnd();
			s = start.fromThisToThatString(end);
		}
				
		if(s.isEmpty()) {
		} else if(s.isBlank()) {
			m_tokens.add(new IdentifierToken(start, end, TokenIdentifier.SPACE));
		} else {
			m_tokens.add(new SingleWordToken(start, end));
			
		}
		
		return result;
	}

	/**
	 * find all line comments and insert the token at the start of the line they are on in the token list
	 * @param c
	 * @return
	 */
	private Coord processV3LineComment(Coord c) {
		if(c == null) {
			return null;
		}
		Coord result = c;//c.trim();
		if(result.startsWith(LINE_COMMENT_START)) {
			Coord start = result;
			Coord end = result.nextLine();
			result = result.incrementIndex(LINE_COMMENT_START);
			String comment = result.remainingString();
			result = result.gotoEol();
			//find the index of the first element of the line that this comment occurred on.
			//place this commment before that element

			m_tokens.add(m_tokens.getFirstIndexBeforeLine(start), new CommentToken(start, end, comment, false));
		}

		return result;
	}
	/**
	 * find all line comments and insert the token at the start of the line they are on in the token list
	 * @param c
	 * @return
	 */
	private Coord processV4Comment(Coord c) {
		if(c == null) {
			return null;
		}
		Coord result = c;//c.trim();
		if(result.startsWith(TokenIdentifier.HASH.id())) {
			Coord start = result;
			Coord end = result.nextLine();
			result = result.incrementIndex(TokenIdentifier.HASH.id());
			String comment = result.remainingString();
			result = result.gotoEol();
			//find the index of the first element of the line that this comment occurred on.
			//place this commment before that element

			m_tokens.add(m_tokens.getFirstIndexBeforeLine(start), new CommentToken(start, end, comment, false));
		}

		return result;
	}
	/**
	 * if the next coord is a single quotation mark then process as a string until the closing quote
	 * if no valid closing quote is found then consume the remainder of the whole input
	 * So this really is not different that processsing single quotes
	 * @param c
	 * @return
	 */
	private Coord processChars(Coord c) {
		if(c == null) {
			return null;
		}

		Coord result = c;

		if(result.startsWith(CHAR_DELIMITER)) {
			Coord start = result.incrementIndex(1);//we don't want to point to the quotation mark
			Coord end = start;
			boolean notDone = true;
			while(notDone) {
				Coord r = result.findNext(CHAR_DELIMITER, STRING_ESCAPE_DELIMITER);
				if(r == null) {
					System.out.println("BlueberrySchemaParser.processStrings");
				}
				result = r;
				if(result.isAtEnd()) {
					notDone = false;
					end = result;
				} else if(result.startsWith(CHAR_DELIMITER)) {
					end = result;
					notDone = false;
					result = result.incrementIndex(1);

				} else {
					//this must be a escape character then the next character might be a quotation so skip it
					result = result.incrementIndex(1);
				}

			}
			m_tokens.add(new StringToken(start, end));
		}
		return result;
	}
	/**
	 * if the next coord is a double quotation mark then process a string until the closing quote
	 * if no valid closing quote is found then consume the remainder of the whole input
	 * @param c
	 * @return
	 */
	private Coord processStrings(Coord c, String delimiter) {
		if(c == null) {
			return null;
		}

		Coord result = c;

		if(result.startsWith(delimiter)) {
			Coord start = result.incrementIndex(1);//we don't want to point to the quotation mark
			Coord end = start;
			boolean notDone = true;
			while(notDone) {
				Coord r = result.findNext(delimiter, STRING_ESCAPE_DELIMITER);
				if(r == null) {
					System.out.println("BlueberrySchemaParser.processStrings");
				}
				result = r;
				if(result.isAtEnd()) {
					notDone = false;
					end = result;
				} else if(result.startsWith(delimiter)) {
					end = result;
					notDone = false;
					result = result.incrementIndex(1);

				} else {
					//this must be a escape character then the next character might be a quotation so skip it
					result = result.incrementIndex(1);
				}

			}
			m_tokens.add(new StringToken(start, end));
		}
		return result;
	}
	

	/**
	 * Checks if the next element of the file is a block comment
	 * @return the next Coord after a comment if there is one. null if incomplete comment, c if no comment
	 */
	private Coord processV3BlockComment(Coord c){
		if(c == null) {
			return null;
		}
		Coord result = c;

		//first check for comment block
		if(result.startsWith(COMMENT_BLOCK_START)) {

			result = result.incrementIndex(COMMENT_BLOCK_START);//move to the end of the block comment
			String comment = "";

			Coord start = result;

			boolean keepGoing = true;
			boolean firstTime = true;
			while(keepGoing) {
				result = result.trim();//remove whitespace if any

				if(result.contains(COMMENT_BLOCK_END)) {
					Coord end = result.indexOf(COMMENT_BLOCK_END);
					String ns = result.fromThisToThatString(end);

					if(!ns.isBlank()) {
						comment += ns;
					}
					m_tokens.add(new CommentToken(start, end, comment, true));
					keepGoing = false;
					result = end.incrementIndex(COMMENT_BLOCK_END).newLineIfEol();
				} else {
					if(result.startsWith(COMMENT_BLOCK_MIDDLE)) {
						result = result.incrementIndex(COMMENT_BLOCK_MIDDLE);
					}
					result = result.trim();
					if(!firstTime) {
						comment += "\n";
					}
					String s = result.remainingString();
					if(!s.isBlank()) {
						comment += s;
						firstTime = false;
					} else {

					}
					result = result.nextLine();
					if(result == null) {
						keepGoing = false;
						result = null;
						m_log.issueError("Block comment missing end! Skipping rest of file.", c);
						m_tokens.gotoNextFile();
					}

				}

			}

		}
		return result;
	}


	public FieldList getMessages() {
		return m_messages;
	}
	public FieldList getDefines() {
		return m_defines;
	}
	public ArrayList<BlueModule> getModules(){
		return m_modules;
	}



	/**
	 * Scan through all messages, resetting the message_key annotation
	 * Then re-assign them from scratch.
	 */
	public void resetKeys() {
		
		m_modules.forEach(m -> {
			m.getMessages().forEachOfType(MessageField.class, false, msg -> {
				Annotation a = new Annotation(Annotation.KnownAnnotation.MESSAGE_KEY.getName(), null);
				msg.addAnnotation(a);
			});
		});
		fillInMissingMessageKeyValues();
		assignModuleAnnotations();
		checkForDuplicateMessageKeys();
		
	}

	public void setVersion(int i) {
		switch(i) {
		case VERSION_3:
		case VERSION_4:
			m_ver = i;
			break;
		default:
			m_ver = VERSION_4;
				
		}
	}



}
