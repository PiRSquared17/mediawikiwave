package wikystuff;

import java.util.HashMap;
import java.util.Map;

import com.google.wave.api.*;

@SuppressWarnings("serial")
public class WikyStuffServlet extends AbstractRobotServlet {
	
	private Map<String,String> pages;
	private boolean debug;
	
	public void WikystuffServlet() {
		debug = false;
		
		pages = new HashMap<String, String>();
		
		pages.put("wavesandbox.com!w+E7oScQSe%D", "User:TheDevilOnLine");
		pages.put("wavesandbox.com!w+GK0H-KpP%C", "Google_Wave");
		pages.put("wavesandbox.com!w+GK0H-KpP%A", "Elephants");
	}
	
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();
          
		for (Event e: bundle.getEvents()) {
			if(e.getType() == EventType.BLIP_SUBMITTED || e.getType() == EventType.BLIP_VERSION_CHANGED || e.getType() == EventType.WAVELET_VERSION_CHANGED) {
				Blip blip = wavelet.appendBlip();
				TextView textView = blip.getDocument();
				
				textView.append("I'm alive!\n");
				
				MediawikiBot bot = new MediawikiBot("http://dev.12wiki.eu/",textView);
				bot.login("TheDevilOnLine", "bluemoon185");
				bot.getEditToken();
				
				String page = "User:TheDevilOnLine"; //pages.get(wavelet.getWaveId());
				
				String waveid = wavelet.getWaveId();
				
				if(waveid.equals("wavesandbox.com!w+E7oScQSe%D"))
					page = "User:TheDevilOnLine";
				else if(waveid.equals("wavesandbox.com!w+GK0H-KpP%A"))
					page = "Elephants";
				else if(waveid.equals("wavesandbox.com!w+GK0H-KpP%C"))
					page = "Google_Wave";
				
				bot.CreatePage(page , "", WikitextConverter.convert(wavelet.getRootBlip().getDocument()));
				textView.append("I'm done! Text append to " + bot.getURL() + "index.php/" + page + "\n");
				if(!debug)
					try {
						blip.delete();
					} catch (NullPointerException e2){}
			}
		}
		
		WikitextToWaveConverter.convert(wavelet.getRootBlip().getDocument());
	}
}