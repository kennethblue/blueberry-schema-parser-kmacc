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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Function;

import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants.TokenIdentifier;

/**
 *
 */
public class TokenList {
	private final ArrayList<Token> m_tokens = new ArrayList<Token>();
	private int m_index = 0;
	public TokenList() {

	}


	public Token getCurrent() {
		if(m_index < 0 || m_index >= m_tokens.size()) {
			return null;
		}
		return m_tokens.get(m_index);
	}
	
	
	public Token get(int i) {
		if(i < 0 || i >= m_tokens.size()) {
			return null;
		}
		return m_tokens.get(i);
	}
	public void clear() {
		m_tokens.clear();
	}
	public Token add(Token t) {
		m_tokens.add(t);
		return t;
	}
	public int size() {
		return m_tokens.size();
	}
	public void remove(Token t) {
		if(t != null) {
			int i = m_tokens.indexOf(t);
			if(i >= 0) {
				if(i < m_index) {
					--m_index;
				}
				remove(i);	
			
				
			}
			
		}
	}
	public void remove(int i) {
		if(i >= 0 && i < m_tokens.size()) {
			if(i < m_index) {
				--m_index;
			}
			m_tokens.remove(i);
		}
	}

	public Token add(int i, Token t) {
		m_tokens.add(i, t);
		return t;
	}
	public void resetIndex() {
		m_index = 0;
	}
	/**
	 * replaced an existing token in the list with another.
	 * This only happens if the old token is actually in the list and the new token is not null
	 * @param oldT
	 * @param newT
	 */
	public void replace(Token oldT, Token newT) {
		if(newT != null) {
			int i = m_tokens.indexOf(oldT);
			if(i >= 0) {
				m_tokens.set(i, newT);
			}
		}
	}

	

	public boolean isAtEnd() {
		return m_index >= m_tokens.size();
	}
	public boolean isMore() {
		return m_index < m_tokens.size();
	}
	/**
	 * sets the current index to the specified token.
	 * If the token is null then this will go to the end of the list
	 * @param t
	 */
	public void setIndex(Token t) {
		if(t != null) {
			int i = m_tokens.indexOf(t);
			if(i < 0) {
				throw new RuntimeException("Specified token not in list!");
			}
			m_index = i;
		} else {
			m_index = m_tokens.size();
		}

	}
	/**
	 * advance the current postion by 1
	 */
	public void next() {
		next(1);
	}
	/**
	 * advances the current position by the specified amount
	 * @param i
	 */
	public void next(int i) {
		int j = i + m_index;
		if(j > m_tokens.size()) {
			j = m_tokens.size();
		}
		m_index = j;
	}
	public Token next(Token t) throws SchemaParserException {
		int i = m_tokens.indexOf(t);
		if(i < 0) {
			throw new SchemaParserException("Token " + t + " is not part of list.", t.getStart());
		} else {
			++i;

		}
		return get(i);
	}

	/**
	 * Finds the next token of the specified type from the current index
	 * Does test the token at the current index
	 * @param start the place to start
	 * @param forwardNotReverse
	 * @param howFar - the number of steps to look. Zero means look until the end.
	 * @param test - the test to check for
	 *
	 * @return the index of the found token or -1 if none found
	 */
	private int findToken(int start, boolean forwardNotReverse, int howFar, Function<Token, Boolean> test) {
		int i  = start;
		int result = -1;
		boolean notDone = true;
		int n;
		if(forwardNotReverse) {
			if(howFar == 0) {
				n = m_tokens.size();
			} else {
				n = i + howFar;
				if(n > m_tokens.size()) {
					n = m_tokens.size();
				}
			}
		} else {
			if(howFar == 0) {
				n = 0;
			} else {
				n = i - howFar;
				if(i < 0) {
					i = 0;
				}
			}
		}


		int j = i;
		while(notDone) {
			Token t = m_tokens.get(j);
			if(test.apply(t)) {
				result = j;
				break;
			} else if(forwardNotReverse) {
				++j;
				if(j >= n) {
					notDone = false;
				}
			} else {
				--j;
				if(j < n) {
					notDone = false;
				}
			}
		}
		return result;
	}

