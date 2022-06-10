package icu.whereis.common.utils;

import icu.whereis.common.model.LivePlatform;
import icu.whereis.common.model.LiveTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LiveTaskUtils {
    private static final List<LiveTask> tasks = new LinkedList<>();
    private static final Map<String, LiveTask> taskMap = new HashMap<>();

    public static LiveTask addLiveTask(String url, String anchorName, String roomNumber, String platformId, boolean monitoring, Integer urlType) {
        LiveTask liveTaskInMap = taskMap.get(roomNumber+"_"+platformId);
        if (liveTaskInMap == null) {
            LiveTask lt = new LiveTask();
            lt.setUrl(url);
            lt.setAnchorName(anchorName);
            lt.setRoomNumber(roomNumber);
            lt.setId(roomNumber+"_"+platformId);
            lt.setMonitoring(monitoring);
            lt.setUrlType(urlType);

            LivePlatform platform = PlatFormUtils.getPlatformMap().get(platformId);
            lt.setPlatform(platform);

            taskMap.put(roomNumber+"_"+platformId, lt);
            tasks.add(lt);
            return lt;
        }
        liveTaskInMap.setAnchorName(anchorName);
        // 为套路直播加入
        liveTaskInMap.setUrl(url);

        return liveTaskInMap;
    }

    public static boolean checkLiveTaskExists(String id) {
        LiveTask liveTaskInMap = taskMap.get(id);
        return liveTaskInMap == null ? false : true;
    }

    public static LiveTask getLiveTaskById(String id) {
        return taskMap.get(id);
    }

    public static List<LiveTask> getTasks() {
        return tasks;
    }

    /**
     * 设置监控
     * @param id
     */
    public static void setMonitoring(String id) {
        taskMap.get(id).setMonitoring(true);
    }

    /**
     * 取消监控
     * @param id
     */
    public static void cancelMonitoring(String id) {
        taskMap.get(id).setMonitoring(false);
    }

    public static void removeTask(LiveTask liveTask) {
        taskMap.remove(liveTask);
    }
}
