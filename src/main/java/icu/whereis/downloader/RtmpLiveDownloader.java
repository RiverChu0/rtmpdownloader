package icu.whereis.downloader;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import icu.whereis.common.model.LivePlatform;
import icu.whereis.common.model.LiveTask;
import icu.whereis.common.utils.LiveTaskUtils;
import icu.whereis.common.utils.StringKit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bytedeco.ffmpeg.ffmpeg;
import org.bytedeco.javacpp.Loader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RtmpLiveDownloader implements Downloader {
    private static RtmpLiveDownloader downloader;

    private int corePoolSize = 51;

    private ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(corePoolSize);
    private ScheduledThreadPoolExecutor executorService2 = new ScheduledThreadPoolExecutor(3);

    private List<LiveTask> liveTasks = new ArrayList<>();

    private static Logger logger = LogManager.getLogger(RtmpLiveDownloader.class);

    private String platformName;
    private String platformStr;
    private String platformId;

    private boolean isStart;

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public RtmpLiveDownloader(String platformId, String platformName, String platformStr) {
        this.platformId = platformId;
        this.platformName = platformName;
        this.platformStr = platformStr;
    }

    @Override
    public void startDownload() {
        logger.warn(platformName+"直播下载任务启动...");
        executorService.setRemoveOnCancelPolicy(true);
        executorService2.setRemoveOnCancelPolicy(true);

        executorService2.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (liveTasks.size() > 0) {
                    String currDir = System.getProperty("user.dir");

                    String filename = platformStr + "_running_livetask.json";
                    File file = new File(currDir + File.separator + "config" + File.separator + filename);

                    String jsonStr = JSONObject.toJSONString(liveTasks, SerializerFeature.PrettyFormat);

                    logger.warn(platformName + "转存正在下载的直播任务到文件...");
                    try {
                        FileUtils.writeStringToFile(file, jsonStr, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 1, 60, TimeUnit.SECONDS);

        setStart(true);
    }

    @Override
    public void addTask(LiveTask liveTask) {
        liveTasks.add(liveTask);
    }

    public void addTask(String url, String anchorName, String roomNumber) {
        LiveTask liveTask = LiveTaskUtils.addLiveTask(url, anchorName, roomNumber, platformId, true, 1);
        if (liveTask.isRunning()) {
            logger.warn("<" + liveTask.getAnchorName() + ">已在下载，跳过...");
        } else {
            addTask(liveTask);
            startLiveTask(liveTask);
            logger.warn(platformName + "<" + liveTask.getAnchorName() + ">已提交下载。。。");
        }
    }

    @Override
    public void updatePlatform(Map<String, LivePlatform> platformMap) {

    }

    public void startLiveTask(LiveTask liveTask) {
        LiveDownloaderTask hdt = new LiveDownloaderTask(liveTask);
        liveTask.setStartTimestamp(System.currentTimeMillis());
        Future<?> future = executorService.scheduleAtFixedRate(hdt, 1, 25, TimeUnit.SECONDS);
        liveTask.setFuture(future);
    }

    public void removeTask(LiveTask liveTask) {
        boolean s = liveTasks.remove(liveTask);
        String str = s ? "成功" : "失败";
        logger.warn(platformName+"<"+liveTask.getAnchorName()+" - "+liveTask.getRoomNumber()+">移除"+str+"!");
    }

    protected class LiveDownloaderTask implements Runnable {

        private LiveTask liveTask;

        public LiveDownloaderTask(LiveTask liveTask) {
            this.liveTask = liveTask;
        }

        @Override
        public void run() {
            try {
                if (liveTask != null) {
                    String url = liveTask.getUrl();
                    String currentDir = System.getProperty("user.dir") + File.separator;
                    String timestamp2 = StringKit.getTimestamp("yyyyMMdd");
                    String filename = liveTask.getRoomNumber() + "_" + liveTask.getAnchorName() + "_" + StringKit.getTimestamp("yyyyMMddHHmmss")+".flv";
                    String filePath = currentDir + "半糖" + File.separator + timestamp2 + File.separator + filename;
                    File file = new File(filePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    String ffmpeg = Loader.load(ffmpeg.class);
                    System.out.println(ffmpeg);
                    ProcessBuilder builder = new ProcessBuilder(ffmpeg,
                            "-i", url,
                            "-codec", "copy",
                            "-f", "flv",
                            "-n", filePath);

                    Process process = builder.inheritIO().start();
                    liveTask.setRunning(true);
                    process.waitFor();

                    logger.info(platformName+"<"+liveTask.getAnchorName()+">："+liveTask.getUrl()+"无数据返回，移除任务");
                    StringKit.showMessage(platformName+"<"+liveTask.getAnchorName()+"_"+liveTask.getRoomNumber()+">下载完毕！");
                    liveTask.setRunning(false);
                    removeTask(liveTask);
                    Future<?> future = liveTask.getFuture();
                    if (future != null) {
                        future.cancel(true);
                    }
                } else {
                    logger.warn(platformName + "liveTask对象为空，任务不执行");
                }
            } catch (Throwable e) {
                logger.error("发生了异常：", e);
            }
        }

        /**
         * 此方法会占用较高CPU
         */
        private void run2() {
            try {
                if (liveTask != null) {
                    String url = liveTask.getUrl();
                    String currentDir = System.getProperty("user.dir") + File.separator;
                    String timestamp2 = StringKit.getTimestamp("yyyyMMdd");
                    String filename = liveTask.getRoomNumber() + "_" + liveTask.getAnchorName() + "_" + StringKit.getTimestamp("yyyyMMddHHmmss")+".flv";
                    String filePath = currentDir + "半糖" + File.separator + timestamp2 + File.separator + filename;
                    File file = new File(filePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    MediaVideoTransfer mediaVideoTransfer = new MediaVideoTransfer(url, file);
                    mediaVideoTransfer.setLiveTask(liveTask);
                    mediaVideoTransfer.live();

                } else {
                    logger.warn(platformName + "liveTask对象为空，任务不执行");
                }
            } catch (Throwable e) {
                logger.error("发生了异常：", e);
            }
        }

        /**
         * 获取当前录制主播的总数
         *
         * @return
         */
        private int getRunningCount() {
            int x = 0;
            for (int i = 0; i < liveTasks.size(); i++) {
                LiveTask liveTask = liveTasks.get(i);
                boolean running = liveTask.isRunning();
                if (running) {
                    x++;
                }
            }
            return x;
        }

    }

    public static RtmpLiveDownloader getDownloader() {
        synchronized (RtmpLiveDownloader.class) {
            if (downloader == null) {
                downloader = new RtmpLiveDownloader("4", "半糖", "bantang");
            }
        }
        return downloader;
    }

}


