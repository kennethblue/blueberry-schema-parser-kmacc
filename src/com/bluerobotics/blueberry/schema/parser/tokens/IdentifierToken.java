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
package com.bluerobotics.blueberry.schema.parser.tokens;

import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants.TokenIdentifier;

/**
 *
 */
public class IdentifierToken extends AbstractToken {
	private final TokenIdentifier m_id;
	public IdentifierToken(Coord start, TokenIdentifier ti) {
		Coord end = start.incrementIndex(ti.id().length());
		super(start, end);
		m_id = ti;
	}
	public IdentifierToken(Coord start, Coord end, TokenIdentifier ti) {
		super(start, end);
		m_id = ti;
	}
	public TokenIdentifier getKeyword() {
		return m_id;
	}
	public String toString() {
		return getClass().getSimpleName()+"("+m_id+")";
	}
	/**
	 * Checks the specified token to see if it's an IdentifierToken and that it's id matches the specified value
	 * @param t2
	 * @param colon
	 * @return
	 */
	public static boolean check(Token t, TokenIdentifier... ids) {
		boolean result = false;
		if(t instanceof IdentifierToken) {
			IdentifierToken it = (IdentifierToken)t;
			result = it.check(ids);
			
		}
		return result;
	}
	
	public boolean check(TokenIdentifier... ids) {
		boolean result = false;
		for(TokenIdentifier ti : ids) {
			if(getKeyword() == ti) {
				result = true;
				break;
			}
		}
		return result;
	}
	public IdentifierToken test(TokenIdentifier...ids) {
		IdentifierToken result = null;
		if(check(ids)) {
			result = this;
		}
		return result;
	}
	public static IdentifierToken test(Token t, TokenIdentifier... ids) {
		IdentifierToken result = null;
		if(t != null && t instanceof IdentifierToken) {
			result = ((IdentifierToken)t).test(ids);
		}
		return result;
	}


}
