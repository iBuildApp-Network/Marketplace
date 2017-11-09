package com.appbuilder.core;
import android.app.Activity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//import java.io.FileInputStream;
//import java.io.InputStream;
//import org.apache.http.util.ByteArrayBuffer;
//import android.content.Context;
//import android.util.Log;
//import com.android_app.ki.SplashScreen;


public class DownloadHelper extends Thread {
	
	/* Error strings */
	private static final String ERR_NO_ERROR =  "ERR_NO_ERROR";
	private static final String ERR_FILE_EXISTS =  "ERR_FILE_EXISTS";
	private static final String ERR_FILE_DELETE_EXCEPTION =  "ERR_FILE_DELETE_EXCEPTION";
	private static final String ERR_FILE_CREATE_DIR =  "ERR_FILE_CREATE_DIR";
	private static final String ERR_HTTP_REQUEST_FAILED =  "ERR_HTTP_REQUEST_FAILED";
	private static final String ERR_ZIP_STREAM_CREATE =  "ERR_ZIP_STREAM_CREATE";
	private static final String ERR_ZIP_READ_ENTRY =  "ERR_ZIP_READ_ENTRY";
	private static final String ERR_ZIP_IO_ENTRY =  "ERR_ZIP_IO_ENTRY";
	private static final String ERR_FILE_IO =  "ERR_FILE_IO";
	
	private Runnable mRunStarted;
	private Runnable mRunFailed;
	private Runnable mRunSuccess;
	
	private String mErrorString;
	
	private String mSourceUrl;
	private String mDownloadPath;
	private DownloadHelperCallback mCallback;
	private boolean mIsZip;
	private boolean mReplaceFile;
	private Activity mHolder;
	DownloadHelper _this;

    public void setStartedRunnable(Runnable run) {
		mRunStarted = run;
	}
	
	public void setFailedRunnable(Runnable run) {
		mRunFailed = run;
	}
	
	public void setSuccessRunnable(Runnable run) {
		mRunSuccess = run;
	}
	
	public final String getErrorString() {
		return mErrorString;
	}
	
	public DownloadHelper(Activity holder, String sourceUrl, String downloadPath, DownloadHelperCallback callback, boolean isZip, boolean replaceFile) {
		mHolder = holder;
		mSourceUrl = sourceUrl;
		mDownloadPath = downloadPath;
		mCallback = callback;
		mIsZip = isZip;
		mReplaceFile = replaceFile;
		_this = this;
		mErrorString = ERR_NO_ERROR;
	}
	
	public String getSourceUrl() {
		return mSourceUrl;
	}
	
	public String getDownloadPath() {
		return mDownloadPath;
	}
	
	public DownloadHelperCallback getCallback() {
		return mCallback;
	}
	
	public boolean getIsZip() {
		return mIsZip;
	}
	
