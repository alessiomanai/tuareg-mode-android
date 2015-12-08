package it.alessiomanai.tuaregmode;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OutputAdapter extends BaseAdapter {
	
	private final static String LOG = "OutputAdapter";
	
	private ArrayList<OutputLine> output_text;
	private Context context;
	
	public OutputAdapter(Context context, ArrayList<OutputLine> output_text2) {
		this.output_text = output_text2;
		this.context = context;
	}

	public int getCount() {
		if(output_text != null)
			return output_text.size();
		else
			return 0;
	}

	public Object getItem(int arg0) {
		if(output_text == null)
			return null;
		else
			return output_text.get(arg0);
	}

	public long getItemId(int arg0) {
		return Long.valueOf(arg0);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
				
		TextView ll = (TextView) convertView;
		if(convertView == null){
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ll = (TextView) vi.inflate(R.layout.output_sublayout, null);
		}
		String output_line = output_text.get(position).getText();
		System.out.println(output_line);
		
        if (output_line != null) {
        	TextView view = (TextView) ll.findViewById(R.id.text);
        	view.setText(Html.fromHtml(output_line));
        } else {
        	Log.e(LOG, String.format("Tried to adapt item %d which is null", position));
        }
        return ll;
	}
}
