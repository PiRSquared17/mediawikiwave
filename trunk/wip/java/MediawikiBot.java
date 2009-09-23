package wikystuff;

import com.google.wave.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MediawikiBot {
	private String m_URL;
	private String a_URL;
	private TextView textView;
	private String lgusername;
	private String lguserid;
	private String lgsessionid;
	private String edittoken;
	private String cookieprefix;
	
	public MediawikiBot (String a_URL, TextView a_TextView) {
		if(!a_URL.endsWith("/"))
			a_URL += "/";
		m_URL = a_URL + "api.php?format=xml";
		textView = a_TextView;
		this.a_URL=a_URL;
	}
	
	public String getURL() { return a_URL; }
	
	public Boolean login(String a_Username, String a_Password) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		
		try {
			db = dbf.newDocumentBuilder();
			InputStream stream = getPostXML("action=login&lgname=" + a_Username + "&lgpassword=" + a_Password);
			if(stream == null)
				return false;
			
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("api");
			
			Node fstNode = nodeLst.item(0);
			Element fstElmnt = (Element) fstNode;
		    
			
		    NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("login");
		    if(fstNmElmntLst.item(0).getAttributes().getNamedItem("result").getNodeValue().compareTo("Success") != 0) {
		    	textView.append("Node value:" + fstNmElmntLst.item(0).getAttributes().getNamedItem("result").getNodeValue());
		    	return false;
		    }
			lgusername = fstNmElmntLst.item(0).getAttributes().getNamedItem("lgusername").getNodeValue();
			lguserid = fstNmElmntLst.item(0).getAttributes().getNamedItem("lguserid").getNodeValue();
			lgsessionid = fstNmElmntLst.item(0).getAttributes().getNamedItem("sessionid").getNodeValue();
			cookieprefix = fstNmElmntLst.item(0).getAttributes().getNamedItem("cookieprefix").getNodeValue();
			
			if(fstNmElmntLst.item(0).getAttributes().getNamedItem("result").getNodeValue() == "Succes") {
				return true;
			} else {
				return false;
			}
		} catch (SAXException e) {
			textView.append("\nSAXException");
		} catch (IOException e) {
			textView.append("\nIOException");
		} catch (ParserConfigurationException e) {
			textView.append("\nParserConfigurationException");
		}
		return false;
	}
	
	private InputStream getPostXML(String a_PostVar) {
		try{
			URL url = new URL(m_URL);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        writer.write(a_PostVar);
	        writer.close();
	        
	        return connection.getInputStream();
		}catch (IOException e) {
			return null;
		}
	}
	
	private InputStream getPostXMLAgain(String a_PostVar) {
		try{
			URL url = new URL(m_URL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestProperty("Cookie",cookieprefix + "UserID=" + lguserid + "; " + cookieprefix + "UserName=" + lgusername + "; " + cookieprefix + "_session=" + lgsessionid);
	        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        writer.write(a_PostVar);
	        writer.close();
	        
	        return connection.getInputStream();
		}catch (IOException e) {
			return null;
		}
	}
	
	private InputStream getXML(String a_GetVar) {
		try{
			URL url = new URL(m_URL + "&" + a_GetVar);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestProperty("Cookie",cookieprefix + "UserID=" + lguserid + "; " + cookieprefix + "UserName=" + lgusername + "; " + cookieprefix + "_session=" + lgsessionid);
	        return connection.getInputStream();
		}catch (IOException e) {
			return null;
		}
	}
	
	public String getEditToken() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Logger log = Logger.getLogger(MediawikiBot.class.getName());
		log.info("obtaining edit token");
		try {
			db = dbf.newDocumentBuilder();
			InputStream stream = getXML("action=query&prop=info&intoken=edit&titles="+Config.data("xmlfrom"));
			if(stream == null)
				return "";
			
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("api");
			
			Node fstNode = nodeLst.item(0);
			Element fstElmnt = (Element) fstNode;
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("query");
			
			fstNode = nodeLst.item(0);
			fstElmnt = (Element) fstNode;
			fstNmElmntLst = fstElmnt.getElementsByTagName("pages");
			
			fstNode = nodeLst.item(0);
			fstElmnt = (Element) fstNode;
			fstNmElmntLst = fstElmnt.getElementsByTagName("page");
			
			edittoken = fstNmElmntLst.item(0).getAttributes().getNamedItem("edittoken").getNodeValue();
			log.info("edittoken = "+edittoken);

		} catch (SAXException e) {
			textView.append("\nSAXException");
		} catch (IOException e) {
			textView.append("\nIOException");
		} catch (ParserConfigurationException e) {
			textView.append("\nParserConfigurationException");
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
			InputStream stream = getPostXMLAgain(postxml);
			if(stream == null) {
				textView.append("getPostXMLAgain fail");
				return;
			}
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("api");
			
			Node fstNode = nodeLst.item(0);
			Element fstElmnt = (Element) fstNode;
		    
		    NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("edit");
		    Node l_Node = fstNmElmntLst.item(0);
		    if(l_Node == null) {
		    	textView.append("\nNode = null\n");
		    	nodeLst = doc.getElementsByTagName("api");
		    	fstNode = nodeLst.item(0);
				fstElmnt = (Element) fstNode;
			    
			    fstNmElmntLst = fstElmnt.getElementsByTagName("error");
			    textView.append("Error: " + fstNmElmntLst.item(0).getAttributes().getNamedItem("code").getNodeValue() + "\n");
			    textView.append("Info: " + fstNmElmntLst.item(0).getAttributes().getNamedItem("info").getNodeValue() + "\n");
			    textView.append("Edittoken: " + edittoken + "\n");
		    	return;
		    }
		    
		    l_Node = l_Node.getAttributes().getNamedItem("result");
		    if(l_Node == null) {
		    	textView.append("\nNode = null\n");
		    	nodeLst = doc.getElementsByTagName("api");
		    	fstNode = nodeLst.item(0);
				fstElmnt = (Element) fstNode;
			    
			    fstNmElmntLst = fstElmnt.getElementsByTagName("error");
			    textView.append("Error: " + fstNmElmntLst.item(0).getAttributes().getNamedItem("code").getNodeValue() + "\n");
		    	return;
		    }
		    
		    if(l_Node.getNodeValue().compareTo("Success") != 0){
		    	textView.append("Node value:" + fstNmElmntLst.item(0).getAttributes().getNamedItem("result").getNodeValue());
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
