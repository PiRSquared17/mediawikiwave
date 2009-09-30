package wikystuff;

/** represents a single character in a Linked List.
 * a null link terminates the list.
 * a->b->c->d->(end/null).
 */
class LinkedChar {
	public char character=null;
	public int position=-1;
	private LinkedChar nextLinkedChar=null;


	public LinkedChar() {
	}

	public LinkedChar(char character, int position) {
		this.character=character;
		this.position=position;
	}

	public LinkedChar getNextLinkedChar() {
		return this.nextLinkedChar;
	}

	/** insert a new LinkedChar after this one.
	 * a->b ; a.insertAfter(foo); a->foo->b */
	public insertAfter(LinkedChar newChar) {
		newChar.insertAfter(this.nextLinkedChar);
		this.nextLinkedChar=newChar;
	}

	public insertUnmarked(String s) {
		for (int i=0;i<s.length();i++) {
			LinkedChar c=new LinkedChar(s.charAt(i),-1);
			this.insertAfter(c);
		}
	}

	public hasMoreChars() {
		return this.nextLinkedChar!=null;
	}

	/** create a new Linked List from a string,
	 * returns the first element in the list */
	public static LinkedChar buildList(String s) {
		
		LinkedChar list=new LinkedChar(s.charAt(0),0);
		for (int i=1;i<s.length();i++) {
			list.insertAfter(s.charAt(i), i)
		}
		return list;
	}

}
