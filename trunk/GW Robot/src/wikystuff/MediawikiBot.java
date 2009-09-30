package wikystuff;

import com.google.wave.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MediawikiBot {
	private String a_URL;
	private TextView textView;
	
	private String edittoken;
	
	Logger log = Logger.getLogger(MediawikiBot.class.getName());
	
	public MediawikiBot (String a_URL, TextView a_TextView) {
		if(!a_URL.endsWith("/"))
			a_URL += "/";
		
		XMLHandler.Init(a_URL);
		textView = a_TextView;
	}
	
	public String getURL() { return a_URL; }
	
	public Boolean login(String a_Username, String a_Password) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		
		try {
			db = dbf.newDocumentBuilder();
			InputStream stream = XMLHandler.getXML("api.php?format=xml","action=login&lgname=" + a_Username + "&lgpassword=" + a_Password,true,false);
			if(stream == null){
				log.severe("No stream available in Mediawikibot.login()");
				return false;
			}
			
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList node = doc.getElementsByTagName("api");
			
			node = XMLHandler.getNode(node,"login");
		    if(XMLHandler.getNodeAttribute(node, "result").compareTo("Success") != 0) {
		    	log.severe("Login FAILED. \nNode value:" + XMLHandler.getNodeAttribute(node, "result"));
		    	return false;
		    }
			XMLHandler.setLoginInfo(node);
			
			return true;
		} catch (SAXException e) {
			log.severe("SAXException in MediawikiBot.login().\n Info: " + e.getMessage());
		} catch (IOException e) {
			log.severe("IOException in MediawikiBot.login().\n Info: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			log.severe("ParserConfigurationException in MediawikiBot.login().\n Info: " + e.getMessage());
		}
		return false;
	}
	
	public String getEditToken() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		log.info("obtaining edit token");
		try {
			db = dbf.newDocumentBuilder();
			String postVars = "action=query&prop=info&intoken=edit&titles="+Config.data("xmlfrom");
			log.info("Post vars in MediawikiBot.getEditToken() are " + postVars);
			InputStream stream = XMLHandler.getXML("api.php?result=xml",postVars,false,true);
			if(stream == null){
				log.severe("No stream available in Mediawikibot.login()");
				return "";
			}
			
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList node = doc.getElementsByTagName("api");
			
			node = XMLHandler.getNode(node,"query");
			node = XMLHandler.getNode(node,"pages");
			node = XMLHandler.getNode(node,"page");
			
			edittoken = XMLHandler.getNodeAttribute(node, "edittoken");
			log.info("edittoken = "+edittoken);

		} catch (SAXException e) {
			log.severe("SAXException in getEditToken().\n Info: " + e.getMessage());
		} catch (IOException e) {
			log.severe("IOException in getEditToken().\n Info: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			log.severe("ParserConfigurationException in getEditToken().\n Info: " + e.getMessage());
		}
		return "";
	}
	
	@SuppressWarnings("deprecation")
	/** Create a page on the wiki.
	 * waveId:  current wave ID
	 * summary: an edit summary for on the wiki
	 */
	public void CreatePage(String a_Page, String a_Title, String a_Content, String waveId, String summary) {
		textView.append("Createpage called");
		a_Title = URLEncoder.encode(a_Title);
		a_Page = URLEncoder.encode(a_Page);
		summary= URLEncoder.encode(summary);
		String encoded_waveId=URLEncoder.encode(waveId);

		if (this.edittoken==null)
			throw new NullPointerException("edittoken is not set");
		String edittoken = URLEncoder.encode(this.edittoken);
		a_Content = URLEncoder.encode(a_Content);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		


		try {
			db = dbf.newDocumentBuilder();
			String a_Section = "new";
			if(a_Title.equals(""))
				a_Section = "0";
				
			
			String postxml="action=edit&title=" + a_Page + "&section=" + a_Section + "&text=" + a_Content + "&summary=" + summary + "&token=" + edittoken+"&wave_id="+encoded_waveId;
			textView.append(postxml+"\n");
			InputStream stream = XMLHandler.getXML("api.php?result=xml", postxml, true, true);
			if(stream == null) {
				textView.append("getPostXMLAgain fail");
				return;
			}
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList node = doc.getElementsByTagName("api");
			
			node = XMLHandler.getNode(node, "edit");
		    Node l_Node = node.item(0);
		    if(l_Node == null) {
		    	textView.append("\nNode = null\n");
		    	node= doc.getElementsByTagName("api");
		    	node = XMLHandler.getNode(node, "error");
			    textView.append("Error: " + XMLHandler.getNodeAttribute(node,"code") + "\n");
			    textView.append("Info: " + XMLHandler.getNodeAttribute(node,"info") + "\n");
			    textView.append("Edittoken: " + edittoken + "\n");
		    	return;
		    }
		    
		    l_Node = l_Node.getAttributes().getNamedItem("result");
		    if(l_Node == null) {
		    	textView.append("\nNode = null\n");
		    	node = doc.getElementsByTagName("api");
		    	node = XMLHandler.getNode(node, "error");
			    textView.append("Error: " + XMLHandler.getNodeAttribute(node,"code") + "\n");
		    	return;
		    }
		    
		    if(l_Node.getNodeValue().compareTo("Success") != 0){
		    	textView.append("Node value:" + XMLHandler.getNodeAttribute(node, "result"));
		    	return;
		    }
		} catch (SAXException e) {
			textView.append("\nSAXException"+e);
		} catch (IOException e) {
			textView.append("\nIOException");
		} catch (ParserConfigurationException e) {
			textView.append("\nParserConfigurationException");
		}
	}
}
