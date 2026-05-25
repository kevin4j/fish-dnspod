package com.kevin.fish.dns;

import com.kevin.fish.utils.HttpClientUtils;
import com.kevin.fish.utils.JSONUtilsEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author kevin
 * @create 2022/1/24 15:12
 */
public class NetClient {

    private static Logger logger = LoggerFactory.getLogger(NetClient.class);

    public static String[] ipUrls = {"https://myip.biturl.top", "https://ipecho.net/plain", "https://api-ipv4.ip.sb/ip","https://api.ip.sb/ip"};

    public static Map<String, String> currIpUrlInfo;

    public static String getCurrentHostIp(){
        logger.info("调用外部服务获取当前ip地址, {}", JSONUtilsEx.serialize(currIpUrlInfo));

        for (String url : ipUrls) {
            try {
                return HttpClientUtils.get(url);
            } catch (Exception e) {
                logger.error("调用外部服务获取当前ip地址出错：{}", e.getMessage(), e);
            }
        }

        return "";
    }

    public static void main(String[] args) {
        System.out.println(getCurrentHostIp());
    }
}
