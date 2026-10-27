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
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 *
 */
public interface TokenConstants {
	public enum TokenIdentifier {
		ANGLE_BRACKET_START("<"),
		ANGLE_BRACKET_END(">"),
		ANNOTATION_START("@"),
		BOOLEAN("boolean"),
		BOOL("bool"),
		BRACE_END("}"),
		BRACE_START("{"),
		BRACKET_END(")"),
		BRACKET_START("("),
		BYTE("byte"),
		DOUBLE_COLON("::"),//this must come before a colon
		CHAR("char"),
		COLON(":"),
		COMMA(","),
		CONST("const"),
		DOUBLE("double"),
		ENUM("enum"),
		EQUALS("="),
//		ESCAPE_DELIMITER("_"),
		FALSE("FALSE"),
		FLOAT("float"),
		FLOAT32("float32"),
		FLOAT64("flaot64"),
		HASH("#"),
		IMPORT("import"),
		INT("int"),
		INT16("int16"),
		INT32("int32"),
		INT64("int64"),
		INT8("int8"),
		LESS_THAN_OR_EQUALS("<="),
		LONG("long"),
		LONG_LONG("long long"),
		MESSAGE("message"),
		MODULE("module"),
		SEMICOLON(";"),
		SEQUENCE("sequence"),
		SHORT("short"),
		SPACE(" "),//this is not intended to be used as a token, just to help parse
		SQUARE_BRACKET_END("]"),
		SQUARE_BRACKET_START("["),
		STRING("string"),
		STRUCT("struct"),
		TAB("\t"),//this is not intended to be used as a token, just to help parse
		TRUE("TRUE"),
		TYPEDEF("typedef"),
		TYPEDEF_SEQUENCE("typedef sequence"),
		UINT16("uint16"),
		UINT32("uint32"),
		UINT64("uint64"),
		UINT8("uint8"),
		UNSIGNED("unsigned"),
		WSTRING("wstring"),
		SINGLE_QUOTE("'"),
		DOUBLE_QUOTE("\"")
		
		;
		private String id;
		TokenIdentifier(String s){
			id = s;
		}
		public String id() {
			return id;
		}
	}
//	public static final String ANNOTATION_START = "@";
//	public static final String ARRAY_MODIFIER = "array";
	public static final String BLOCK_MODIFIER = "block";
//	public static final String BOOLEAN_KEYWORD = "boolean";
//	public static final String BRACE_END = "}";
//	public static final String BRACE_START = "{";
//	public static final String BRACKET_END = ")";
//	public static final String BRACKET_START = "(";
//	public static final String BYTE_KEYWORD = "byte";
	public static final String CHAR_DELIMITER = "\'";
	public static final String COMMENT_BLOCK_END = "*/";
	public static final String COMMENT_BLOCK_MIDDLE = "*";
	public static final String COMMENT_BLOCK_START = "/*";
	public static final String COMPACT_ARRAY_MODIFIER = "compact";
	public static final String COMPOUND_MODIFIER = "compound";
//	public static final String CONST_KEYWORD = "const";
	public static final String DEFINED_BLOCK_TOKEN = "define";
//	public static final String DOUBLE_KEYWORD = "double";
//	public static final String ENUM_KEYWORD = "enum";
//	public static final String EQUALS = "=";
	public static final String ESCAPE_DELIMITER = "_";
//	public static final String FLOAT_KEYWORD = "float";
//	public static final String INT_KEYWORD = "int";
//	public static final String INT16_KEYWORD = "int16";
//	public static final String INT32_KEYWORD = "int32";
//	public static final String INT64_KEYWORD = "int64";
//	public static final String INT8_KEYWORD = "int8";
	public static final String KEY_FIELD_NAME = "key";
	public static final String LINE_COMMENT_START = "//";
//	public static final String LONG_KEYWORD = "long";
//	public static final String MESSAGE_KEYWORD = "message";
//	public static final String MODULE_KEYWORD = "module";
//	public static final String SEQUENCE_KEYWORD = "sequence";
//	public static final String SHORT_KEYWORD = "short";
//	public static final String SQUARE_BRACKET_END = "]";
//	public static final String SQUARE_BRACKET_START = "[";
	public static final String STRING_DELIMITER = "\"";
	public static final String STRING_ESCAPE_DELIMITER = "\\";
//	public static final String STRING_KEYWORD = "string";
//	public static final String STRUCT_KEYWORD = "struct";
//	public static final String TYPEDEF_KEYWORD = "typedef";
//	public static final String UINT16_KEYWORD = "uint16";
//	public static final String UINT32_KEYWORD = "uint32";
//	public static final String UINT64_KEYWORD = "uint64";
//	public static final String UINT8_KEYWORD = "uint8";
//	public static final String UNSIGNED_KEYWORD = "unsigned";


}
