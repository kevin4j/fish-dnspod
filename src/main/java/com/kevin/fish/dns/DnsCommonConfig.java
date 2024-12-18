package com.kevin.fish.dns;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author kevin
 */
@Component
public class DnsCommonConfig {

    private static Logger logger = LoggerFactory.getLogger(DnsCommonConfig.class);

    public static String LOGIN_TOKEN;

    public static String DOMAIN;

    public static String SUB_DOMAIN;

    public static String DNS_PROVIDER;

    @Value("${dns.token:}")
    public void setLoginToken(String loginToken){
        LOGIN_TOKEN = loginToken;
    }

    @Value("${dns.domain:}")
    public void setDomain(String domain){
        DOMAIN = domain;
    }

    @Value("${dns.sub_domain:}")
    public void setSubDomain(String subDomain){
        SUB_DOMAIN = subDomain;
    }

    @Value("${dns.provider:}")
    public void setDnsProvider(String dnsProvider){
        DNS_PROVIDER = dnsProvider;
    }

    public static void checkConfig(){
        logger.info("检查配置，dns.token:{}, dns.domain:{}, dns.sub_domain:{}",
                new String[]{LOGIN_TOKEN, DOMAIN, SUB_DOMAIN});

        Assert.isTrue(StringUtils.isNotBlank(LOGIN_TOKEN), "缺少dns.token配置");
        Assert.isTrue(StringUtils.isNotBlank(DOMAIN), "缺少dns.domain配置");
        Assert.isTrue(StringUtils.isNotBlank(SUB_DOMAIN), "缺少dns.sub_domain配置");
    }

}
