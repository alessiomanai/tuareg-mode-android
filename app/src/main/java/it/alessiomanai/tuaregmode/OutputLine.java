package it.alessiomanai.tuaregmode;

public class OutputLine {
	
	public static final String TOPLEVEL_START_TAG = "";
	public static final String TOPLEVEL_END_TAG = "";
	public static final String USER_START_TAG = "<b>";
	public static final String USER_END_TAG = "</b>";
	
	private String text = "";
	private int origin;
	
	public OutputLine(String text, int origin){
		addText(text, origin);
		this.origin = origin;
	}

	public String getText() {
		return text;
	}

	public void addText(String text, int origin) {
		if(origin == MainActivity.FROM_USER){
			this.text = this.text + USER_START_TAG + 
					text.replace("<", "&lt;").replace("\n", "<br />") + 
					USER_END_TAG;
		} else {
			this.text = this.text + TOPLEVEL_START_TAG + 
					text.replace("<", "&lt;").replace("\n", "<br />") + 
					TOPLEVEL_END_TAG;
		}
	}

	public int getOrigin() {
		return origin;
	}

}
