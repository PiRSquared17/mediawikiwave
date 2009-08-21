package wikystuff;

import com.google.wave.api.ProfileServlet;

@SuppressWarnings("serial")
public class WikyProfileServlet extends ProfileServlet {

	@Override
	  public String getRobotAvatarUrl() {
	    return "http://wikystuff.appspot.com/images/wiki.png";
	  }

	  @Override
	  public String getRobotName() {
	    return "Wiky the wikibot";
	  }
	}
