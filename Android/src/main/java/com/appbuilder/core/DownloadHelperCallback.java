package com.appbuilder.core;
import com.appbuilder.core.DownloadHelper;

public interface DownloadHelperCallback {
	public void DownloadHelperCallbackStarted(DownloadHelper obj);
	public void DownloadHelperCallbackSuccess(DownloadHelper obj);
	public void DownloadHelperCallbackFailed(DownloadHelper obj, String errorString);
}
