package com.kevin.fish.dns;

import com.kevin.fish.utils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author kevin
 * @create 2022/1/25 10:14
 */
public class DnsCommonClient {

    private static Logger logger = LoggerFactory.getLogger(DnsCommonClient.class);

    public static String LOGIN_TOKEN;

    public static String DOMAIN;

    public static String SUB_DOMAIN;

    public static void loadConfig(){
        LOGIN_TOKEN = PropertyUtils.getProperty("dns.token");
        DOMAIN = PropertyUtils.getProperty("dns.domain");
        SUB_DOMAIN = PropertyUtils.getProperty("dns.sub_domain");

        logger.info("加载配置，dns.token:{}, dns.domain:{}, dns.sub_domain:{}", new String[]{LOGIN_TOKEN, DOMAIN, SUB_DOMAIN});

        Assert.isTrue(StringUtils.isNotBlank(LOGIN_TOKEN), "缺少dns.token配置");
        Assert.isTrue(StringUtils.isNotBlank(DOMAIN), "缺少dns.domain配置");
        Assert.isTrue(StringUtils.isNotBlank(SUB_DOMAIN), "缺少dns.sub_domain配置");
    }

}
