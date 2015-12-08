package it.alessiomanai.tuaregmode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

public class OcamlTop {

	public static final String TAG = "ocaml-android";
	public static final String ASSETS_STDLIBPATH = "ocaml-stdlib";
	public static final String STDSTREAM_NAME = "jp.co.itpl.ocamltop";

	private void prepareStdlib(Context context) throws Exception {
		File cache = context.getCacheDir();
		AssetManager assets = context.getAssets();
		try {
			String[] files = assets.list(ASSETS_STDLIBPATH);
			Log.d(TAG, "copying " + files.length + " files of stdlib...");
			for (String file : files) {
				InputStream in = null;
				OutputStream out = null;
				try {
					in = assets.open(ASSETS_STDLIBPATH + File.separator + file);
					String outfile = cache.getAbsolutePath() + File.separator + file;
					out = new FileOutputStream(outfile);
					IOUtils.copy(in, out);
				} finally {
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
			}
			Log.d(TAG, "copy complete.");
		} catch (IOException e) {
			Log.e(TAG, "stdlib copy error", e);
			throw(new Exception("Stdlib copy error. The program will be killed."));
		}
	}

	public final LocalSocket stream;

	public OcamlTop(final Context context) throws Exception {
		try {
			prepareStdlib(context);
		} catch (Exception e) {
			throw(e);
		}
		Native.chdir(context.getCacheDir().getAbsolutePath());

		Log.i(TAG, "connecting std stream");
		LocalServerSocket listenSock = new LocalServerSocket(STDSTREAM_NAME);
		new Thread() {
			public void run() {
				Native.start(STDSTREAM_NAME);
				Log.i(TAG, "ocaml thread started");
			}
		}.start();
		stream = listenSock.accept();
		Log.i(TAG, "connected std stream");
	}
}
