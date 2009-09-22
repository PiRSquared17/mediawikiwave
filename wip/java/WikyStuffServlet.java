package wikystuff;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
				
				textView.append("I'm alive!\n");
				
				MediawikiBot bot = new MediawikiBot(REPLACEME.fromwiki,textView);
				bot.login(REPLACEME.user, REPLACEME.password);
				bot.getEditToken();
				
				String text=wavelet.getRootBlip().getDocument().getText();
				String[] lines=text.split("\n");

				String page = wavelet.getTitle(); 

				textView.append("title1 = -"+page+"-\n");
				textView.append("I'm still alive5!\n");
				textView.append("Wave ID: "+wavelet.getWaveId()+"\n");
				textView.append("Wavelet ID: "+wavelet.getWaveletId()+"\n");
				List<String> participants=wavelet.getParticipants();
				String summary="From Wave. participants: ";
				for (String participant : participants) {
					summary=summary+participant+"; ";
				}

				if (page.equals("") && lines.length>0) {
					page=lines[0];
					textView.append("title2 = -"+page+"-\n");
				}
				if (!page.equals("")) {
					bot.CreatePage(page , "", WikitextConverter.convert(wavelet.getRootBlip().getDocument()),wavelet.getWaveId(), summary);
					textView.append("I'm done! Text append to " + bot.getURL() + "index.php/" + page + "\n");
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
		// This probably isn't the best of solutions, but what's a man to do?
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
	
	/** given an array of String-s, concatenate all the strings*/
	public static String concatStrings(String[] array) {
		String total="";
		for (String partial : array) {
			total=total.concat(partial);
		}
		return total;

	}
}
