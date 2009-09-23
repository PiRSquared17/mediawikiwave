package wikystuff;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Config {
	/* implement simple singleton */
	private static String config_file="settings.xml";
	private static Config instance = null;
	private Map<String,String> config_data=Collections.synchronizedMap(new HashMap<String,String>());

	protected Config() throws Exception {
		readFile();
	}
		
	public static Config getInstance() throws Exception {
		if (instance== null) {
			instance=new Config();
		}
		
		return instance;
	}
	
	private void readFile() throws Exception {
		try {
			File file=new File(this.config_file);
			InputStream stream = new FileInputStream(file);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();
			Document doc = db.parse(stream);
			Element root=doc.getDocumentElement();
			root.normalize();
		
			NodeList nodelist=root.getChildNodes();
			
			for (int i=0; i<nodelist.getLength(); i++) {
				Node n=nodelist.item(i);
				if (n.getNodeType()==Node.ELEMENT_NODE) {
					String key=n.getNodeName();
					NodeList subChildren=n.getChildNodes();
					Node textNode=subChildren.item(0);
					String value=textNode.getNodeValue();
					this.config_data.put(key, value);
				}
			}
		} catch (Exception cause) { throw new Exception("Failed to parse config file "+this.config_file, cause);}
	}

	public String get(String key) {
		return this.config_data.get(key);
	}

	public static String data(String key) {
		try {
			Config instance=Config.getInstance();
		} catch (Exception cause) {throw new RuntimeException("Failed to instantiate Config",cause);}
		return instance.get(key);
	}
}
