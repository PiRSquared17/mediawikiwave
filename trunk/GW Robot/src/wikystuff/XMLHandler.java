package wikystuff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.wave.api.TextView;

public class XMLHandler {
	static private String m_URL = "";
	static private String lgusername = "";
	static private String lguserid = "";
	static private String lgsessionid = "";
	static private String cookieprefix = "";
	
	static private Logger log = Logger.getLogger(XMLHandler.class.getName());
	
	static private boolean isInit = false;
	
	static public void Init(String a_URL) {
		m_URL = a_URL;
		isInit = true;
	}
	
	static public String getURL() { return m_URL; }
	
	static public InputStream getXML(String a_Page, String a_Var, boolean isPost, boolean useCookie){
		if(!isInit)
			return null;
		
		try{
			String l_Url = m_URL + a_Page;
			if(!a_Var.equals("") && !isPost)
				l_Url += "&" + a_Var;
			
			log.info("URL: " + l_Url);
			
			URL url = new URL(l_Url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			if(useCookie) {
				String cookieString = cookieprefix + "UserID=" + lguserid + ";" + cookieprefix + "UserName=" + lgusername + ";" + cookieprefix + "_session=" + lgsessionid;
				log.info("Cookie string: " + cookieString);
				connection.setRequestProperty("Cookie",cookieString);
			}
	        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	        connection.setDoOutput(true);
	        if(isPost) {
		        connection.setRequestMethod("POST");
		        
		        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		        log.info("Sending POST var: " + a_Var);
		        writer.write(a_Var);
		        writer.close();
	        }
	        
	        return connection.getInputStream();
		}catch (IOException e) {
			return null;
		}
	}
	
	static public NodeList getNode(NodeList a_Node, String a_NodeName) {
		if(!isInit)
			return null;
		
		return ((Element)a_Node.item(0)).getElementsByTagName(a_NodeName);
	}
	
	static public String getNodeAttribute(NodeList a_Node, String a_AttributeName) {
		if(!isInit)
			return "";
		
		Node l_Node = a_Node.item(0).getAttributes().getNamedItem(a_AttributeName);
		if(l_Node == null)
			return "";
		
		return l_Node.getNodeValue();
	}
	
	static public String getNodeContent(NodeList a_Node) {
	    return a_Node.item(0).getChildNodes().item(0).getNodeValue();
	}
	
	static public void setLoginInfo(NodeList node) {
		lgusername = getNodeAttribute(node,"lgusername");
		lguserid = getNodeAttribute(node,"lguserid");
		lgsessionid = getNodeAttribute(node,"sessionid");
		cookieprefix = getNodeAttribute(node,"cookieprefix");
	}
	
	static public void getNodeError(NodeList a_Node, TextView textView) {
		try {
	    	NodeList node = XMLHandler.getNode(a_Node,"Error");
		    textView.append("Error: " + XMLHandler.getNodeAttribute(node,"code") + "\n");
		    textView.append("Info: " + XMLHandler.getNodeAttribute(node,"info") + "\n");
		} catch (NullPointerException e) {
			textView.append("NullPointerException in getNodeError");
		}
	}
}
