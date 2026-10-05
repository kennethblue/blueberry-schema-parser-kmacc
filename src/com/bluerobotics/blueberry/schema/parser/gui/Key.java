/*
Copyright (c) 2017-2018 STARFISH PRODUCT ENGINEERING INC.

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

package com.bluerobotics.blueberry.schema.parser.gui;

import java.io.File;
import java.net.URI;

import com.starfishmedical.settings.SettingsKey;






public enum Key implements SettingsKey {
//	APP_HEIGHT(Integer.class, 480),
//	APP_WIDTH(Integer.class, 640),
	VERSION_4_NOT_3(Boolean.class, true),
	CODE_HEADER_FILE_PATH(URI.class, (new File(System.getProperty("user.dir"))).toURI()),
	IDL_HEADER_FILE_PATH(URI.class, (new File(System.getProperty("user.dir"))).toURI()),
	SCHEMA_DIRECTORY(File.class, (new File(System.getProperty("user.dir")))),
	JAVA_DIRECTORY(File.class, (new File(System.getProperty("user.dir")))),
	JAVA_PACKAGE_NAME(String.class, "com.bluerobotics.blueberry.bb"),
	C_DIRECTORY(File.class, (new File(System.getProperty("user.dir")))),
	
	
	DEFAULT_FILE_PATH(File.class, (new File(System.getProperty("user.dir")))),
	APP_POS_X(Integer.class, 0),
	APP_POS_Y(Integer.class, 0),
	APP_WIDTH(Integer.class, 1000),
	APP_HEIGHT(Integer.class, 700),
	SPLIT_PANE_POS(Integer.class, 200),
	

	;
	private Class<?> m_varClass;
	private Object m_default;
	private Key(Class<?> c, Object defaultValue){
		m_varClass = c;
		m_default = defaultValue;
	}
	
	@Override
	public Class<?> getVarClass(){
		return m_varClass;
	}

	/* (non-Javadoc)
	 * @see com.vivitrolabs.vivihub.settings.SettingsKey#getDefaultValue()
	 */
	@Override
	public Object getDefaultValue(){
		return m_default;
	}
	
	@Override
	public SettingsKey[] getValues(){
		return values();
	}

}