package wikystuff;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.wave.api.Annotation;
import com.google.wave.api.Range;
import com.google.wave.api.StyleType;
import com.google.wave.api.StyledText;
import com.google.wave.api.TextView;

public class WikitextToWaveConverter {
	public static void convert(TextView a_textView){
		parseTemplate(a_textView);
		convertBold(a_textView);
		convertItalic(a_textView);
		convertHeading(a_textView);
	}
	
	private static void convertItalic(TextView a_textView) {
		int first_occurence = a_textView.getText().indexOf("''");
		if(first_occurence == -1) {
			return;
		}
		
		int second_occurence = a_textView.getText().indexOf("''",first_occurence + 2);
		if(second_occurence == -1) {
			return;
		}
		
		int delta = second_occurence - first_occurence;
		
		if(delta <= 2) {
			return;
		}
		
		Range range = new Range(first_occurence + 2, second_occurence);
		
		String text = a_textView.getText(range);
		
		List<Annotation> list = a_textView.getAnnotations(range);
		
		range = new Range(first_occurence, second_occurence + 2);
		
		a_textView.delete(range);
		
		/*StyledText styledtext = new StyledText();
		styledtext.addStyle(StyleType.BOLD);
		styledtext.appendText(text);
		
		a_textView.insertStyledText(first_occurence, styledtext);*/
		a_textView.insert(first_occurence, text);
		a_textView.setAnnotation(new Range(first_occurence,first_occurence + text.length()), "style/fontStyle", "italic");
		
		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Annotation annotation = list.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				
				a_textView.setAnnotation(new Range(first_occurence,first_occurence + text.length()), name, value);
			}
		}
	}
	
	private static void convertBold(TextView a_textView) {
		int first_occurence = a_textView.getText().indexOf("'''");
		if(first_occurence == -1) {
			return;
		}
		
		int second_occurence = a_textView.getText().indexOf("'''",first_occurence + 3);
		if(second_occurence == -1) {
			return;
		}
		
		int delta = second_occurence - first_occurence;
		
		if(delta <= 3) {
			return;
		}
		
		Range range = new Range(first_occurence + 3, second_occurence);
		
		String text = a_textView.getText(range);
		
		List<Annotation> list = a_textView.getAnnotations(range);
		
		range = new Range(first_occurence, second_occurence + 3);
		
		a_textView.delete(range);
		
		/*StyledText styledtext = new StyledText();
		styledtext.addStyle(StyleType.BOLD);
		styledtext.appendText(text);
		
		a_textView.insertStyledText(first_occurence, styledtext);*/
		a_textView.insert(first_occurence, text);
		a_textView.setAnnotation(new Range(first_occurence,first_occurence + text.length()), "style/fontWeight", "bold");
		
		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Annotation annotation = list.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				
				a_textView.setAnnotation(new Range(first_occurence,first_occurence + text.length()), name, value);
			}
		}
	}
	
	private static void convertHeading(TextView a_textView) {
		int first_occurence = a_textView.getText().indexOf("==");
		if(first_occurence == -1) {
			return;
		}
		
		int second_occurence = a_textView.getText().indexOf("==",first_occurence + 1);
		if(second_occurence == -1) {
			return;
		}
		
		int delta = second_occurence - first_occurence;
		
		if(delta <= 2) {
			return;
		}
		
		Range range = new Range(first_occurence + 2, second_occurence);
		
		String text = a_textView.getText(range);
		
		range = new Range(first_occurence, second_occurence + 2);
		
		a_textView.delete(range);
		
		StyledText styledtext = new StyledText();
		styledtext.addStyle(StyleType.HEADING1);
		styledtext.appendText(text);
		
		a_textView.insertStyledText(first_occurence, styledtext);
	}
	
	private static InputStream getXML(String a_GetVar) {
		try{
			URL url = new URL("http://dev.12wiki.eu/api.php?format=xml" + "&" + a_GetVar);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        return connection.getInputStream();
		}catch (IOException e) {
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void parseTemplate(TextView a_textView) {
		int first_occurence = a_textView.getText().indexOf("{{");
		if(first_occurence == -1) {
			return;
		}
		
		int second_occurence = a_textView.getText().indexOf("}}",first_occurence + 1);
		if(second_occurence == -1) {
			return;
		}
		
		int delta = second_occurence - first_occurence;
		
		if(delta <= 0) {
			return;
		}
		
		Range range = new Range(first_occurence, second_occurence + 2);
		
		String text = a_textView.getText(range);
		
		range = new Range(first_occurence, second_occurence + 2);
		
		a_textView.delete(range);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		
		try {
			db = dbf.newDocumentBuilder();
			InputStream stream = getXML("action=expandtemplates&text=" + URLEncoder.encode(text));
			if(stream == null)
				return;
			
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("api");
			
			Node fstNode = nodeLst.item(0);
			Element fstElmnt = (Element) fstNode;
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("expandtemplates");
			
			Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		    NodeList fstNm = fstNmElmnt.getChildNodes();
		    String expandedTemplate = ((Node) fstNm.item(0)).getNodeValue();
		    		    
		    a_textView.insert(first_occurence, expandedTemplate);
		    a_textView.setAnnotation(new Range(first_occurence,first_occurence + expandedTemplate.length()), "template", text);
		    
		} catch (SAXException e) {
			//textView.append("\nSAXException");
		} catch (IOException e) {
			//textView.append("\nIOException");
		} catch (ParserConfigurationException e) {
			//textView.append("\nParserConfigurationException");
		}
		
		return;
	}
}
