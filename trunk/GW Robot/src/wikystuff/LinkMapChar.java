package wikystuff;


import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.google.wave.api.*;

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
		renumber();
	}
			
	/** (re)number all elements, starting at 0 */
	public void renumber() {
		
		this.map=new HashMap<Integer,LinkedChar>();
		
		int position=0;
		LinkedChar currentChar=this.list;
		while (currentChar.hasMoreChars()) {
			currentChar.position=position;
			map.put(new Integer(position),currentChar);
			currentChar=currentChar.getNext();
			position+=1;
		}
		currentChar.position=position;
		map.put(new Integer(currentChar.position),currentChar);

	}

	/** Converts back to a normal String */		
	public String toString() {
		LinkedChar currentChar=this.list;
		StringBuffer s=new StringBuffer();
		while (currentChar.hasMoreChars()) {
			s=s.append(currentChar.character);
			currentChar=currentChar.getNext();
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
		LinkedChar location=getAt(position);
		location.insertUnmarked(s);
	}

	/** insert additional unmarked (no position data) characters from a String at
	 position p in this data structure, with an extra offset.
	 See also: Other insertUnmarkedAt, above  */
	public void insertUnmarkedAt(int position, String s, int offset) {
		LinkedChar location=getAt(position);
		location.insertUnmarked(s,offset);
	}


	/** Delete the chracter that was originally at the specified position. */
	public void deleteAt(int position) {
		LinkedChar location=getAt(position);
		location.suicide();
		map.remove(new Integer(position));
	}


	/** Delete the characters that originally started at fromPosition, and ended at toPosition.
	 *  Be careful:  If characters were inserted in the mean time, you might end up deleting something other than you
	 * than you bargained for,
	 */
	public void deleteAt(int fromPosition,int toPosition) {
		for (int position=fromPosition; position<toPosition; position++) {
			deleteAt(position);
		}
	}

	public void deleteAt(Range r) {
		deleteAt(r.getStart(), r.getEnd());
	}	

	public LinkedChar getAt(int position) {
		LinkedChar location=map.get(new Integer(position));
		if (location==null)
			throw new NullPointerException("Position "+position+" is either out of range or deleted.");
		return location;
	}	



	public void setAnnotation(Annotation annotation) {
		String name=annotation.getName();
		String value=annotation.getValue();
		Range range=annotation.getRange();
		setAnnotation(range, name, value);
	}

	public void setAnnotation(Range range, String name, String value) {
	/** set annotation on a character-by-character basis. 
  	 * Externally,  this works similar to the normal setAnnotation() used in wave textView.
	 * Attempts to set Annotations on deleted or out-of-bounds characters will
 	 * simply be ignored. (So you can apply annotations correctly even after having done a delete)
	 */
		
		// Is range.getEnd() inclusive or exclusive the character at that position? 
		for (int position=range.getStart(); position<range.getEnd(); position++) {
			try {
				Range singleRange=new Range(position, position+1);
				Annotation singleAnnotation=new Annotation(name, value, singleRange);
				LinkedChar location=getAt(position);
				location.addAnnotation(singleAnnotation);
			} catch (NullPointerException ignored) {}
		}

	}

	public void setAnnotations(List<Annotation> annotationList) {
		for (Annotation annotation: annotationList) {
			setAnnotation(annotation);
		}	
	}

	/** get a list of annotations for this text.
	  * annotations are stored on a character-by-character basis internally. This
	  * won't do for wave, so we coalesce them back into annotations over spans */
	public ArrayList<Annotation> getAnnotations() {
		ArrayList<Annotation> annotationList=new ArrayList<Annotation>();
		HashMap<ArrayList,Annotation> tracking=new HashMap<ArrayList,Annotation>();
		

		LinkedChar currentChar=this.list;
		/* We're storing annotations on a per-character basis. 
		 * we need to coalesce these annotations into larger annotations
		 * across spans.
		 */
		do {
			//System.out.println(currentChar);
			/*first, we discover where annotations of a certain
			* name+value start, and track them 
			*/
			List<Annotation> charAnnotations=currentChar.getAnnotations();
			HashSet<ArrayList> currentKeys=new HashSet<ArrayList>();
			for (Annotation charAnnotation: charAnnotations) {
				String name=charAnnotation.getName();
				String value=charAnnotation.getValue();
				int charStart=charAnnotation.getRange().getStart();
				int charEnd=charAnnotation.getRange().getEnd();
				
				/* we use name,value pairs for keys*/
				ArrayList<String> key=new ArrayList<String>();
				key.add(name);
				key.add(value);

				currentKeys.add(key); /* track which keys were used in this char */

				if (tracking.containsKey(key)) {
					/* update annotations that are already being tracked */
					Annotation updateable=tracking.get(key);
					updateable.setRange(new Range(updateable.getRange().getStart(),charEnd));
				} else {
					/* add a new annotation to the tracker */
					Annotation newFound=new Annotation(name, value, new Range(charStart,charEnd));
					tracking.put(key,newFound);
				}
				
			}
			
			/* Now, find out if an annotation stops here */
			Set<ArrayList> stopsHere=new HashSet<ArrayList>();
			Set<ArrayList> trackingKeys=tracking.keySet();
			for (ArrayList trackingKey: trackingKeys) {
				if (!currentKeys.contains(trackingKey)) 
					stopsHere.add(trackingKey);
			}
			
			/* If it stops, we add it to our final list, and stop updating it. */
			for (ArrayList stopped : stopsHere) {
				annotationList.add(tracking.get(stopped));
				tracking.remove(stopped);
			}		
		} while ((currentChar=currentChar.getNext())!=null);

		return annotationList;
	}
}

