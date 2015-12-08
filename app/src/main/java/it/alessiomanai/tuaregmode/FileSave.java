package it.alessiomanai.tuaregmode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FileSave extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.filesave);
		
		String previous_filename = this.getIntent().getExtras().getString("previous_filename");
		
		Button button_save = (Button) findViewById(R.id.button_save);
		Button button_cancel = (Button) findViewById(R.id.button_cancel);
		
		final EditText fileName = (EditText) findViewById(R.id.filename);
		
		fileName.setText(previous_filename);
		fileName.selectAll();
		
		button_save.setOnClickListener(
				new OnClickListener() {

					public void onClick(View arg0) {
						String result;
						if(!(result = fileName.getText().toString()).equals("")){
							Intent intent = new Intent(FileSave.this, MainActivity.class);
							intent.putExtra("fileName", result);
							setResult(Activity.RESULT_OK, intent);
							FileSave.this.finish();
						}
					}
				});
				
	
		button_cancel.setOnClickListener(
			new OnClickListener() {

				public void onClick(View arg0) {
					setResult(Activity.RESULT_CANCELED);
					FileSave.this.finish();
				}
			});
			
	}
}
