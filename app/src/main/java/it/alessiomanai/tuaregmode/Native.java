package it.alessiomanai.tuaregmode;

public class Native {

	static {
		System.loadLibrary("ocamltop");
	}

	public static native boolean start(String connectname);
	public static native void chdir(String path);
	
}