	public boolean getReplaceFile() {
		return mReplaceFile;
	}
	
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	    	File[] files = path.listFiles();
            assert files != null;
            for(int i=0; i<files.length; i++) {
	    		if(files[i].isDirectory()) {
	    			deleteDirectory(files[i]);
	    		} else {
	    			files[i].delete();
	    		}
	    	}
	    }
	    return( path.delete() );
	}
		
	private void processError(final String err) {
		mErrorString = err;
		if (mCallback != null && mHolder != null) {
			Runnable started = new Runnable() {
				public void run(){
					mCallback.DownloadHelperCallbackFailed(_this, err);
				}
			};
			mHolder.runOnUiThread(started);
		}
				
		if (mRunFailed != null) {
            assert mHolder != null;
            mHolder.runOnUiThread(mRunFailed);
		}
		
		throw new RuntimeException();
	}
	
	@Override
	public void run() {
		if (mCallback != null && mHolder != null) {
			Runnable started = new Runnable() {
				public void run(){
					mCallback.DownloadHelperCallbackStarted(_this);
				}
			};
			mHolder.runOnUiThread(started);
		}
		
		if (mRunStarted != null) {
            assert mHolder != null;
            mHolder.runOnUiThread(mRunStarted);
		}
		
		
		try {
			if (mIsZip) {
				processZipFile();
			} else {
				processRawFile();
			}
		} catch (RuntimeException e) {
			return;
		}
		
		if (mCallback != null && mHolder != null) {
			Runnable started = new Runnable() {
				public void run(){
					mCallback.DownloadHelperCallbackSuccess(_this);
				}
			};
			mHolder.runOnUiThread(started);
		}
		
		if (mRunSuccess != null) {
			mHolder.runOnUiThread(mRunSuccess);
		}
		
	}
	
	private void processRawFile() {
		File outFile = new File(mDownloadPath);
		if (outFile.exists()) {
			if (mReplaceFile) {
				try {
					outFile.delete();
				} catch (SecurityException e) {
					e.printStackTrace();
					processError(DownloadHelper.ERR_FILE_DELETE_EXCEPTION);
				}
			} else {
				processError(DownloadHelper.ERR_FILE_EXISTS);
			}
		}
		
		HttpGet request = new HttpGet(mSourceUrl);
		request.addHeader("Accept", "*/*"); 
		HttpResponse response = null;
		try
		{
			DefaultHttpClient client = new DefaultHttpClient();
			client.getParams().setBooleanParameter("http.protocol.handle-redirects", true);
			response = client.execute(request);
		} catch (IOException e) { } ;
		if( response == null ) {
			processError(ERR_HTTP_REQUEST_FAILED);
			return;
		}
		BufferedInputStream bis = null;
		OutputStream out = null;
		try {
			bis = new BufferedInputStream(response.getEntity().getContent()); 
			out=new FileOutputStream(outFile);
			byte buf[]=new byte[1024];
			int len;
			while((len=bis.read(buf))>0) {
				out.write(buf,0,len);
				out.flush();
			}
		} catch (Exception e) {
			processError(ERR_FILE_IO);
		} finally {
			try {
                assert out != null;
                out.close();
                assert bis != null;
                bis.close();
			} catch (IOException e) { }
		}
	}
	
	private void processZipFile() {
		File outDir = new File(mDownloadPath);
		if (outDir.exists()) {
			if (mReplaceFile) {
				try {
					//outFile.delete();
					DownloadHelper.deleteDirectory(outDir);
				} catch (SecurityException e) {
					e.printStackTrace();
					processError(DownloadHelper.ERR_FILE_DELETE_EXCEPTION);
				}
			} else {
				processError(DownloadHelper.ERR_FILE_EXISTS);
			}
		}
		try {
			outDir.mkdir();
		} catch (SecurityException e){
			e.printStackTrace();
			processError(ERR_FILE_CREATE_DIR);
			return;
		}
		
		HttpGet request = new HttpGet(mSourceUrl);
		request.addHeader("Accept", "*/*"); 
		HttpResponse response = null;
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			client.getParams().setBooleanParameter("http.protocol.handle-redirects", true);
			response = client.execute(request);
		} 
		catch (IOException e) { } ;
		if( response == null ) {
			processError(ERR_HTTP_REQUEST_FAILED);
			return;
		}

		ZipInputStream zip;
		try {
			zip = new ZipInputStream(response.getEntity().getContent());
		} catch( java.io.IOException e ) {
			processError(ERR_ZIP_STREAM_CREATE);
			return;
		}
		
		String path;
		byte[] buf = new byte[1024];
		ZipEntry entry;
		while(true)
		{
			try {
				entry = zip.getNextEntry();
			} catch( java.io.IOException e ) {
				processError(ERR_ZIP_READ_ENTRY);
				return;
			}
			if( entry == null )
				break;                 
			if( entry.isDirectory() ) {
				try {
					String entry_dir = mDownloadPath + entry.getName();
					(new File(entry_dir)).mkdirs();
				} catch( SecurityException e ) { };
				continue;
			}
			
			OutputStream out = null;
			path = mDownloadPath + entry.getName();
			try 
			{
				out = new FileOutputStream( path );
			} catch( FileNotFoundException e ) {
			} catch( SecurityException e ) { };
			if( out == null ) {
				processError(ERR_ZIP_READ_ENTRY);
				return;
			}

			try	{
				int len;
				while ((len = zip.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}
				out.flush();
			} catch( java.io.IOException e ) {
				processError(ERR_ZIP_IO_ENTRY);
				return;
			}
		}
		try {
			zip.close();
		} catch (IOException e) {
			
		}
	}
}
