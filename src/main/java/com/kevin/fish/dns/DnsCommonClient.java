package com.kevin.fish.dns;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author kevin
 * @create 2022/1/25 10:14
 */
@Component
public abstract class DnsCommonClient<T> {

    private static Logger logger = LoggerFactory.getLogger(DnsCommonClient.class);

    public void initClient(){

    }

    public void refreshDns(String currentIp){
        initClient();

        if(StringUtils.isNotBlank(DnsCommonConfig.SUB_DOMAIN)){
            String[] subDomainArr = StringUtils.split(DnsCommonConfig.SUB_DOMAIN, ",");
            for(String subDomain : subDomainArr){
                logger.info("check sub-domain:{}", subDomain);
                T record = getRecordBySubDomain(subDomain);
                if(record == null){
                    logger.warn("sub-domain:{} not exist", subDomain);
                    //add record
                    addRecord(subDomain, currentIp);
                    continue;
                }
                String oldValue = getOldValue(record);
                if(!StringUtils.equals(oldValue, currentIp)){
                    logger.info("update sub-domain {}, old IP ( {} ), current IP ( {} )", subDomain, oldValue, currentIp);
                    modifyRecord(record, currentIp);
                }else{
                    logger.info("sub-domain:{} with same IP, skip", subDomain);
                }
            }
        }
    }

    public T getRecordBySubDomain(String subDomain){
        List<T> recordList = getRecordList(subDomain);
        return CollectionUtils.isEmpty(recordList) ? null : recordList.get(0) ;
    }

    public abstract List<T> getRecordList(String subDomain);

    public abstract String getOldValue(T record);

    public abstract void addRecord(String subDomain, String ip);

    public abstract void modifyRecord(T record, String newValue);

}
