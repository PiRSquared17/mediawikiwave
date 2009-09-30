package wikystuff;

import com.google.wave.api.Range;
import com.google.wave.api.TextView;

public class WikitextToWaveConverter {
	private TextView m_TextView;
	
	public String convert(TextView a_TextView) {
		m_TextView = a_TextView;
		
		boolean doReplace = true;
		while(doReplace) {
			doReplace = replace("'''","style/fontWeight","bold");
			doReplace |= replace("''","style/fontStyle","italic");
		}
		
		return m_TextView.getText();
	}
	
	//pre- and post-tag the same
	
	private boolean replace(String a_Tag, String a_AnnotationName, String a_AnnotationValue){
		return replace(a_Tag,a_Tag,a_AnnotationName,a_AnnotationValue);
	}
	
	//pre- and post-tag differ
	private boolean replace(String a_PreTag, String a_PostTag, String a_AnnotationName, String a_AnnotationValue){
		//Search for starttag
		int l_StartPos = m_TextView.getText().indexOf(a_PreTag);
		if(l_StartPos == -1) //Pre tag not found
			return false;
		
		//Search for endtag
		int l_EndPos = m_TextView.getText().indexOf(a_PostTag, l_StartPos + a_PreTag.length());
		if(l_EndPos == -1)
			return false;
		
		l_EndPos += a_PostTag.length();
		
		//Remove tags
		m_TextView.delete(new Range(l_EndPos - a_PostTag.length(),l_EndPos));
		m_TextView.delete(new Range(l_StartPos,l_StartPos + a_PreTag.length()));
		
		//Apply Annotation
		m_TextView.setAnnotation(new Range(l_StartPos,l_EndPos - a_PreTag.length() - a_PostTag.length()), a_AnnotationName, a_AnnotationValue);
		return true;
	}
	
	//only a pre-tag
}
