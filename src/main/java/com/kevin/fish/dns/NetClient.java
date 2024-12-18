package com.kevin.fish.dns;

import com.kevin.fish.utils.HttpClientUtils;
import com.kevin.fish.utils.JSONUtilsEx;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author kevin
 * @create 2022/1/24 15:12
 */
public class NetClient {

    private static Logger logger = LoggerFactory.getLogger(NetClient.class);

    public static Map<String, String> ipUrlMain = new HashMap<String, String>(){{
        put("type", "3");
        put("url", "https://www.ip.cn/api/index?ip=&type=0");
    }};

    public static Map<String, String> ipUrlSub = new HashMap<String, String>(){{
        put("type", "2");
        put("url", "http://23.80.5.90/ip.php");
    }};

    public static Map<String, String> ipUrlOther = new HashMap<String, String>(){{
        put("type", "0");
        put("url", "https://api.ipify.org, https://ip4.seeip.org, https://api.ip.sb/ip");
    }};

    public static Map<String, String> currIpUrlInfo = ipUrlMain;

    public static String getCurrentHostIp(){
        logger.info("调用外部服务获取当前ip地址, {}", JSONUtilsEx.serialize(currIpUrlInfo));
        if(StringUtils.equals(currIpUrlInfo.get("type"), "3")){
            try{
                return getCurrentHostIp3();
            }catch (Exception e){
                logger.error("调用外部服务获取当前ip地址出现异常{}", e);

                currIpUrlInfo = ipUrlSub;
                logger.info("切换为备用地址{}", ipUrlSub);
            }
        }else if(StringUtils.equals(currIpUrlInfo.get("type"), "2")){
            try{
                return getCurrentHostIp2();
            }catch (Exception e){
                logger.error("调用外部服务获取当前ip地址出现异常{}", e);

                currIpUrlInfo = ipUrlOther;
                logger.info("切换为其他地址{}", ipUrlOther);
            }
        }

        return getCurrentHostIp1();
    }

    public static String getCurrentHostIp1(){
        String[] urlArr = StringUtils.split(ipUrlOther.get("url"), ",");
        if(urlArr.length>0){
            int urlIndex = new Random().nextInt(urlArr.length);
            return HttpClientUtils.get(urlArr[urlIndex]);
        }
        return "";
    }

    public static String getCurrentHostIp2(){
        String htmlResult = HttpClientUtils.get(ipUrlSub.get("url"));
        Document document =Jsoup.parse(htmlResult);
        String body = document.body().text();
        return StringUtils.split(body, " ")[0];
    }

    public static String getCurrentHostIp3(){
        String jsonResult = HttpClientUtils.get(ipUrlMain.get("url"));
        Map<String, Object> resultMap = JSONUtilsEx.deserialize(jsonResult, Map.class);
        return (String) resultMap.get("ip");
    }

    public static void main(String[] args) {
        System.out.println(getCurrentHostIp());
    }
}
