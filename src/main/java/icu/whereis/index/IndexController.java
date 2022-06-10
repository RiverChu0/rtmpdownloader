package icu.whereis.index;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import icu.whereis.common.model.LiveTask;
import icu.whereis.common.utils.LiveTaskUtils;
import icu.whereis.common.utils.StringKit;
import icu.whereis.downloader.MediaVideoTransfer;
import icu.whereis.downloader.RtmpLiveDownloader;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法
 * 详见 JFinal 俱乐部: https://jfinal.com/club
 *
 * IndexController
 */
@Path(value = "/", viewPath = "/index")
public class IndexController extends Controller {

	private Map<String, String> names = new HashMap<String, String>();
	private String prefix = "KEY";

	public void index() {
		String v = getPara("v");
		if (StrKit.notBlank(v)) {
			String vv = PropKit.get("v");
			if (Objects.equals(v, vv)) {
				render("index.html");
				return;
			} else {
				renderError(404);
				return;
			}
		} else {
			renderError(404);
			return;
		}
	}

	public void downloadRtmp() {
		String rtmpUrl = getPara("rtmpUrl");
		String url1 = getPara("url1");
		String url2 = getPara("url2");
		String type = getPara("type");
		String url = getPara("url");
		String thtml = getPara("thtml");
		String xnm = getPara("xnm");

		doRtmp(rtmpUrl, url1, url2, type, url, thtml, xnm);
	}

