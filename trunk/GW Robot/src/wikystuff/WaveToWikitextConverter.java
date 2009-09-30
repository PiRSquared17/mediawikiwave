package wikystuff;

import java.util.List;

import com.google.wave.api.Annotation;
import com.google.wave.api.TextView;

public class WaveToWikitextConverter {
	private TextView m_TextView;
	
	public String convert(TextView a_TextView) {
		m_TextView = a_TextView;
		
		replace("style/fontWeight","bold","'''");
		replace("style/fontStyle","italic","''");
		
		return m_TextView.getText();
	}
	
	//pre- and post-tag the same
	private void replace(String a_AnnotationName, String a_AnnotationValue, String a_Tag){
		replace(a_AnnotationName,a_AnnotationValue,a_Tag,a_Tag);
	}
	
	//pre- and post-tag differ
	private void replace(String a_AnnotationName, String a_AnnotationValue,String a_PreTag, String a_PostTag){
		List<Annotation> l_AnnotationList = m_TextView.getAnnotations(a_AnnotationName);
		for(int i = 0; i < l_AnnotationList.size(); i++) {
			Annotation l_Annotation = l_AnnotationList.get(i);
			if(l_Annotation.getValue().compareTo(a_AnnotationValue) == 0) {
				m_TextView.insert(l_Annotation.getRange().getEnd(), a_PostTag);
				m_TextView.insert(l_Annotation.getRange().getStart(), a_PreTag);
			}
		}
		
		m_TextView.deleteAnnotations(a_AnnotationName);
	}
}
