package wikystuff;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.wave.api.Annotation;
import com.google.wave.api.Range;
import com.google.wave.api.TextView;

public class WikitextToWaveConverter {
	
	private static TextView textView = null;
	private static boolean isInit = false;
	private static int lastFirstOccurence = 0;
	private static List<Annotation> lastAnnotationList = null;
	
	public static void Init(TextView a_TextView) {
		textView = a_TextView;
		isInit = true;
	
	}
	
	public static void convert(){
		if(!isInit)
			return;
		
		checkNoWiki(); //Check if there are no false annotations
		findNoWiki(); // Check if there are new <nowiki> ... </nowiki> constructions
		
		checkTemplates(); // Prevent templates from being edited
		parseTemplate(); // Parse new templates
		
		checkLinks(); //Prevent links from being edited for now
		parseLinks(); //Parse links AND images
		
		for(int i = 4; i != 0; i--) { //There are 4 levels of headings, find them all
			String l_Identifier = "";
			
			for(int j = i; j != 0; j--)
				l_Identifier += "=";
			
			replaceWikiText(l_Identifier,"styled-text","HEADING" + i);
			
			for(int j = 1; j != i; j++) {
				l_Identifier.replaceFirst("=", "");
				replaceWikiText(l_Identifier,"styled-text","HEADING" + i,"styled-text","HEADING" + j); // Just in case the bot edits before the user was done typing (We're as fast as lightning)
			}
		}
		
		replaceWikiText("'''","style/fontWeight", "bold"); // Make stuff bold
		replaceWikiText("''","style/fontStyle", "italic"); // Make stuff italic
		replaceWikiText("'","style/fontWeight", "bold","style/fontStyle", "italic"); // Just in case the bot edits before the user was done typing (We're as fast as lightning)
		
		parseLists(); //Let's show lists with perfect indentation
	}
	
