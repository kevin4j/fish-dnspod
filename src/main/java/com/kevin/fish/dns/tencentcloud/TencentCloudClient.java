package com.kevin.fish.dns.tencentcloud;

import com.kevin.fish.dns.DnsCommonClient;
import com.kevin.fish.dns.DnsCommonConfig;
import com.kevin.fish.utils.JSONUtilsEx;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author kevin
 *
 * 腾讯云api集成dnspod https://cloud.tencent.com/document/api/1427/56194
 */
@Component
public class TencentCloudClient extends DnsCommonClient<RecordListItem> {

    private static Logger logger = LoggerFactory.getLogger(TencentCloudClient.class);

    public static Credential credential;

    public static DnspodClient client;

    @Override
    public void initClient(){
        super.initClient();
        if(client == null){
            credential = new Credential(DnsCommonConfig.LOGIN_TOKEN.split(",")[0], DnsCommonConfig.LOGIN_TOKEN.split(",")[1]);
            client = new DnspodClient(credential, "ap-shanghai");
        }
    }

    @Override
    public String getOldValue(RecordListItem record) {
        return record.getValue();
    }

    @Override
    public List<RecordListItem> getRecordList(String subDomain){
        DescribeRecordListRequest req = new DescribeRecordListRequest();
        req.setDomain(DnsCommonConfig.DOMAIN);
        req.setSubdomain(subDomain);
        req.setRecordType("A");

        try{
            DescribeRecordListResponse response = client.DescribeRecordList(req);
            logger.debug("查询record返回结果：{}", JSONUtilsEx.serialize(response));
            return Arrays.asList(response.getRecordList());

        } catch (Exception e){
            logger.error("查询record异常，request: {}, e:{}", JSONUtilsEx.serialize(req), e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void addRecord(String subDomain, String ip){
        CreateRecordRequest req = new CreateRecordRequest();
        req.setDomain(DnsCommonConfig.DOMAIN);
        req.setSubDomain(subDomain);
        req.setRecordType("A");
        req.setValue(ip);
        req.setRecordLine("默认");

        try{
            CreateRecordResponse response = client.CreateRecord(req);
            logger.debug("添加record返回结果：{}", JSONUtilsEx.serialize(response));
        } catch (Exception e){
            logger.error("添加record异常，request: {}, e:{}", JSONUtilsEx.serialize(req), e.getMessage(), e);
        }
    }

    @Override
    public void modifyRecord(RecordListItem record, String newValue){
        ModifyRecordRequest req = new ModifyRecordRequest();
        req.setRecordId(record.getRecordId());
        req.setDomain(DnsCommonConfig.DOMAIN);
        req.setSubDomain(record.getName());
        req.setRecordType(record.getType());
        req.setRecordLineId(record.getLineId());
        req.setRecordLine(record.getLine());
        req.setValue(newValue);

        try {
            ModifyRecordResponse response = client.ModifyRecord(req);
            logger.debug("修改record返回结果：{}", JSONUtilsEx.serialize(response));
        } catch (Exception e){
            logger.error("修改record异常，request: {}, e:{}", JSONUtilsEx.serialize(req), e.getMessage(), e);
        }
    }


}
