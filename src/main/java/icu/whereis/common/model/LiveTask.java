package icu.whereis.common.model;

import icu.whereis.downloader.DownloaderListener;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 直播任务
 */
public class LiveTask {
    private String id;
    private String url;
    private String anchorName;
    private String roomNumber;
    private LivePlatform platform;
    /**
     * 录制中
     */
    private boolean running;
    /**
     * 是否监控
     */
    private boolean monitoring;

    /**
     * 平台ID
     */
    private String platformId;

    /**
     * 地址类型 1直播流 2直播间地址
      */
    private Integer urlType;

    private List<DownloaderListener> listeners;

    private Future<?> future;

    /**
     * 任务启动时间。单位：毫秒
     */
    private long startTimestamp;

    /**
     * 上次直播时间。单位：毫秒
     */
    private long lastRunningTimestamp;

    /**
     * 超过一定时间没开播，冷却一段。下一次执行的时间。单位：毫秒
     */
    private long nextStartstamp;

    /**
     * 运行次数时间戳(套路直播用)
     */
    private long runningCountTimestamp;
    /**
     * 一定时间内运行次数(套路直播用)
     */
    private int runningCount;

    /**
     * 流过期时间
     */
    private long streamExpireTime;

    /**
     * 外界中断通知停止的标记
     */
    private boolean nodifyStop = false;

    /**
     * 是否已经下载过？仅对非优先主播有效
     */
    private boolean downloaded = false;

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isNodifyStop() {
        return nodifyStop;
    }

    public void setNodifyStop(boolean nodifyStop) {
        this.nodifyStop = nodifyStop;
    }

    public long getStreamExpireTime() {
        return streamExpireTime;
    }

    public void setStreamExpireTime(long streamExpireTime) {
        this.streamExpireTime = streamExpireTime;
    }

    public long getRunningCountTimestamp() {
        return runningCountTimestamp;
    }

    public void setRunningCountTimestamp(long runningCountTimestamp) {
        this.runningCountTimestamp = runningCountTimestamp;
    }

    public int getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(int runningCount) {
        this.runningCount = runningCount;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAnchorName() {
        return anchorName;
    }

    public void setAnchorName(String anchorName) {
        this.anchorName = anchorName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LivePlatform getPlatform() {
        return platform;
    }

    public void setPlatform(LivePlatform platform) {
        this.platform = platform;
    }

    public Integer getUrlType() {
        return urlType;
    }

    public void setUrlType(Integer urlType) {
        this.urlType = urlType;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DownloaderListener> getListeners() {
        return listeners;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getLastRunningTimestamp() {
        return lastRunningTimestamp;
    }

    public void setLastRunningTimestamp(long lastRunningTimestamp) {
        this.lastRunningTimestamp = lastRunningTimestamp;
    }

    public long getNextStartstamp() {
        return nextStartstamp;
    }

    public void setNextStartstamp(long nextStartstamp) {
        this.nextStartstamp = nextStartstamp;
    }

    public LiveTask addListener(DownloaderListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LiveTask removeListener(DownloaderListener listener) {
        this.listeners.remove(listener);
        return this;
    }
}