	private void doRtmp(String rtmpUrl, String url1, String url2, String type, String url, String thtml, String xnm) {
		if (StrKit.isBlank(rtmpUrl)) {
			if (StrKit.isBlank(url)) {
				renderJson(Ret.fail("msg","地址不能为空"));
				return;
			}
			if (Objects.equals(type, "2")) {
				rtmpUrl = url2+url;
			} else if (Objects.equals(type, "1")) {
				rtmpUrl = url1+url;
			}
		}

		if (StrKit.notBlank(rtmpUrl)) {
			rtmpUrl = rtmpUrl.trim();
			if (!rtmpUrl.startsWith("rtmp") && !rtmpUrl.startsWith("http")) {
				renderJson(Ret.fail("msg", "仅支持rtmp、http"));
				return;
			}
			rtmpUrl = rtmpUrl.replace("<playpath>", "/");
			rtmpUrl = rtmpUrl.replace("<swfUrl>", "");
			rtmpUrl = rtmpUrl.replace("<pageUrl>", "");
			rtmpUrl = rtmpUrl.trim();

			String timestamp = StringKit.getTimestamp("yyyyMMddHHmmss");

			//提取文件名
			Pattern pattern = Pattern.compile("(live|taolu)/[A-Za-z0-9_.]+\\?");
			Matcher matcher = pattern.matcher(rtmpUrl);

			String currentDir = System.getProperty("user.dir")+File.separator;
			String namesTxt = currentDir+"config"+File.separator+"names.txt";

			try {
				List<String> urls = FileUtils.readLines(new File(namesTxt), Charset.forName("UTF-8"));
				for (int i=0; i<urls.size(); i++) {
					String line = urls.get(i);
					if (line != null && !"".equals(line)) {
						String[] lineAay = line.split(" ");
						String room = lineAay[0];
						String anchorName = lineAay[1];

						names.put(prefix+room, anchorName);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			String filename = "";
			String roomNumber = "";
			String anchorName = "";
			if (matcher.find()) {
				String group0 = matcher.group(0);
				String filename2 = group0.replaceAll("(live|taolu)/", "").replaceAll("\\?", "");
				filename2 = filename2.replace(".flv", "");

				// 套路直播的流
				if (filename2.contains("A")) {
					String[] taoluAay = filename2.split("A");
					anchorName = names.get(prefix+taoluAay[0]);
					roomNumber = taoluAay[0];

					filename = anchorName+"_"+taoluAay[0]+"_"+taoluAay[1]+".flv";
				} else if (filename2.contains("_")) {
					String[] aay = filename2.split("_");
					anchorName = names.get(prefix+aay[0]);
					roomNumber = aay[0];

					filename = aay[0]+"_"+anchorName+"_"+timestamp+".flv";
				} else {
					anchorName = names.get(prefix+filename2);
					roomNumber = filename2;
					filename = filename2+"_"+anchorName+"_"+timestamp+".flv";
				}
			}

			if (thtml!=null && thtml.contains("1")) {
				rtmpUrl = rtmpUrl.replace("rtmp", "http");
				rtmpUrl = rtmpUrl.replace("?", ".flv?");
			}
			if (xnm!=null && xnm.contains("1")) {
				rtmpUrl = rtmpUrl+"\r\n";
				rtmpUrl = rtmpUrl+"@";
			}

			RtmpLiveDownloader downloader = RtmpLiveDownloader.getDownloader();
			if (!downloader.isStart()) {
				downloader.startDownload();
			}

			downloader.addTask(rtmpUrl, anchorName, roomNumber);

			renderJson(Ret.ok("提交成功！"));
		} else {
			renderJson(Ret.fail("地址不能为空"));
		}
	}

	/*public void receiveFromClient() {

		UploadFile uploadFile = getFile();
		File file = uploadFile.getFile();
		try {
			String fileContent = FileUtils.readFileToString(file);

			if (fileContent.contains("rtmp")) {
				String rtmpPart = "rtmp://"+cutString(fileContent, "rtmp://", "/live")+"/live/";
				String appPart = cutString(fileContent, "play", "Response Head:");

				String reg = "[0-9]+[0-9a-z_=?&]+t=[0-9]+";
				Pattern pattern = Pattern.compile(reg);
				Matcher matcher = pattern.matcher(appPart);
				if (matcher.find()) {
					appPart = matcher.group(0);
				} else {
					reg = "[0-9]+[0-9?&]+auth_key=[0-9a-z\\-]+";
					pattern = Pattern.compile(reg);
					matcher = pattern.matcher(appPart);

					if (matcher.find()) {
						appPart = matcher.group(0);
					}
				}

				String rtmpUrl = rtmpPart + appPart;
				doRtmp(rtmpUrl, null, null, null, null, null, null);
			} else {
				String reqParams = getReqParams(fileContent);
				String resData = getResData(fileContent);

				JSONObject parseObject = JSONObject.parseObject(resData);
				String resData0 = parseObject.getString("data");

				try {
					String decResData = AESEncryption.decrypt(resData0);
					String decReqParams = AESEncryption.decrypt(reqParams);

					System.out.println(decResData);
					System.out.println(decReqParams);

					JSONObject jsonObject = JSONObject.parseObject(decResData);
					JSONObject jsonObject1 = JSONObject.parseObject(decReqParams);
					JSONObject roomInfo = jsonObject.getJSONObject("roomInfo");
					String playUrl = roomInfo.getString("playUrl");
					String roomId = roomInfo.getString("roomId");
					String userId = jsonObject1.getString("userId");
					String reqTime = jsonObject1.getString("temptime");

					String videoUrl = CommonUtil.getVideoUrl(true, playUrl, roomId, reqTime, userId);

					doRtmp(videoUrl, null, null, null, null, null, null);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			}

		} catch (IOException e) {

			e.printStackTrace();
		}

		file.delete();
		renderJson(Ret.ok());
	}*/

	private String getReqParams(String content) {
		String str1 = cutString(content, "reqParams=", "Response");

		return str1.trim();
	}

	private String getResData(String content) {
		String str1 = "{"+cutString(content, "\\{", "\\}")+"}";

		return str1.trim();
	}

	/**
	 * 字符串截取中间部分
	 */
	private String cutString(String str, String start, String end) {
		try{
			if (str == null || "".equals(str)) {
				return str;
			}
			String reg = start + "([\\s\\S]*)" + end;
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(str);
			while (matcher.find()) {
				str = matcher.group(1);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return str;
	}


}



