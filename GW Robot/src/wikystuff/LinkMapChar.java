package wikystuff;


import java.util.HashMap;

/** a hybrid data structure:  linked list of chars, retaining the indexes the chars had in the
 * source string. (basically a not-so-glorified StringBuffer, it just remembers the original indexes 
 * from the String we used to make it with)
 * The reason for having this is because Annotations work exactly wrong for us atm.
 * If we insert items into a TextView, the Annotations don't shift along with the
 * insertion for whatever reason. So we need to keep track of the original
 * indexes, so that we insert at the right location.
 * I didn't have much time to come up with a meaningful name. 
 */

class LinkMapChar {
	private LinkedChar list;
	private HashMap<Integer,LinkedChar> map;
	
	/** creates a new data structure from String, remembering the 
	 original index of each character.*/
	public LinkMapChar(String s) {
		this.list=LinkedChar.buildList(s);
		this.map=new HashMap<Integer,LinkedChar>();

		LinkedChar currentChar=this.list;
		while (currentChar.hasMoreChars()) {
			map.put(new Integer(currentChar.position),currentChar);
			currentChar=currentChar.getNextLinkedChar();
		}
		map.put(new Integer(currentChar.position),currentChar);
	}
			
	/** Converts back to a normal String */		
	public String toString() {
		LinkedChar currentChar=this.list;
		StringBuffer s=new StringBuffer();
		while (currentChar.hasMoreChars()) {
			s=s.append(currentChar.character);
			currentChar=currentChar.getNextLinkedChar();
		}
		s=s.append(currentChar.character);
		return s.toString();
	}	

	/** insert additional unmarked (no position data) characters
          from a String at a specified index (int position). The index corresponds 
	  to where it would have been in the original String, no matter what changes
	  have been made to this data structure in the mean time.
	  (current design limitation: don't use large Strings for s */

	public void insertUnmarkedAt(int position, String s) {
		LinkedChar location=map.get(new Integer(position));
		location.insertUnmarked(s);
	}

	/** insert additional unmarked (no position data) characters from a String at
	 position p in this data structure, with an extra offset.
	 See also: Other insertUnmarkedAt, above  */
	public void insertUnmarkedAt(int position, String s, int offset) {
		LinkedChar location=map.get(new Integer(position));
		location.insertUnmarked(s,offset);
	}

}

