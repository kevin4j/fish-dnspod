package com.kevin.fish.job;

import com.kevin.fish.constant.DnsProviderEnum;
import com.kevin.fish.dns.DnsCommonClient;
import com.kevin.fish.dns.NetClient;
import com.kevin.fish.dns.dnspod.DnsPodClient;
import com.kevin.fish.utils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kevin
 * @create 2022/1/24 17:58
 */
@Component
public class DnsRefreshJob {

    private static Logger logger = LoggerFactory.getLogger(DnsRefreshJob.class);

    public static Map<String, Object> execResult = new HashMap<>();

    @PostConstruct
    public void loadConfig(){
        DnsCommonClient.loadConfig();

        refreshIp();
    }

    @Scheduled(cron = "0 0/1 * * *  ?")
    public void refreshIp(){
        String result = "success";
        try{
            //获取当前IP
            String currentIp = NetClient.getCurrentHostIp();
            logger.info("当前公网IP为："+currentIp);

            String dnsProvider = PropertyUtils.getProperty("dns.provider");
            if(StringUtils.isBlank(dnsProvider) || StringUtils.equals(DnsProviderEnum.DNSPOD.toString(), dnsProvider)){
                DnsPodClient.refreshDns(currentIp);
            }else{
                logger.error("执行任务出错：不支持的DNS服务商");
            }
        }catch (Exception e){
            logger.error("执行任务出错："+e.getMessage());
            result = e.getMessage();
        }finally {
            execResult.put("last_exec_time", new Date());
            execResult.put("exec_result", result);
        }
    }

}
