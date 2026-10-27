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
import java.util.function.Function;

import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * 
 */
public interface MultipleField extends Field {
	/**
	 * A class to contain useful information about an array or sequence dimension
	 */
	public class Index {
		public final MultipleField p;//the sequence or array that this is a dimension for
		public final int i;//the number of the dimension - only useful for multidimensional arrays
		public final int ofN;//how many dimensions are there?
		public final int n;//the number of elements in this dimension
		public final String type;//either "array" or "sequence"
		public final boolean arrayNotSequence;//true if p is an array, false if p is a sequence
		public final int bytesPerElement;
		public String paramName;
		Index(MultipleField pf, int j, int ofM, int num, int bpe){
			p = pf;
			i = j;
			n = num;
			ofN = ofM;
			arrayNotSequence =  pf instanceof ArrayField;
			type = arrayNotSequence ? "array" : "sequence";
			paramName = "";
			bytesPerElement = bpe;
		}
		
	}
	
	
	public List<Index> getIndeces();
		
	public static List<Index> getIndeces(Field f){
		return getIndeces(f, ft -> (ft.getTypeId() != TypeId.MESSAGE));
	}
	
	/**
	 * Scans upward from the specified field to the specified ParentField and note any array fields along the way
	 * @param f
	 * @param keepGoing - a function that checks the current field and returns true if it should keep scanning upwards, false if it should stop
	 * @return
	 */
	public static List<Index> getIndeces(Field f, Function<Field, Boolean> keepGoing) {
		
		
		
		ArrayList<Index> result = new ArrayList<>();
		ParentField pf = f.getParent();

		List<ParentField> pfs = new ArrayList<>();
		while(pf != null && keepGoing.apply(pf)) {
			
			pfs.add((ParentField)pf);
			
			pf = pf.getParent();
		}
		
		//now re-order the fields so the go top to bottom instead of bottom-up
		pfs = pfs.reversed();
		
		for(ParentField pft : pfs) {
			
			if(pft instanceof MultipleField) {
				result.addAll(((MultipleField)pft).getIndeces());
			}
			
			
		}
		
		for(int i = 0; i < result.size(); ++i) {
			Index pi = result.get(i);
			pi.paramName = "i"+i;
		}
		
		return result;
	}
		
	

	/**
	 * the size of this array, i.e. the number of elements
	 * If this field is not fixed, then this represents the maximum bounds
	 * @return
	 */
	public int[] getNumber();
	
	/**
	 * indicates whether this field is fixed in size or whether it can change its size
	 * @return
	 */
	public boolean isFixed();
	


}
