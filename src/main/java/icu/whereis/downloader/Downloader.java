package icu.whereis.downloader;

import icu.whereis.common.model.LivePlatform;
import icu.whereis.common.model.LiveTask;

import java.util.Map;

/**
 * 下载器接口
 */
public interface Downloader {
    void startDownload();

    void addTask(LiveTask liveTask);

    void updatePlatform(Map<String, LivePlatform> platformMap);
}
