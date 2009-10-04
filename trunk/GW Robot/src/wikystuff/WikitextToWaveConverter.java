package wikystuff;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.wave.api.Range;
import com.google.wave.api.TextView;
import com.google.wave.api.Annotation;

public class WikitextToWaveConverter {
	private TextView m_TextView;
	private Logger log = Logger.getLogger(WikitextToWaveConverter.class.getName());
	private LinkMapChar lmc;	

	public void convert(TextView a_TextView) {
		m_TextView = a_TextView;
		
		this.lmc=new LinkMapChar(a_TextView.getText());
		boolean doReplace = true;
		while(doReplace) {
			doReplace = replace("'''","style/fontWeight","bold");
			doReplace |= replace("''","style/fontStyle","italic");
			doReplace |= replace("====","styled-text","HEADING4");
			doReplace |= replace("===","styled-text","HEADING3");
			doReplace |= replace("==","styled-text","HEADING2");
			doReplace |= replace("=","styled-text","HEADING1");
			doReplace |= replaceLists();
			doReplace |= replaceParsed("[[", "]]", "wiki/InternalLink");
			doReplace |= replaceParsed("[", "]", "wiki/ExternalLink");
			doReplace |= replaceParsed("{{", "}}", "wiki/Template");
		}
		
		m_TextView.replace(this.lmc.toString());
		lmc.renumber();
		for (Annotation annotation: lmc.getAnnotations()) {
			m_TextView.setAnnotation(annotation.getRange(), annotation.getName(), annotation.getValue());
		}
	}
	
	//pre- and post-tag the same
	
	private boolean replace(String a_Tag, String a_AnnotationName, String a_AnnotationValue){
		return replace(a_Tag,a_Tag,a_AnnotationName,a_AnnotationValue);
	}
	
	//pre- and post-tag differ
	private boolean replace(String a_PreTag, String a_PostTag, String a_AnnotationName, String a_AnnotationValue){

		//Search for starttag
		int l_StartPos = this.lmc.toString().indexOf(a_PreTag);
		if(l_StartPos == -1) //Pre tag not found
			return false;
		
		//Search for endtag
		int l_EndPos = this.lmc.toString().indexOf(a_PostTag, l_StartPos + a_PreTag.length());
		if(l_EndPos == -1)
			return false;
		
		lmc.setAnnotation(new Range(l_StartPos,l_EndPos), a_AnnotationName, a_AnnotationValue);
		l_EndPos += a_PostTag.length();
		
		//Remove tags
		lmc.deleteAt(new Range(l_EndPos - a_PostTag.length(),l_EndPos));
		lmc.deleteAt(new Range(l_StartPos,l_StartPos + a_PreTag.length()));
		
		lmc.renumber();
		//Apply Annotation
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean replaceParsed(String a_PreTag, String a_PostTag, String a_AnnotationName) {
		int l_StartPos = m_TextView.getText().indexOf(a_PreTag);
		if(l_StartPos == -1) //Pre tag not found
			return false;
		
		//Search for endtag
		int l_EndPos = m_TextView.getText().indexOf(a_PostTag, l_StartPos + a_PreTag.length());
		if(l_EndPos == -1)
			return false;
		
		l_EndPos += a_PostTag.length();
		
		//Get text
		String l_Text = m_TextView.getText(new Range(l_StartPos, l_EndPos));
		
		//Remove tags
		m_TextView.delete(new Range(l_StartPos,l_EndPos));
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		
		try {
			db = dbf.newDocumentBuilder();
			String getVar = "action=parse&text=" + URLEncoder.encode(l_Text);
			InputStream stream = XMLHandler.getXML("api.php?format=xml",getVar,false,false);
			log.info("Getvar: " + getVar);
			if(stream == null) {
				log.severe("No stream!");
				return false;
			}
			
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList node = doc.getElementsByTagName("api");
			
			node = XMLHandler.getNode(node,"parse");
			node = XMLHandler.getNode(node,"text");
			
			String result = URLDecoder.decode(XMLHandler.getNodeContent(node));
			log.info("result: " + result);
			log.info("Start pos:" + l_StartPos);
			m_TextView.appendMarkup(result);
			
			//Apply Annotation
			//m_TextView.setAnnotation(new Range(l_StartPos,l_StartPos + result.length()), a_AnnotationName, l_Text);
			return true;
		} catch (Exception e){ return false;}
	}
	
	private boolean replaceLists() {
		//Search for starttag
		int l_StartPos = m_TextView.getText().indexOf("*");
		if(l_StartPos == -1) //Pre tag not found
			return false;
		
		//Search for endtag
		int l_EndPos = m_TextView.getText().indexOf("\n", l_StartPos + 1);
		if(l_EndPos == -1)
			return false;
		
		//Remove tags
		m_TextView.delete(new Range(l_StartPos,l_StartPos + 1));
		
		//Apply Annotation
		m_TextView.setAnnotation(new Range(l_StartPos,l_EndPos), "styled-text", "BULLETED");
		return true;
	}
	
	//only a pre-tag
}
