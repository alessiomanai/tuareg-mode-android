package it.alessiomanai.tuaregmode;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FileOverwrite extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fileoverwrite);
		
		Button button_yes = (Button) findViewById(R.id.button_yes);
		Button button_no = (Button) findViewById(R.id.button_no);
		
		button_yes.setOnClickListener(
				new OnClickListener() {

					public void onClick(View arg0) {
							setResult(Activity.RESULT_OK);
							FileOverwrite.this.finish();
						}
				});
				
		button_no.setOnClickListener(
				new OnClickListener() {

					public void onClick(View arg0) {
							setResult(Activity.RESULT_CANCELED);
							FileOverwrite.this.finish();
						}
				});
	}
}
