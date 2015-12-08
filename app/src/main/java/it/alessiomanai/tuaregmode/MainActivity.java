package it.alessiomanai.tuaregmode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private static final int MENU_SAVE = 1;
    private static final int MENU_OPEN = 2;
    private static final int MENU_CLEAR = 3;
    private static final int MENU_ABOUT = 4;
    private static final int MENU_QUIT = 5;
    public static final int FROM_TOPLEVEL = 1;
    public static final int FROM_USER = 2;
    private static final int FILE_NAME = 0;
    private static final int OVERWRITE = 1;
    private static final int OPEN_FILE = 2;
    private static final int BACKTHREAD_IND = 0;
    private static final int EDITOR_IND = 1;
    private static final int TAB_IND = 2;
    private static final int OUTPUT_IND = 3;
    private static final int ADAPTER_IND = 4;

    private static final String TAG = "ocaml-android";

    private TabHost tabHost;
    private ListView outView;
    private OutputAdapter outAdapter;
    private ArrayList<OutputLine> output_text; // Static is to avoid
    // painful java stuff on
    // orientation changes
    private StringBuffer output_buffer;
    private PrintWriter out = null;
    private BackThread backThread = null;
    private TextView editor;
    private String fileName = "";
    // private Handler outputUpdater = new Handler();
    private static final Object lock = new Object();

    // private static boolean output_upToDate = true;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();
        TabSpec spec1 = tabHost.newTabSpec("Tab 1");
        spec1.setIndicator("Code Editor",
                getResources().getDrawable(R.drawable.ide_tab));
        spec1.setContent(R.id.tab1);
        TabSpec spec2 = tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("OCaml Toplevel",
                getResources().getDrawable(R.drawable.toplevel_tab));
        spec2.setContent(R.id.console_output);
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.setCurrentTab(1);

        output_buffer = new StringBuffer();

        outView = (ListView) findViewById(R.id.console_output);

        editor = (TextView) findViewById(R.id.editor);
        Button compile_all = (Button) findViewById(R.id.compile_all);
        compile_all.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                if (out != null
                        && !"".equals(editor.getText().toString().trim())) {

                    // Hide the soft Keyboard manually
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(editor.getWindowToken(), 0);

                    tabHost.setCurrentTab(1);
                    String line = editor.getText().toString() + "\n";
                    parseAndPrint(line, FROM_USER);

                    out.println(line);
                    out.flush();
                }
            }

        });

        Object[] tab = (Object[]) getLastNonConfigurationInstance();
        if (tab == null) { // The application has just been launched by the user

            output_text = new ArrayList<OutputLine>();
            outAdapter = new OutputAdapter(this, output_text);
            outView.setAdapter(outAdapter);

            FileInputStream fis = null;
            ;
            InputStreamReader isr = null;
            ;
            BufferedReader br = null;
            StringBuilder content = new StringBuilder();
            try {
                fis = openFileInput("autosave");
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }
                editor.setText(content);
            } catch (FileNotFoundException e) {
                // It's okay, first launch for example. Don't care anyway.
            } catch (IOException e) {
                // Hmm?
                e.printStackTrace();
            }

            tabHost.setCurrentTab(1);

            backThread = new BackThread();
            backThread.execute();
        } else { // The application resumed from an orientation change
            editor.setText((String) tab[EDITOR_IND]);
            tabHost.setCurrentTab((Integer) tab[TAB_IND]);
            output_text = (ArrayList<OutputLine>) tab[OUTPUT_IND];
            backThread = (BackThread) tab[BACKTHREAD_IND];
            outAdapter = (OutputAdapter) tab[ADAPTER_IND];
            out = backThread.getOutput();
            outView.setAdapter(outAdapter);
        }
    }

    public void onPause() {
        super.onPause();
        // outputUpdater.removeCallbacks(outputUpdaterTask);
        FileOutputStream fos = null;
        ;
        try {
            fos = openFileOutput("autosave", Context.MODE_PRIVATE);
            fos.write(editor.getText().toString().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/*
	 * public void onResume(){ super.onResume();
	 * outputUpdater.post(outputUpdaterTask); }
*/

    public Object setRetainInstance() {
        Object[] tab = new Object[5];
        tab[BACKTHREAD_IND] = backThread;
        tab[EDITOR_IND] = editor.getText().toString();
        tab[TAB_IND] = tabHost.getCurrentTab();
        tab[OUTPUT_IND] = output_text;
        tab[ADAPTER_IND] = outAdapter;
        return tab;
    }

    private void clearOutputBuffer() {
        if (output_buffer.length() > 0)
            output_buffer.delete(0, output_buffer.length());
    }

    private void parseAndPrint(String line, int origin) {
        synchronized (lock) {
            clearOutputBuffer();
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\n') {
                    println(output_buffer.toString(), origin, true);
                    clearOutputBuffer();
                } else {
                    output_buffer.append(c);
                }
            }
            if (output_buffer.length() != 0) {
                println(output_buffer.toString(), origin, false);
                clearOutputBuffer();
            }
        }
    }

    private void println(String line, int origin, boolean line_finished) {
        synchronized (lock) {
            OutputLine last_line = null;
            if (!output_text.isEmpty()) {
                last_line = output_text.get(output_text.size() - 1);
            } else {
                last_line = new OutputLine("", origin);
                output_text.add(last_line);
            }
            last_line.addText(line, origin);
            if (line_finished) {
                output_text.add(new OutputLine("", origin));
            }
            outAdapter.notifyDataSetChanged();
        }
    }

    private void clear() {
        Log.d(TAG, "console clear");
        output_text.clear();
        println("# ", FROM_TOPLEVEL, false);
        tabHost.setCurrentTab(1);
        Toast.makeText(MainActivity.this, R.string.clear_toast,
                Toast.LENGTH_SHORT).show();
    }

    private void saveFile(String name, boolean overwrite) {
        String state = Environment.getExternalStorageState();
        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "ocaml";
            new File(path).mkdirs();
            File file = new File(path + File.separator + name);
            if (file.isFile() && !overwrite) {
                Intent intent = new Intent(MainActivity.this,
                        FileOverwrite.class);
                startActivityForResult(intent, OVERWRITE);
            } else {
                try {
                    fOut = new FileOutputStream(file);
                    osw = new OutputStreamWriter(fOut);
                    osw.write(editor.getText().toString());
                    Toast.makeText(MainActivity.this,
                            R.string.file_external_toast, Toast.LENGTH_LONG)
                            .show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, R.string.file_write_fail,
                            Toast.LENGTH_LONG).show();
                } finally {
                    try {
                        osw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "File saved in " + path + ".");
            }
        } else if (Environment.MEDIA_SHARED.equals(state)) {
            Toast.makeText(MainActivity.this, R.string.file_readonly_toast,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, R.string.file_no_sdcard,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openFile(String name) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder content = new StringBuilder();
        try {
            fis = new FileInputStream(name);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, R.string.file_read_fail,
                    Toast.LENGTH_LONG).show();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editor.setText(content);
        Log.d(TAG, "Sources imported from " + name + ".");
    }

    private void menuSave() {
        Intent intent = new Intent(MainActivity.this, FileSave.class);
        intent.putExtra("previous_filename", fileName);
        startActivityForResult(intent, FILE_NAME);
    }

    private void menuOpen() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "ocaml";
            File file = new File(path);
            if (!file.isDirectory() || file.list().length == 0) {
                Toast.makeText(MainActivity.this,
                        R.string.file_directory_not_found, Toast.LENGTH_LONG)
                        .show();
            } else {
                Intent intent = new Intent(MainActivity.this, FileOpen.class);
                intent.putExtra("path", path);
                startActivityForResult(intent, OPEN_FILE);
            }
        } else if (Environment.MEDIA_SHARED.equals(state)) {
            Toast.makeText(MainActivity.this, R.string.file_readonly_toast,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, R.string.file_no_sdcard,
                    Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int result, Intent intent) {
        switch (requestCode) {
            case FILE_NAME:
                if (result == Activity.RESULT_OK) {
                    String name = intent.getExtras().getString("fileName");

                    fileName = name;
                    saveFile(name, false);
                } else {
                    Toast.makeText(MainActivity.this, R.string.file_not_saved,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case OVERWRITE:
                if (result == Activity.RESULT_OK) {
                    saveFile(fileName, true);
                } else {
                    Toast.makeText(MainActivity.this, R.string.file_not_saved,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case OPEN_FILE:
                if (result == Activity.RESULT_OK) {
                    String file = intent.getExtras().getString("file");
                    openFile(file);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_OPEN, Menu.NONE, R.string.menu_open).setIcon(
                R.drawable.ic_menu_archive);
        menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, R.string.menu_save).setIcon(
                R.drawable.ic_menu_save);
        menu.add(Menu.NONE, MENU_CLEAR, Menu.NONE, R.string.menu_clear)
                .setIcon(R.drawable.ic_menu_clear_playlist);
        menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about)
                .setIcon(R.drawable.ic_menu_info_details);
        menu.add(Menu.NONE, MENU_QUIT, Menu.NONE, R.string.menu_quit).setIcon(
                R.drawable.ic_menu_close_clear_cancel);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_OPEN:
                menuOpen();
                return true;
            case MENU_SAVE:
                menuSave();
                ;
                return true;
            case MENU_CLEAR:
                clear();
                return true;
            case MENU_ABOUT:
                Intent intent_about = new Intent(MainActivity.this, About.class);
                startActivity(intent_about);
                return true;
            case MENU_QUIT:
                FileOutputStream fos = null;
                ;
                try {
                    fos = openFileOutput("autosave", Context.MODE_PRIVATE);
                    fos.write(editor.getText().toString().getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	/*
	 * private Runnable outputUpdaterTask = new Runnable() {
	 *
	 * public void run() { // takes the lock, updates the outview, and says the
	 * output is up-to-date synchronized(lock){ if(!output_upToDate){
	 * outView.setText(Html.fromHtml(output_text.toString()));
	 * //outView.postInvalidate(); //scrollView_toplevel.clearAnimation();
	 * //scrollView_toplevel.smoothScrollTo(0, outView.getHeight());
	 * //scrollView_toplevel.requ output_upToDate = true; Log.v(TAG, "bip!"); }
	 * } outputUpdater.postDelayed(this, REFRESH_INTERVAL); }
	 *
	 * };
	 */

    private class BackThread extends AsyncTask<Void, String, Void> {

        private PrintWriter out;
        private InputStream in = null;

        public PrintWriter getOutput() {
            return out;
        }

        @Override
        protected void onPreExecute() {
            try {

                OcamlTop toplevel = null;
                Object[] tab = (Object[]) getLastNonConfigurationInstance();
                if (tab == null) {
                    try {
                        toplevel = new OcamlTop(MainActivity.this);
                    } catch (Exception e) {
                        output_text.clear();
                        println(getString(R.string.toplevel_start_fail),
                                FROM_TOPLEVEL, true);
                    }
                    Log.d(TAG, "New Toplevel created");
                } else {
                    toplevel = (OcamlTop) tab[0];
                    Log.d(TAG, "Old Toplevel found");
                }
                if (toplevel == null || toplevel.stream == null) {
                    println(getString(R.string.toplevel_start_fail),
                            FROM_TOPLEVEL, true);
                    Log.e(TAG, "Unable to start the toplevel");
                } else {
                    in = toplevel.stream.getInputStream();
                    out = new PrintWriter(new OutputStreamWriter(
                            toplevel.stream.getOutputStream(), "utf-8"));
                    MainActivity.this.out = out;
                }

            } catch (IOException e) {
                Log.e(TAG, "error in main", e);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (in == null) {
                runOnUiThread(new Runnable() {

                    public void run() {
                        output_text.clear();
                        println(getString(R.string.toplevel_start_fail),
                                FROM_TOPLEVEL, true);
                    }
                });
            } else {

                try {
                    while (true) {
                        int i = in.read();
                        if (i == -1) {
                            break;
                        }
                        int len = in.available();
                        byte[] b = new byte[len + 1];
                        b[0] = (byte) (i <= 127 ? i : i - 256);
                        in.read(b, 1, len);
                        publishProgress(new String(b, "utf-8"));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Datas has been lost", e);
                    Toast.makeText(MainActivity.this, R.string.data_lost,
                            Toast.LENGTH_LONG).show();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... strings) {
            parseAndPrint(strings[0], FROM_TOPLEVEL);
            Log.v(TAG, strings[0]);
        }
    }

}
