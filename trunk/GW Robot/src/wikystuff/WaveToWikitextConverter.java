package wikystuff;

import java.util.List;

import com.google.wave.api.Annotation;
import com.google.wave.api.Range;
import com.google.wave.api.TextView;

public class WaveToWikitextConverter {
	private TextView m_TextView;
	
	public String convert(TextView a_TextView) {
		m_TextView = a_TextView;
		
		
		LinkMapChar list=new LinkedMapChar(m_TextView.getText());

		replace(list, "style/fontWeight","bold","'''",true);
		replace(list, "style/fontStyle","italic","''",true);
		replace(list, "styled-text","HEADING4","====",false);
		replace(list, "styled-text","HEADING3","===",false);
		replace(list, "styled-text","HEADING2","==",false);
		replace(list, "styled-text","HEADING1","=",false);
		replace(list, "styled-text","BULLETED","*","\n",true);
	
		
		m_TextView.replace(list.toString());	
		replaceSpecial("wiki/InternalLink");
		replaceSpecial("wiki/ExternalLink");
		replaceSpecial("wiki/Template");
		
		
		return m_TextView.getText();
	}
	
	//pre- and post-tag the same
	private void replace(String a_AnnotationName, String a_AnnotationValue, String a_Tag, boolean a_RemoveAnnotations){
		replace(a_AnnotationName,a_AnnotationValue,a_Tag,a_Tag,a_RemoveAnnotations);
	}
	
	//pre- and post-tag differ
	/*
	private void replace(String a_AnnotationName, String a_AnnotationValue,String a_PreTag, String a_PostTag, boolean a_RemoveAnnotations){
		List<Annotation> l_AnnotationList = m_TextView.getAnnotations(a_AnnotationName);
		for(int i = 0; i < l_AnnotationList.size(); i++) {
			Annotation l_Annotation = l_AnnotationList.get(i);
			if(l_Annotation.getValue().compareTo(a_AnnotationValue) == 0) {
				this.m_TextView.insert(l_Annotation.getRange().getEnd(), a_PostTag);
				this.m_TextView.insert(l_Annotation.getRange().getStart(), a_PreTag);
			}
		}
		if(a_RemoveAnnotations)
			m_TextView.deleteAnnotations(a_AnnotationName);
	}
	*/


	


	private void replace(LinkedChar list,String a_AnnotationName, String a_AnnotationValue,String a_PreTag, String a_PostTag, boolean a_RemoveAnnotations){
				
			
		List<Annotation> l_AnnotationList = m_TextView.getAnnotations(a_AnnotationName);
		for(int i = 0; i < l_AnnotationList.size(); i++) {
			Annotation l_Annotation = l_AnnotationList.get(i);
			if(l_Annotation.getValue().compareTo(a_AnnotationValue) == 0) {
				list.insertUnmarkedAt(l_Annotation.getRange().getEnd(), a_PostTag);
				list.insertUnmarkedAt(l_Annotation.getRange().getStart(), a_PreTag);
			}
		}
	}	

	private void replaceSpecial(String a_AnnotationName) {
		List<Annotation> l_AnnotationList = m_TextView.getAnnotations(a_AnnotationName);
		for(int i = 0; i < l_AnnotationList.size(); i++) {
			Annotation l_Annotation = l_AnnotationList.get(i);
			//String l_CurrentText = this.m_TextView.getText(l_Annotation.getRange());
			String l_NewText = l_Annotation.getValue();
			this.m_TextView.delete(new Range(l_Annotation.getRange().getStart(), l_Annotation.getRange().getEnd()));
			this.m_TextView.insert(l_Annotation.getRange().getStart(), l_NewText);
		}
		m_TextView.deleteAnnotations(a_AnnotationName);
	}
}