	@SuppressWarnings("deprecation")
	private static void parseTemplate() {
		String text = getText("{{","}}");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		
		try {
			db = dbf.newDocumentBuilder();
			InputStream stream = XMLHandler.getXML("api.php?format=xml","action=expandtemplates&text=" + URLEncoder.encode("{{" + text + "}}"),false,false);
			if(stream == null)
				return;
			
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList node = doc.getElementsByTagName("api");
			
			node = XMLHandler.getNode(node,"expandtemplates");
			
			String expandedTemplate = XMLHandler.getNodeContent(node);
		    		    
		    textView.insert(lastFirstOccurence, expandedTemplate);
		    textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + expandedTemplate.length()), "template", "{{" + text + "}}");
		    textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + expandedTemplate.length()), "template-parsed", expandedTemplate);
		    
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
		} catch (NullPointerException e) {}
	}
	
	private static void replaceWikiText(String a_Identifier, String a_AnnotationName, String a_AnnotationValue) {
		replaceWikiText(a_Identifier, "", a_AnnotationName, a_AnnotationValue);
	}
	
	private static void replaceWikiText(String a_Identifier, String a_PrependText, String a_AnnotationName, String a_AnnotationValue) {
		String text = getText(a_Identifier);
		textView.insert(lastFirstOccurence, a_PrependText + text);
		textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + text.length()), a_AnnotationName,a_AnnotationValue);
		
		if(lastAnnotationList != null && lastAnnotationList.size() > 0) {
			for(int i = 0; i < lastAnnotationList.size(); i++) {
				Annotation annotation = lastAnnotationList.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				
				textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + text.length()), name, value);
			}
		}
	}
	
	private static void replaceWikiText(String a_Identifier, String a_AnnotationName, String a_AnnotationValue, String a_OldAnnotationName, String a_OldAnnotationValue) {
		replaceWikiText(a_Identifier, "", a_AnnotationName, a_AnnotationValue, a_OldAnnotationName, a_OldAnnotationValue);
	}
	
	private static void replaceWikiText(String a_Identifier, String a_PrependText, String a_AnnotationName, String a_AnnotationValue, String a_OldAnnotationName, String a_OldAnnotationValue) {
		boolean doReplace = false;
		String text = getText(a_Identifier);
		textView.insert(lastFirstOccurence, text);
		textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + text.length()), a_AnnotationName,a_AnnotationValue);
		
		if(lastAnnotationList != null && lastAnnotationList.size() > 0) {
			for(int i = 0; i < lastAnnotationList.size(); i++) {
				Annotation annotation = lastAnnotationList.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				
				if(a_OldAnnotationName.equals(name) && a_OldAnnotationValue.equals(value))
					doReplace = true;
				
				textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + text.length()), name, value);
			}
		}
		
		if(!doReplace) {
			if(text.length() == 0)
				return;
			
			textView.delete(new Range(lastFirstOccurence,lastFirstOccurence + text.length()));
			textView.insert(lastFirstOccurence, a_Identifier + text + a_Identifier);
			
			if(lastAnnotationList != null && lastAnnotationList.size() > 0) {
				for(int i = 0; i < lastAnnotationList.size(); i++) {
					Annotation annotation = lastAnnotationList.get(i);
					String name = annotation.getName();
					String value = annotation.getValue();
					
					if(a_OldAnnotationName.equals(name) && a_OldAnnotationValue.equals(value))
						doReplace = true;
					
					textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + text.length() + (a_Identifier.length() * 2)), name, value);
				}
			}
		}
	}
	
	private static String getText(String a_Identifier) {
		return getText(a_Identifier,a_Identifier);
	}
	
	private static int findOccurence(String text) {
		return findOccurence(text,0);
	}
	
	private static int findOccurence(String text, int start) {
		int occurence = textView.getText().indexOf(text,start);
		
		if(occurence == -1)
			return -1;
		
		List<Annotation> tempList = textView.getAnnotations(new Range(occurence, occurence + 1));
		if(tempList.size() > 0) {
			for(int i = 0; i < tempList.size(); i++) {
				Annotation annotation = tempList.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				
				if(name.equals("wikitext") || value.equals("nowiki"))
					occurence = findOccurence(text,occurence + 1);
			}
		}
		
		return occurence;
	}
		
	private static String getText(String a_BeginIdentifier, String a_EndIdentifier) {
		int first_occurence = findOccurence(a_BeginIdentifier);
		if(first_occurence == -1) {
			return "";
		}
		
		int second_occurence = findOccurence(a_EndIdentifier,first_occurence + a_BeginIdentifier.length());
		if(second_occurence == -1) {
			return "";
		}
		
		int delta = second_occurence - first_occurence;
		
		if(delta <= a_BeginIdentifier.length()) {
			return "";
		}
		
		Range range = new Range(first_occurence + a_BeginIdentifier.length(), second_occurence);
		
		String text = textView.getText(range);
		
		lastAnnotationList = textView.getAnnotations(range);
		
		range = new Range(first_occurence, second_occurence + a_EndIdentifier.length());
		
		textView.delete(range);
		lastFirstOccurence = first_occurence;
		return text;
	}
	
	private static void findNoWiki() {
		String text = getText("<nowiki>","</nowiki>");
		if(text.equals(""))
			return;
		
		textView.insert(lastFirstOccurence, "<nowiki>" + text + "</nowiki>");
		textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + text.length()), "wikitext","nowiki");
		
		WikitextConverter.convert(textView,lastAnnotationList);
	}
	
	private static void checkNoWiki() {
		List<Annotation> tempList = textView.getAnnotations();
		
		if(tempList.size() > 0) {
			for(int i = 0; i < tempList.size(); i++) {
				Annotation annotation = tempList.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				
				if(name.equals("wikitext") || value.equals("nowiki")) {
					String content = textView.getText(annotation.getRange());
					if(!content.startsWith("<nowiki>") || !content.endsWith("</nowiki>")) {
						textView.deleteAnnotations(annotation.getRange());
					}
				}
			}
		}
	}
	
	private static void checkTemplates() {
		List<Annotation> tempList = textView.getAnnotations();
		
		if(tempList.size() > 0) {
			for(int i = 0; i < tempList.size(); i++) {
				Annotation annotation = tempList.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				
				if(name.equals("template-parsed")) {
					if(!WikitextConverter.convert(textView,textView.getAnnotations(annotation.getRange())).equals(value)) {
						textView.replace(annotation.getRange(), value);
					}
				}
			}
		}
	}

	private static void parseLists() {
		List<Annotation> tempList = textView.getStyles();
		fixindentiaton(tempList); // First fix the indentation of old lists
		replaceWikiText("\n*","\n","styled-text","BULLETED"); // Lets get lists
		tempList = textView.getStyles();
		//fixindentiaton(tempList); // Fix the indentiaton of new lists
		
	}
	
	private static void fixindentiaton(List<Annotation> tempList) {
		return;
	}

	@SuppressWarnings("deprecation")
	private static void parseLinks(){
		String text = getText("{{","}}");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		
		try {
			db = dbf.newDocumentBuilder();
			InputStream stream = XMLHandler.getXML("api.php?format=xml","action=parse&text=" + URLEncoder.encode("[[" + text + "]]"),false,false);
			if(stream == null)
				return;
			
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList node = doc.getElementsByTagName("api");
			node = XMLHandler.getNode(node,"parse");
			
			node = XMLHandler.getNode(node,"text");
			
			String parsedLink = XMLHandler.getNodeContent(node);
			
			int occurence = parsedLink.indexOf("<p>");
			if(occurence != -1)
				parsedLink = parsedLink.substring(occurence);
			
			occurence = parsedLink.indexOf("</p>");
				if(occurence != -1)
					parsedLink = parsedLink.substring(0,occurence);
		    		    
			parsedLink.replace("href=\"/", "href=\""+ XMLHandler.getURL() +"/");
				
		    textView.insert(lastFirstOccurence, parsedLink);
		    textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + parsedLink.length()), "link", "[[" + text + "]]");
		    textView.setAnnotation(new Range(lastFirstOccurence,lastFirstOccurence + parsedLink.length()), "link-parsed", parsedLink);
		    
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
		} catch (NullPointerException e) {}
	}
	
	private static void checkLinks() {
		List<Annotation> tempList = textView.getAnnotations();
		
		if(tempList.size() > 0) {
			for(int i = 0; i < tempList.size(); i++) {
				Annotation annotation = tempList.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				
				if(name.equals("link-parsed")) {
					if(!WikitextConverter.convert(textView,textView.getAnnotations(annotation.getRange())).equals(value)) {
						textView.replace(annotation.getRange(), value);
					}
				}
			}
		}
	}
}
