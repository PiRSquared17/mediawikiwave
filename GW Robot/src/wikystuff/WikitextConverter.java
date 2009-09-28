package wikystuff;

import com.google.wave.api.Annotation;
import com.google.wave.api.Range;
import com.google.wave.api.TextView;
import java.util.List;

public class WikitextConverter {
	public static String convert(TextView textView){
		List<Annotation> list = textView.getAnnotations();
		
		return convert(textView,list);
	}
	
	public static String convert(TextView textView, List<Annotation> list) {
		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Annotation annotation = list.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				if(name.equals("style/fontWeight") && value.equals("bold")){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.equals("\n") || test.equals("\r")) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "'''");
					textView.insert(range.getStart(), "'''");
				} else if (name.equals("style/fontStyle") && value.equals("italic")){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.equals("\n") || test.equals("\r")) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "''");
					textView.insert(range.getStart(), "''");
				} else if (name.equals("style/textDecoration")){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.equals("\n") || test.equals("\r")) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "</span>");
					textView.insert(range.getStart(), "<span style='text-decoration: " + value + "'>");
				} else if (name.equals("style/fontFamily")){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.equals("\n") || test.equals("\r")) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "</span>");
					textView.insert(range.getStart(), "<span style='font-family: " + value + "'>");
				} else if (name.equals("style/color")){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.equals("\n") || test.equals("\r")) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "</span>");
					textView.insert(range.getStart(), "<span style='color: " + value + "'>");
				} else if (name.equals("style/backgroundColor")){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.equals("\n") || test.equals("\r")) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "</span>");
					textView.insert(range.getStart(), "<span style='background-color: " + value + "'>");
				} else if (name.equals("template")){
					Range range = annotation.getRange();
					textView.replace(range, value);
				} else if (name.equals("link")){
					Range range = annotation.getRange();
					textView.replace(range, value);
				} else if (name.equals("style-text")){
					Range range = annotation.getRange();
					String identifier = "";
					
					if(value.startsWith("HEADING" )){
						for(int j = 1; j <= 4; j++) {
							identifier += "=";
							if(value.equals("HEADING" + j)) {
								String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
								int offset = 0;
								if(test.equals("\n") || test.equals("\r")) {
									offset = -1;
								}
								textView.insert(range.getEnd() + offset, identifier);
								textView.insert(range.getStart(), identifier);
							}
						}
					} else if(value.equals("BULLETED")) {
						boolean addBrakeAtEnd = false;
						
						String content = textView.getText(range);
						while(content.endsWith("\n") || content.endsWith("\r")) {
							content = content.substring(0, content.lastIndexOf("\n"));
							addBrakeAtEnd = true;
						}
						content = content.replace("\n", "\n*");
						if(addBrakeAtEnd)
							content += "\n";
						
						textView.replace(range, content);
					} else if(value.startsWith("INDENT")) {
						String content = textView.getText(range);
						String identChar = ":";
						List<Annotation> tempList = textView.getStyles(range);
						for(int j = 0; j < tempList.size(); j++) {
							if(tempList.get(j).getValue().equals("BULLETED"))
								identChar = "*";
						}
						
						for(int j = 1; j <= 4; j++) {
							identifier += identChar;
							if(value.equals("INDENT" + j)) {
								boolean addBrakeAtEnd = false;
								
								while(content.endsWith("\n") || content.endsWith("\r")) {
									content = content.substring(0, content.lastIndexOf("\n"));
									addBrakeAtEnd = true;
								}
								content = content.replace("\n", "\n" + identifier);
								if(addBrakeAtEnd)
									content += "\n";
								
								textView.replace(range, content);
							}
						}
					}
				}
			}
		}
		
		String text = textView.getText();
		text = text.replace("\n*","\\n*");
		text = text.replace("\n:","\\n:");
		text = text.replace("\n#","\\n#");
		
		text = text.replace("\n", "\n\n");
		text = text.replace("\\n", "" +
				"\n");
		while(text.startsWith("\n"))
			text.replaceFirst("\n","");
		
		return text;
	}
}
