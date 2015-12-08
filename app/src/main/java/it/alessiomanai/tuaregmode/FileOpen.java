package it.alessiomanai.tuaregmode;

import java.io.File;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileOpen extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fileopen);
		
		final String path = this.getIntent().getExtras().getString("path");
		File file = new File(path);
		final String[] liste = file.list();
		
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.list_item, liste);

		ListView lv = (ListView)findViewById(R.id.listview);
		lv.setTextFilterEnabled(true);
		lv.setAdapter(aa);
	
		lv.setOnItemClickListener(new OnItemClickListener() {
		    
			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		      Intent intent = new Intent(FileOpen.this, MainActivity.class);
		      intent.putExtra("file", path + File.separator + liste[position]);
		      setResult(Activity.RESULT_OK, intent);
		      FileOpen.this.finish();
		    }

		  });
		}
}