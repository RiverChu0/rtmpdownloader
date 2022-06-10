package icu.whereis.downloader;

public interface DownloaderListener {

    void onStart(String fname);

    void onUpdate(int bytes, int totalDownloaded);

    void onComplete(String fname);

}
