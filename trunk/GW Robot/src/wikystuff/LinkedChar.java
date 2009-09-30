//package wikystuff;

/** represents a single character in a Linked List.
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

	public void insertUnmarked(String s) {
		for (int i=0;i<s.length();i++) {
			LinkedChar c=new LinkedChar(s.charAt(i),-1);
			this.insertAfter(c);
		}
	}

	public boolean hasMoreChars() {
		return this.nextLinkedChar!=null;
	}

	/** create a new Linked List from a string,
	 * returns the first element in the list */
	public static LinkedChar buildList(String s) {
		System.out.println(s);		

		LinkedChar list=new LinkedChar(s.charAt(0),0);
		LinkedChar current=list;
		for (int i=1;i<s.length();i++) {
			LinkedChar c=new LinkedChar(s.charAt(i),i);
			current.insertAfter(c);
			current=c;
		}
		return list;
	}
	
	public String toString() {
		return "LinkedChar('"+this.character+"',"+this.position+")";
	}

}
