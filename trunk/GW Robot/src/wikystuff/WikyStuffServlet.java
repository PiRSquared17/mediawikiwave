package wikystuff;

import java.util.List;
import java.util.logging.Logger;
import com.google.wave.api.*;

@SuppressWarnings("serial")
public class WikyStuffServlet extends AbstractRobotServlet {
	
	private boolean debug = true;
	private boolean isWikiText = false;
	private Logger log;

	public WikyStuffServlet() {
		this.log=Logger.getLogger(WikyStuffServlet.class.getName());
	}
	
	public void processEvents(RobotMessageBundle bundle) {
		long startTime = System.currentTimeMillis();
		WaveToWikitextConverter toWikitextConverter = new WaveToWikitextConverter();
		this.log.info("Event Received!");
		
		Wavelet wavelet = bundle.getWavelet();
		
		for (Event e: bundle.getEvents()) {
			if(e.getType() == EventType.WAVELET_SELF_ADDED) {
				addForm(wavelet.appendBlip());
			}

			if(e.getType() == EventType.BLIP_SUBMITTED || e.getType() == EventType.BLIP_VERSION_CHANGED || e.getType() == EventType.WAVELET_VERSION_CHANGED) {
				Blip blip = wavelet.appendBlip();
				TextView textView = blip.getDocument();
				
				MediawikiBot bot = new MediawikiBot(Config.data("wiki"),textView);//FIX ME
				if(!bot.login(Config.data("user"), Config.data("password"))) {//FIX ME
					log.severe("Login error!");
					return;
				}
				if(bot.getEditToken().compareTo("") == 0) {
					log.severe("No edit token retrieved!");
					return;
				}

				/* Create an edit summary crediting all participants */
				List<String> participants=wavelet.getParticipants();
				String summary="From Wave. Participants: ";
				for (String participant : participants) {
					summary=summary+participant+"; ";
				}

				/* Get document text and page title */
				TextView rootTextView=safeTextView(wavelet.getRootBlip());
				if (rootTextView==null)
					continue;
				String text=rootTextView.getText();
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
					String finalText = "";
					if(isWikiText) {
						finalText = wavelet.getRootBlip().getDocument().getText();
					} else {
						finalText = toWikitextConverter.convert(wavelet.getRootBlip().getDocument());
					}
					bot.CreatePage(pageTitle , "", finalText,wavelet.getWaveId(), summary);
				} else {
					textView.append("Couldn't figure a title, so sorry, no upload.\n");
				}

				if(!debug) {
					try {
						blip.delete();
					} catch (NullPointerException e2){}
				}
			}
			
			if(e.getType() == EventType.FORM_BUTTON_CLICKED) {
				TextView rootTextView=safeTextView(wavelet.getRootBlip());
				if (rootTextView!=null) {
					if(!isWikiText) {
						toWikitextConverter.convert(rootTextView);
					} else {
						WikitextToWaveConverter toWaveConverter = new WikitextToWaveConverter();
						toWaveConverter.convert(rootTextView);
					}
					//rootTextView.append("\n\nHello World");
					isWikiText = !isWikiText;
					
				}
			}
		}

		
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		
		this.log.info("Time spent: " + executionTime + "ms");
	}
	
	public void addForm(Blip blip) {
		TextView textView=safeTextView(blip);
		this.log("Adding form?\n");
		if (textView!=null) {
			this.log("Adding form!\n");
			FormElement button=new FormElement(ElementType.BUTTON,"WYSIorWIKI");
			button.setLabel("switch");
			button.setValue("Switch");
			textView.appendElement(button);

		}
		
		/*
		textView.addElement(new FormElement(ElementType.RADIO_BUTTON_GROUP, "WYSIorWIKI"));
		
		wysi=new FormElement(ElementType.RADIO_BUTTON,"WYSIorWIKI");
		wysi.addLabel("WYSIWYG");
		textView.addElement(wysi);		

		wiki=new FormElement(ElementType.RADIO_BUTTON,"WYSIorWIKI");
		wiki.addLabel("WikiText");
		textView.addElement(wiki);	
		*/

	}


	/** Workaround.  BlipImpl.isDocumentAvailable can break with a NullPointerException if data is not initialized for some reason.
	// (see issue 200 - http://code.google.com/p/google-wave-resources/issues/detail?id=200&start=100 )
	 documentAvailable is true if rootBlip.isDocumentAvailable is true. false in all other cases.
	UTILITY*/
	public static TextView safeTextView(Blip blip) {
		boolean documentAvailable=false;
		TextView textView=null;
		try {
			documentAvailable=blip.isDocumentAvailable();
		} catch (NullPointerException ignored)  {}

		if (documentAvailable) {
			textView=blip.getDocument();
			
		}
		return textView;
	}
	
	
	/** given an array of String-s, concatenate all the strings
	 * (perl, php and python call this a "join", the opposite of split). 
	UTILITY.
	*/
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
