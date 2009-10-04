package wikystuff;
import com.google.wave.api.*;

import java.util.ArrayList;

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
	private LinkedChar previousLinkedChar=null;
	private ArrayList<Annotation> annotations=null;

	public LinkedChar() {
		this.annotations=new ArrayList<Annotation>();
	}

	public LinkedChar(char character, int position) {
		this.character=character;
		this.position=position;
		this.annotations=new ArrayList<Annotation>();
	}

	public LinkedChar(char character, int position, LinkedChar nextLinkedChar) {
		this.character=character;
		this.position=position;
		this.nextLinkedChar=nextLinkedChar;
		this.annotations=new ArrayList<Annotation>();
	}

	/** get the next character in the Linked List */
	public LinkedChar getNext() {
		return this.nextLinkedChar;
	}

	/** get the next character in the Linked List */
	protected void setNext(LinkedChar nextLinkedChar) {
		this.nextLinkedChar=nextLinkedChar;
	}

	/** get the next character in the Linked List */
	public LinkedChar getPrevious() {
		return this.previousLinkedChar;
	}

	/** get the next character in the Linked List */
	protected void setPrevious(LinkedChar previousLinkedChar) {
		this.previousLinkedChar=previousLinkedChar;
	}
	
	public void addAnnotation(Annotation annotation) {
		this.annotations.add(annotation);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Annotation> getAnnotations() {
		ArrayList<Annotation> annotations= (ArrayList<Annotation>) this.annotations.clone();
		for (Annotation annotation: annotations) {
			annotation.setRange(new Range(this.position,this.position+1));
		}
		return annotations;	
	}
	/** insert a new LinkedChar after this one.
	 * a->b ; a.insertAfter(foo); a->foo->b */
	public void insertAfter(LinkedChar newChar) {
		if (newChar!=null)
			newChar.setPrevious(this);
			newChar.setNext(this.getNext());
		this.setNext(newChar);
	}

	/** do a small insertion, where we don't care about the position of the characters.
	   don't use this for entire documents. Future TODO: Current implementation is tail-recursive-ish
	   replace with a loop? */
	public void insertUnmarked(String s) {
		if (s.length()>0) {
			LinkedChar c=new LinkedChar(s.charAt(0),-1);
			this.insertAfter(c);
			c.insertUnmarked(s.substring(1));
		}
	}

	/** same as above, except we insert at some offset to the right*/
	public void insertUnmarked(String s, int offset) {
		LinkedChar c=this;
		for (int i=1; i<offset; i++) {
			if (c!=null)
				c=c.getNext();
			else
				break;
		}	
		if (c!=null)
			c.insertUnmarked(s);
	}

	/** Returns true if there are more characters in this data structure,
	    returns false if there are no more characters. (The end of the list
	 is denoted by a null link)  */
	public boolean hasMoreChars() {
		return this.nextLinkedChar!=null;
	}

	public void delAfter() {
		LinkedChar beyondNext=null;
		LinkedChar next=this.getNext();
		if (next!=null)
			beyondNext=next.getNext();
		if (beyondNext!=null)
			beyondNext.setPrevious(this);
		this.setNext(beyondNext);
	}

	public void suicide() {
		LinkedChar previous=this.getPrevious();
		LinkedChar next=this.getNext();
		if (previous!=null) 
			previous.setNext(next);
		if (next!=null)
			next.setPrevious(previous);
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
