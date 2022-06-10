package icu.whereis.downloader;

import icu.whereis.common.model.LiveTask;
import icu.whereis.common.utils.StringKit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bytedeco.javacv.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

public class MediaVideoTransfer {
    private Logger logger = LogManager.getLogger(MediaVideoTransfer.class);

    private static final String VIDEO_FORMAT = "flv";

    private String outputFilePath;

    private OutputStream outputStream;

    private String rtmpUrl;
    private LiveTask liveTask;

    private String rtmpTransportType = "tcp";

    private FFmpegFrameGrabber grabber;

    private FFmpegFrameRecorder recorder;

    private boolean isStart = false;

    public LiveTask getLiveTask() {
        return liveTask;
    }

    public void setLiveTask(LiveTask liveTask) {
        this.liveTask = liveTask;
    }

    public MediaVideoTransfer(String rtmpUrl, OutputStream outputStream) {
        this.rtmpUrl = rtmpUrl;
        this.outputStream = outputStream;
    }

    public MediaVideoTransfer(String rtmpUrl, File outputFile) {
        this.rtmpUrl = rtmpUrl;
        try {
            this.outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            logger.error("文件不存在！", e);
        }
    }

    public MediaVideoTransfer(String rtmpUrl, String outputFilePath) {
        this(rtmpUrl, new File(outputFilePath));
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public String getRtmpUrl() {
        return rtmpUrl;
    }

    public void setRtmpUrl(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
    }

    public String getRtmpTransportType() {
        return rtmpTransportType;
    }

    public void setRtmpTransportType(String rtmpTransportType) {
        this.rtmpTransportType = rtmpTransportType;
    }

    public FFmpegFrameGrabber getGrabber() {
        return grabber;
    }

    public void setGrabber(FFmpegFrameGrabber grabber) {
        this.grabber = grabber;
    }

    public FFmpegFrameRecorder getRecorder() {
        return recorder;
    }

    public void setRecorder(FFmpegFrameRecorder recorder) {
        this.recorder = recorder;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    /**
     * 开启获取rtsp流
     */
    public void live() {
        logger.info("连接rtmp：" + rtmpUrl + ",开始创建grabber");
        boolean isSuccess = createGrabber(rtmpUrl);
        if (isSuccess) {
            logger.info("创建grabber成功");
        } else {
            logger.info("创建grabber失败");
        }
        startCameraPush();
    }

    /**
     * 构造视频抓取器
     *
     * @param rtmp 拉流地址
     * @return 创建成功与否
     */
    private boolean createGrabber(String rtmp) {
        // 获取视频源
        try {
            grabber = FFmpegFrameGrabber.createDefault(rtmp);
            grabber.setOption("rtmp_transport", rtmpTransportType);
            grabber.start();
            isStart = true;

            recorder = new FFmpegFrameRecorder(outputStream, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
            //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setVideoCodec(grabber.getVideoCodec());
            recorder.setAudioCodec(grabber.getAudioCodec());
            recorder.setFormat(VIDEO_FORMAT);

            //设置视频码率，否则会出现马赛克画质
            recorder.setVideoBitrate(grabber.getVideoBitrate());
            // 在此处设置音频码率会报错
            //recorder.setAudioBitrate(grabber.getAudioBitrate());
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setSampleRate(grabber.getSampleRate());
            return true;
        } catch (FrameGrabber.Exception e) {
            logger.error("创建解析rtsp FFmpegFrameGrabber 失败");
            logger.error("create rtsp FFmpegFrameGrabber exception: ", e);
            stop();
            reset();
            return false;
        }
    }

    /**
     * 推送图片（摄像机直播）
     */
    private void startCameraPush() {
        if (grabber == null) {
            logger.info("重试连接rtmp：" + rtmpUrl + ",开始创建grabber");
            boolean isSuccess = createGrabber(rtmpUrl);
            if (isSuccess) {
                logger.info("创建grabber成功");
            } else {
                logger.info("创建grabber失败");
            }
        }
        try {
            if (grabber != null) {
                liveTask.setRunning(true);
                recorder.start();
                recorder.setAudioBitrate(grabber.getAudioBitrate());
                Frame frame;
                while (isStart && (frame = grabber.grabFrame()) != null) {
                    recorder.setTimestamp(grabber.getTimestamp());
                    recorder.record(frame);
                }

                logger.info("<"+liveTask.getAnchorName()+">："+liveTask.getUrl()+"无数据返回，移除任务");
                StringKit.showMessage("<"+liveTask.getAnchorName()+"_"+liveTask.getRoomNumber()+">下载完毕！");
                liveTask.setRunning(false);
                RtmpLiveDownloader.getDownloader().removeTask(liveTask);
                Future<?> future = liveTask.getFuture();
                if (future != null) {
                    future.cancel(true);
                }

                stop();
                reset();
            }
        } catch (FrameGrabber.Exception | RuntimeException | FrameRecorder.Exception e) {
            logger.error(e.getMessage(), e);
            stop();
            reset();
        }
    }

    private void stop() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            if (grabber != null) {
                grabber.stop();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void reset() {
        recorder = null;
        grabber = null;
        isStart = false;
    }
}
