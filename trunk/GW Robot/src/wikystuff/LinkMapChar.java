//package wikystuff;


import java.util.HashMap;

/** a hybrid data structure:  linked list of chars, retaining the indexes the chars had at time of creation.
 * The reason for having this is because Annotations work exactly wrong for us atm.
 * If we insert items into a TextView, the Annotations don't shift along with the
 * insertion for whatever reason. So we need to keep track of the original
 * indexes, so that we insert at the right location. 
 */

class LinkMapChar {
	private LinkedChar list=null;
	private HashMap<int><LinkedChar> map=new HashMap<int><LinkedChar>;
	
	/** creates a new data structure from string*/
	public LinkMapChar(String s) {
		this.list=LinkedChar.buildList(s);

		LinkedChar currentChar=this.list;
		while (currentChar.hasMoreChars()) {
			map.put(currentChar.position,currentChar);
			currentChar=currentChar.getNextLinkedChar();
		}
	}
			
	/** Returns the string representation of this structure */		
	public String toString() {
		LinkedChar currentChar=this.list;
		StringBuffer s=new StringBuffer();
		while (currentChar.hasMoreChars()) {
			s=s.append(currentChar.character);
			currentChar=currentChar.getNextLinkedChar();
		}
		return s.toString();
	}	

	/** insert additional unmarked (no position data) String at
	 position p in this data structure */
	public insertUnmarkedAt(int position, String s) {
		LinkedChar location=map.get(position);
		location.insertUnmarked(s);
	}
}

