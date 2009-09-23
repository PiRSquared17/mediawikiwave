package wikystuff;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import com.google.wave.api.*;

@SuppressWarnings("serial")
public class WikyStuffServlet extends AbstractRobotServlet {
	
	private boolean debug=true;
	
	public void WikystuffServlet() {
		debug = true;
	}
	
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();
          
		for (Event e: bundle.getEvents()) {
			if(e.getType() == EventType.BLIP_SUBMITTED || e.getType() == EventType.BLIP_VERSION_CHANGED || e.getType() == EventType.WAVELET_VERSION_CHANGED) {
				Blip blip = wavelet.appendBlip();
				TextView textView = blip.getDocument();
				
				
				Logger log = Logger.getLogger(MediawikiBot.class.getName());

				MediawikiBot bot = new MediawikiBot(Config.data("wiki"),textView);
				bot.login(Config.data("user"), Config.data("password"));
				bot.getEditToken();

				/* Create an edit summary crediting all participants */
				List<String> participants=wavelet.getParticipants();
				String summary="From Wave. Participants: ";
				for (String participant : participants) {
					summary=summary+participant+"; ";
				}

				/* Get document text and page title */
				String text=wavelet.getRootBlip().getDocument().getText();
				String[] lines=text.split("\n");

				String pageTitle = wavelet.getTitle(); 

				/* Workaround. if getTitle is (still) broken, 
				 * use first line as title*/	
				//String pageContent="";
				if (pageTitle.equals("") && lines.length>0) {
					pageTitle=lines[0];
					//pageContent=join(lines,1);
				} else {
					//pageContent=text;
				}
				
				/* And submit to wiki. */
				if (!pageTitle.equals("")) {
					bot.CreatePage(pageTitle , "", WikitextConverter.convert(wavelet.getRootBlip().getDocument()),wavelet.getWaveId(), summary);
				} else {
					textView.append("Couldn't figure a title, so sorry, no upload.\n");
				}

				if(!debug) {
					try {
						blip.delete();
					} catch (NullPointerException e2){}
				}
			}
		}

		Blip rootBlip=wavelet.getRootBlip();
		
		// Workaround.  BlipImpl.isDocumentAvailable can break with a NullPointerException if data is not initialized for some reason.
		// (see issue 200 - http://code.google.com/p/google-wave-resources/issues/detail?id=200&start=100 )
		// documentAvailable is true if rootBlip.isDocumentAvailable is true. false in all other cases
		boolean documentAvailable=false;
		try {
			documentAvailable=rootBlip.isDocumentAvailable();
		} catch (NullPointerException ignored)  {}

		if (documentAvailable) {
			TextView rootTextView=rootBlip.getDocument();
			WikitextToWaveConverter.convert(rootTextView);
		}
	}
	
	/** given an array of String-s, concatenate all the strings
	 * (perl, php and python call this a "join", the opposite of split) */
	public static String join(String[] lines, int firstIndex) {
		String total="";
		for (int i=firstIndex; i<lines.length;i++) {
			String line=lines[i];
			total=total.concat(line);
			total=total.concat("\n");
		}
		return total;
	}
}