	public Token find(Token start, boolean forwardNotReverse, Class<?>... cs) {
		Token result = null;
		int startI = m_tokens.indexOf(start);
		if(startI < 0) {
			startI = 0;
		}
		int i = findToken(startI, forwardNotReverse, 0, (t) -> {
			boolean found = false;
			for(Class<?> c : cs) {
				if(t.getClass() == c) {
					found = true;
					break;
				}

			}
			return found;
		});
		if(i >= 0) {
			result = m_tokens.get(i);
		}
		return result;
	}
	/**
	 * goes to the next token that passes the specified test
	 * if none exists then index is moved to past the last element of the list
	 * The current location is tested and will be returned if it passes.
	 * @param test
	 * @return
	 */
	private Token gotoNext(Function<Token, Boolean> test) {
		Token result = findNext(getCurrent(), null, test);

		setIndex(result);
		return result;
	}
	
	/**
	 * finds the next token that passes the specified test
	 * if none exists then returns null
	 * The current location is tested and will be returned if it passes.
	 * @param test
	 * @return
	 */
	private Token findNext(Token start, Token end, Function<Token, Boolean> test) {
		int startI = m_tokens.indexOf(start);
		int endI = m_tokens.indexOf(end);
		int range = 0;
		
		if(startI < 0) {
			startI = 0;
		}
		if(endI >= 0) {
			range = endI - startI;
			if(range < 0) {
				return null;
			}
		}
		
		
		int i = findToken(startI, true, range, test);

		return get(i);
	}

	/**
	 * Advances to the next token of the specified sub-type
 	 * if none exists then index is moved to past the last element of the list
 	 * The current location is tested and will be returned if it passes.
	 * @param <T>
	 * @param cs
	 * @return
	 */
	public <T extends Token> T gotoNext(Class<T> cs) {
		return cs.cast(gotoNext( t -> {
			return cs == t.getClass();
		}));
	}
	/**
	 * returns the current token as the specified type
	 * or returns null if it is not of that type
	 * @param <T>
	 * @param c
	 * @return
	 */
	public <T extends Token> T getCurrentAs(Class<T> c) {
		Token t = getCurrent();
		if(c.isInstance(t)) {
			return c.cast(getCurrent());
		}
		return null;
	}
	
	/**
	 * finds the next tokens of the specified class, but will stop if it reaches stopHere
	 * @param stopHere
	 * @param cs
	 * @return
	 */
	public Token gotoNextOfThese(Token stopHere, Class<?>... cs) {
		Token resultT = gotoNext(t -> {
			boolean result = false;
			if(t == stopHere) {
				result = true;
			} else {
				for(Class<?> c : cs) {
					if(t.getClass() == c) {
						result = true;
						break;
					}
				}
			}
			return result;
		});
		return resultT;
	}

	/**
	 * Finds the next IdentifierToken of the specified sub-type
 	 * if none exists then returns null
 	 * The current location is tested and will be returned if it passes.
 	 * @param ti
	 * @return
	 */
	public IdentifierToken findNextId(Token start, TokenIdentifier... tis) {
		return findNextId(start, null, tis);
	}
	/**
	 * Finds the next IdentifierToken of the specified sub-type
 	 * if none exists then returns null
 	 * The current location is tested and will be returned if it passes.
 	 * @param ti
	 * @return
	 */
	public IdentifierToken findNextId(Token start, Token end, TokenIdentifier... tis) {
		IdentifierToken it = null;
		Token t = findNext(start, end, (tt) -> {
			boolean result = false;
			if(tt.getClass() == IdentifierToken.class) {
				for(TokenIdentifier ti : tis) {
					if(((IdentifierToken)tt).getKeyword() == ti) {
						result = true;
						break;
					}
				}

			}
			return result;
		});
		if(t != null) {
			it = (IdentifierToken)t;
		}
		return it;
	}
	
