package it.alessiomanai.tuaregmode;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class About extends Activity {

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.about);
        
    }
	
}