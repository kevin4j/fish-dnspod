package com.kevin.fish.dns;

import com.kevin.fish.utils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * @author kevin
 * @create 2022/1/25 10:14
 */
public class DnsCommonClient {

    public static String LOGIN_TOKEN;

    public static String DOMAIN;

    public static String SUB_DOMAIN;

    public static void loadConfig(){
        LOGIN_TOKEN = PropertyUtils.getProperty("dns.token");
        DOMAIN = PropertyUtils.getProperty("dns.domain");
        SUB_DOMAIN = PropertyUtils.getProperty("dns.sub_domain");

        Assert.isTrue(StringUtils.isNotBlank(LOGIN_TOKEN), "缺少dns.token配置");
        Assert.isTrue(StringUtils.isNotBlank(DOMAIN), "缺少dns.domain配置");
        Assert.isTrue(StringUtils.isNotBlank(SUB_DOMAIN), "缺少dns.sub_domain配置");
    }

}