	public boolean isMatchId(Token t, TokenIdentifier...ids) {
		boolean result = false;
		if(t instanceof IdentifierToken) {
			IdentifierToken idt = (IdentifierToken)t;
			for(TokenIdentifier id : ids) {
				if(((IdentifierToken) t).getKeyword() == id) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	/**
	 * Advances to the next IdentifierToken of the specified sub-type
 	 * if none exists then index is moved to past the last element of the list
 	 * The current location is tested and will be returned if it passes.
 	 * @param ti
	 * @return
	 */
	public IdentifierToken gotoNextId(TokenIdentifier... tis) {
		IdentifierToken result = findNextId(getCurrent(), tis);
		setIndex(result);

		return result;
	}
	public Token relative(int i) {
		int j = i + m_index;
		if(j < 0 || j >= m_tokens.size()) {
			return null;
		}
		return m_tokens.get(j);

	}
	/**
	 * get the token in the position relative to the current token if it is of the specified type. Otherwise it return null.
	 * @param <T>
	 * @param i
	 * @param type
	 * @return
	 */
	public <T extends Token> T relative(int i, Class<T> type){
		T result = null;
		Token t = relative(i);
		if(type.isInstance(t)) {
			result = type.cast(t);
		}
		return result;
	}
	public Token relative(Token t, int i) {
		int j = m_tokens.indexOf(t) - m_index+i;
		return relative(j);
	}
	public <T extends Token> T relative(Token t, int i, Class<T> type) {
		T result = null;
		Token t2 = relative(t, i);
		if(t2 != null && type.isInstance(t2)) {
			result = type.cast(t2);
		}
		return result;
	}
	
	
	public IdentifierToken relativeId(int i, TokenIdentifier... ids) {

		return IdentifierToken.test(relative(i, IdentifierToken.class), ids);
	}

	public IdentifierToken relativeId(Token t, int i, TokenIdentifier... ids) {
		int j = m_tokens.indexOf(t) - m_index + i;
		return relativeId(j, ids);
	}
	/**
	 * finds the next occurance of the specified type and returns it cast to the specific type
	 * @param <T>
	 * @param start
	 * @param type
	 * @return
	 */
	public <T extends Token> T findNext(Token start, Class<T> type) {
		return findNext(start, null, type);
	}
	/**
	 * finds the next occurance of the specified type and returns it cast to the specific type
	 * @param <T>
	 * @param start
	 * @param end
	 * @param type
	 * @return
	 */
	public <T extends Token> T findNext(Token start, Token end, Class<T> type) {
		Token r = findNext(start, end, t -> {
			return t.getClass() == type;
		});

		return type.cast(r);
	}
	
	
	public Token getLastNonNull(Token...tokens){
		int i = -1;
		for(Token t : tokens) {
			if(t != null) {
				int j = m_tokens.indexOf(t);
				if(j > i) {
					i = j;
				}
			}
		}
		Token result = null;
		if(i >= 0) {
			result = m_tokens.get(i);
		}
		return result;
	}
	
	

	public String toString() {
		return getClass().getSimpleName() + "("+getCurrent()+", " + relative(1) + ", " + relative(2) + "...)";
	}


	/**
	 * find the bracket that closes the current opening
	 * Looks for braces (curly brackets), (round) brackets, angle brackets and square brackets.
	 * If the current location is not at an opening, it should return null, but will still have checked the whole list.
	 * If the input is null then it starts at the beginning of the list
	 * If the token list does not have matches for all brackets of all types then an exception will be thrown.
	 * @param t - the token to start at. Must be a member of this token list
	 * @return - the matching closing brace token
	 * @throws SchemaParserException 
	 */
	public IdentifierToken matchBracket(Token start) throws SchemaParserException {
		IdentifierToken result = null;
	
		Token t = start;
		if(t == null) {
			t = get(0);
		}
		IdentifierToken it = null;
		TokenIdentifier match = null;
		if(t instanceof IdentifierToken) {
			it = (IdentifierToken)t;

			switch(it.getKeyword()) {
			case BRACE_START:
				match = TokenIdentifier.BRACE_END;
				break;
			case BRACKET_START:
				match = TokenIdentifier.BRACKET_END;
				break;
			case SQUARE_BRACKET_START:
				match = TokenIdentifier.SQUARE_BRACKET_END;
				break;
			case ANGLE_BRACKET_START:
				match = TokenIdentifier.ANGLE_BRACKET_END;
				break;
			case ANGLE_BRACKET_END:
			case BRACE_END:
			case BRACKET_END:
			case SQUARE_BRACKET_END:
				throw new SchemaParserException("Somehow got one of these "+it+"without an opening one.", it.getStart());

			default:
				break;
			}
			if(match != null) {
				t = next(t);
			}
		}
		//at this point, if match == null then this was not an open bracket and it cannot be a close bracket because that would have thrown an exception
		//so for now the only course of action is to look for the next interesting thing
		
		boolean keepLooking = true;
		Token foundT = t;
		while(keepLooking) {
			foundT = findNextId(foundT,
					TokenIdentifier.BRACE_START,
					TokenIdentifier.BRACKET_START,
					TokenIdentifier.SQUARE_BRACKET_START,
					TokenIdentifier.ANGLE_BRACKET_START,
					TokenIdentifier.BRACE_END,
					TokenIdentifier.BRACKET_END,
					TokenIdentifier.SQUARE_BRACKET_END,
					TokenIdentifier.ANGLE_BRACKET_END
					);
			//if we've found an opening then we need to recurse. That will return the closing brace of the opening, so we may need to look further
			if(isMatchId(foundT, TokenIdentifier.BRACE_START,
					TokenIdentifier.BRACKET_START,
					TokenIdentifier.SQUARE_BRACKET_START,
					TokenIdentifier.ANGLE_BRACKET_START)){
				foundT = matchBracket(foundT);
				//this is now the closing bracket of the start we just found
				//move ahead one token so we don't get caught in an infinite loop
				//but if we didn't find anything then just bail
				if(foundT == null) {
					keepLooking = false;
				} else {	
					foundT = next(foundT);
					if(foundT == null) {
						//hit the end of the list
						keepLooking = false;
					}
				}
				
			} else {
				//this must be a closing brace or null
				keepLooking = false;
			}
		}
		
		
		//at this point we have either found a closing brace or nothing at all
		
		if(foundT == null) {
			if(match != null) {
				//we have not found anthing but we should have
				throw new SchemaParserException("Did not find a match for "+it, it.getStart());
			} else {
				//we found nothing but that's ok
				result = null;
				
			}
		} else {
			//we found a thing - it must be an identifier token because it would not have left the loop above otherwise
		
			result = (IdentifierToken)foundT; 
			if(result.getKeyword() != match) {
				//the found thing does not match but it must be a closing bracket because of the loop exit circumstances above. This is bad
				throw new SchemaParserException("Found an unexpected "+result.getKeyword(), result.getStart());

			} else {
				//we have a match so we found the thing!
			}
		}
			
		
	
		return result;

	}
	/**
	 * Returns true if the first token occurs before the second token in this list
	 * If tokens are equal or a token is not actually in the list or either are null then returns false
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean inOrder(Token t1, Token t2) {
		if(t1 == null || t2 == null) {
			return false;
		}
		int i1 = m_tokens.indexOf(t1);
		if(i1 < 0) {
			return false;
		}
		int i2 = m_tokens.indexOf(t2);
		if(i2 < 0) {
			return false;
		}
		if(i1 < i2) {
			return true;
		}
		return false;

	}
	/**
	 * Computes whether this list's current token is before the specified token
	 * @param t - the specified token
	 * @return - true if the current token is before the specified one
	 */
	public boolean isCurrentBefore(Token t) {
		if(isAtEnd()) {
			return false;
		} else {
			return inOrder(getCurrent(), t);
		}
	}
	/**
	 * removes a range of tokens from start to end.
	 * Does not include end
	 * @param start
	 * @param end
	 */
	public void remove(Token start, Token end) {
		Token t = relative(end, -1);
		while(inOrder(start, t)) {
			Token newT = relative(t, -1);
			remove(t);
			t = newT;

		}

	}
	/**
	 * looks back through the tokens and finds the first index of this list that occurs on the specified token's line in the same file.
	 * if there is no such token in the same file then it will return an index after the last token of the list
	 * if there is no such line then it returns the first token of a following line
	 * if there is no same or greater line then it returns an index after the last token of the list
	 * @param t - the token on the line that we want to find the start of
	 * @return the index of token at the start of the line.
	 */
	public int getFirstIndexBeforeLine(Coord c) {
		//first find the first token of the same file
		int i = findToken(0, true, 0, t -> t.getStart().isSameFile(c));
		
		
		if(i == -1) {
			//if we couldn't find one then return the end
			return size();
		}
		//now look for token on the same line
		i = findToken(i, true, 0, t -> t.getStart().isSameFile(c) && t.getStart().line >= c.line);
		
		if(i == -1) {
			return size();
		}
		
		if(m_tokens.get(i) instanceof FilePathToken) {
			//if we've found a file path token then advance forward one
			++i;
		}
		
		return i;
	}


	public ListIterator<Token> getIterator() {
		return m_tokens.listIterator();
	}


	public Token getFirst() {
		return m_tokens.getFirst();
	}

	public Token getLast() {
		return m_tokens.getLast();
	}


	public void gotoSemi() {
		gotoNextId(TokenIdentifier.SEMICOLON);
	}


	public void gotoEol() {
		gotoNextId(TokenIdentifier.SEMICOLON);
	}


	public void gotoNextFile() {
		gotoNext(FilePathToken.class);
	}

	

}
