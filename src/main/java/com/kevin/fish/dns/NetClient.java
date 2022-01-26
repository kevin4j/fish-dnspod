package com.kevin.fish.dns;

import com.kevin.fish.utils.HttpClientUtils;

/**
 * @author kevin
 * @create 2022/1/24 15:12
 */
public class NetClient {

    public static String getCurrentHostIp(){
        return HttpClientUtils.get("https://myip.biturl.top/");
    }

    public static void main(String[] args) {
        getCurrentHostIp();
    }
}
