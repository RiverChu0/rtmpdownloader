package icu.whereis.common.utils;

import icu.whereis.common.model.LivePlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台工具类
 */
public class PlatFormUtils {

    private static Map<String, LivePlatform> platformMap = new HashMap<>();

    public static List<LivePlatform> init() {
        List<LivePlatform> platforms = new ArrayList<>();
        LivePlatform lp = new LivePlatform();
        lp.setId("4");
        lp.setName("半糖");
        platforms.add(lp);

        return platforms;
    }

    /**
     * 获取map形式的平台信息
     * @return
     */
    public static Map<String, LivePlatform> getPlatformMap() {
        List<LivePlatform> platforms = init();
        platformMap.clear();

        for (LivePlatform lp : platforms) {
            String id = lp.getId();
            platformMap.put(id, lp);
        }
        return platformMap;
    }
}
