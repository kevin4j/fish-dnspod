package com.kevin.fish.dns.dnspod;

import com.kevin.fish.core.exception.ServiceException;
import com.kevin.fish.dns.DnsCommonClient;
import com.kevin.fish.utils.HttpClientUtils;
import com.kevin.fish.utils.JSONUtilsEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kevin
 * @create 2022/1/24 11:23
 */
public class DnsPodClient extends DnsCommonClient {

    private static Logger logger = LoggerFactory.getLogger(DnsPodClient.class);

    public final static String BASE_URL = "https://dnsapi.cn";

    public final static String API_RECORD_LIST = "/Record.List";

    public final static String API_RECORD_MODIFY = "/Record.Modify";


    public static void refreshDns(String currentIp){
        if(StringUtils.isBlank(LOGIN_TOKEN)){
            loadConfig();
        }

        if(StringUtils.isNotBlank(SUB_DOMAIN)){
            String[] subDomainArr = StringUtils.split(SUB_DOMAIN, ",");
            for(String subDomain : subDomainArr){
                logger.info("check sub-domain:"+subDomain);
                Record record = getRecordBySubDomain(subDomain);
                if(record == null){
                    logger.error("sub-domain:"+subDomain + " not exist");
                    continue;
                }
                if(!StringUtils.equals(record.getValue(), currentIp)){
                    logger.info(String.format("update sub-domain %s, old IP ( %s ), current IP ( %s )", subDomain, record.getValue(), currentIp));
                    record.setValue(currentIp);
                    modifyRecord(record);
                }else{
                    logger.info("sub-domain:"+subDomain + " with same IP, skip");
                }
            }
        }
    }

    public static Map<String, String> getDefaultParams(){
        Map<String, String> params = new HashMap<>();
        params.put("login_token", LOGIN_TOKEN);
        params.put("format", "json");
        params.put("domain", DOMAIN);

        return params;
    }

    public static Record getRecordBySubDomain(String subDomain){
        List<Record> recordList = getRecordList(subDomain);
        return CollectionUtils.isEmpty(recordList) ? null : recordList.get(0);
    }

    public static List<Record> getRecordList(String subDomain){
        String url = String.format("%s%s", BASE_URL, API_RECORD_LIST);
        Map<String, String> params = getDefaultParams();
        params.put("record_type", "A");
        if(StringUtils.isNotBlank(subDomain)){
            params.put("sub_domain", subDomain);
        }
        String result = HttpClientUtils.post(url, params);
        logger.debug("查询record返回结果："+result);
        ResponseRecordList response = JSONUtilsEx.deserialize(result, ResponseRecordList.class);
        if(StringUtils.equals(response.getStatus().getCode(), "1")){
            return response.getRecords();
        }else{
            logger.error(String.format("url: %s, params:%s, response: %s", url, JSONUtilsEx.serialize(params), result));
            throw new ServiceException(String.format("查询记录列表出错，code：%s， msg：%s", response.getStatus().getCode(), response.getStatus().getMessage()));
        }
    }

    public static void modifyRecord(Record record){
        String url = String.format("%s%s", BASE_URL, API_RECORD_MODIFY);
        Map<String, String> params = getDefaultParams();
        params.put("record_id", record.getId());
        params.put("sub_domain", record.getName());
        params.put("record_type", record.getType());
        params.put("record_line", record.getLine());
        params.put("record_line_id", record.getLine_id());
        params.put("value", record.getValue());
        params.put("mx", record.getMx());
        String result = HttpClientUtils.post(url, params);
        logger.debug("修改record返回结果："+result);
        ResponseRecordModify response = JSONUtilsEx.deserialize(result, ResponseRecordModify.class);
        if(!StringUtils.equals(response.getStatus().getCode(), "1")){
            logger.error(String.format("url: %s, params:%s, response: %s", url, JSONUtilsEx.serialize(params), result));
            throw new ServiceException(String.format("更新记录出错，code：%s， msg：%s", response.getStatus().getCode(), response.getStatus().getMessage()));
        }
    }

    public static void main(String[] args) {

    }
}
