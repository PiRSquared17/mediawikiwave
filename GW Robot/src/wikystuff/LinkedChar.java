package wikystuff;

/** represents a single character in a Linked List.
 * As additional data, we also remember the original position
 * the character had in the source String which we used to build the
 * list (see LinkMapChar for details)
 * a null link terminates the list.
 * a->b->c->d->(end/null).
 */
class LinkedChar {
	public char character='\u0000';
	public int position=-1;
	private LinkedChar nextLinkedChar=null;

	public LinkedChar() {
	}

	public LinkedChar(char character, int position) {
		this.character=character;
		this.position=position;
	}
	public LinkedChar(char character, int position, LinkedChar nextLinkedChar) {
		this.character=character;
		this.position=position;
		this.nextLinkedChar=nextLinkedChar;
	}


	public LinkedChar getNextLinkedChar() {
		return this.nextLinkedChar;
	}

	/** insert a new LinkedChar after this one.
	 * a->b ; a.insertAfter(foo); a->foo->b */
	public void insertAfter(LinkedChar newChar) {
		if (newChar!=null)
			newChar.insertAfter(this.nextLinkedChar);
		this.nextLinkedChar=newChar;
	}

	/** do a small insertion, where we don't care about the position of the characters.
	   don't use this for entire documents. */
	public void insertUnmarked(String s) {
		if (s.length()>0) {
			LinkedChar c=new LinkedChar(s.charAt(0),-1,this.getNextLinkedChar());
			this.nextLinkedChar=c;
			c.insertUnmarked(s.substring(1));
		}
	}

	/** same as above, except we insert with some offset */
	public void insertUnmarked(String s, int offset) {
		LinkedChar c=this;
		for (int i=1; i<offset; i++) {
			if (c!=null)
				c=c.getNextLinkedChar();
			else
				break;
		}	
		if (c!=null)
			c.insertUnmarked(s);
	}

	/** Returns true if there are more characters in this data structure,
	    returns false if the next link is a null (end of structure) */
	public boolean hasMoreChars() {
		return this.nextLinkedChar!=null;
	}

	/** create a new Linked List from a string,
	 * returns the first element in the list */
	public static LinkedChar buildList(String s) {

		LinkedChar list=new LinkedChar(s.charAt(0),0);
		LinkedChar current=list;
		for (int i=1;i<s.length();i++) {
			LinkedChar c=new LinkedChar(s.charAt(i),i);
			current.insertAfter(c);
			current=c;
		}
		return list;
	}
	
	/** A string representation, useful for debugging. */
	public String toString() {
		return "LinkedChar('"+this.character+"',"+this.position+")";
	}

}
