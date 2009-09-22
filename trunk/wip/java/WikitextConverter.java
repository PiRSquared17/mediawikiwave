package wikystuff;

import com.google.wave.api.Annotation;
import com.google.wave.api.Range;
import com.google.wave.api.TextView;

public class WikitextConverter {
	public static String convert(TextView a_textView){
		TextView textView = a_textView;
		java.util.List<Annotation> list = textView.getAnnotations();
		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Annotation annotation = list.get(i);
				String name = annotation.getName();
				String value = annotation.getValue();
				if(name.compareTo("style/fontWeight") == 0 && value.compareTo("bold") == 0){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.compareTo("\n") == 0 || test.compareTo("\r") == 0) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "'''");
					textView.insert(range.getStart(), "'''");
				} else if (name.compareTo("style/fontStyle") == 0 && value.compareTo("italic") == 0){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.compareTo("\n") == 0 || test.compareTo("\r") == 0) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "''");
					textView.insert(range.getStart(), "''");
				} else if (name.compareTo("style/textDecoration") == 0){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.compareTo("\n") == 0 || test.compareTo("\r") == 0) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "</span>");
					textView.insert(range.getStart(), "<span style='text-decoration: " + value + "'>");
				} else if (name.compareTo("style/fontFamily") == 0){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.compareTo("\n") == 0 || test.compareTo("\r") == 0) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "</span>");
					textView.insert(range.getStart(), "<span style='font-family: " + value + "'>");
				} else if (name.compareTo("style/color") == 0){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.compareTo("\n") == 0 || test.compareTo("\r") == 0) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "</span>");
					textView.insert(range.getStart(), "<span style='color: " + value + "'>");
				} else if (name.compareTo("style/backgroundColor") == 0){
					Range range = annotation.getRange();
					String test = textView.getText(new Range(range.getEnd() - 1, range.getEnd()));
					int offset = 0;
					if(test.compareTo("\n") == 0 || test.compareTo("\r") == 0) {
						offset = -1;
					}
					textView.insert(range.getEnd() + offset, "</span>");
					textView.insert(range.getStart(), "<span style='background-color: " + value + "'>");
				} else if (name.compareTo("template") == 0){
					Range range = annotation.getRange();
					a_textView.replace(range, value);
				}
			}
		}
		return textView.getText().replace("\n", "\n\n");
	}
}
