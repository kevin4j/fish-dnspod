package com.kevin.fish.dns.dnspod;

import com.kevin.fish.dns.DnsCommonClient;
import com.kevin.fish.dns.DnsCommonConfig;
import com.kevin.fish.utils.HttpClientUtils;
import com.kevin.fish.utils.JSONUtilsEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kevin
 * @create 2022/1/24 11:23
 */
@Component
public class DnsPodClient extends DnsCommonClient<Record> {

    private static Logger logger = LoggerFactory.getLogger(DnsPodClient.class);

    public final static String BASE_URL = "https://dnsapi.cn";

    public final static String API_RECORD_LIST = "/Record.List";

    public final static String API_RECORD_MODIFY = "/Record.Modify";

    public final static String API_RECORD_Create = "/Record.Create";

    public Map<String, String> getDefaultParams(){
        Map<String, String> params = new HashMap<>();
        params.put("login_token", DnsCommonConfig.LOGIN_TOKEN);
        params.put("format", "json");
        params.put("domain", DnsCommonConfig.DOMAIN);

        return params;
    }

    @Override
    public String getOldValue(Record record) {
        return record.getValue();
    }

    @Override
    public List<Record> getRecordList(String subDomain){
        String url = String.format("%s%s", BASE_URL, API_RECORD_LIST);
        Map<String, String> params = getDefaultParams();
        params.put("record_type", "A");
        if(StringUtils.isNotBlank(subDomain)){
            params.put("sub_domain", subDomain);
        }
        String result = HttpClientUtils.post(url, params);
        logger.debug("查询record返回结果：{}", result);
        ResponseRecordList response = JSONUtilsEx.deserialize(result, ResponseRecordList.class);
        if(StringUtils.equals(response.getStatus().getCode(), "1")){
            return response.getRecords();
        }else{
            logger.error("查询record, url: {}, params:{}, response: {}, message:{}", url, JSONUtilsEx.serialize(params), result, response.getStatus().getMessage());
        }
        return null;
    }

    @Override
    public void addRecord(String subDomain, String ip){
        String url = String.format("%s%s", BASE_URL, API_RECORD_Create);
        Map<String, String> params = getDefaultParams();
        params.put("domain", DnsCommonConfig.DOMAIN);
        params.put("sub_domain", subDomain);
        params.put("record_type", "A");
        params.put("record_line", "\u9ed8\u8ba4");
        params.put("record_line_id", "0");
        params.put("value", ip);
        String result = HttpClientUtils.post(url, params);
        logger.debug("添加record返回结果：{}", result);
        ResponseRecordSave response = JSONUtilsEx.deserialize(result, ResponseRecordSave.class);
        if(!StringUtils.equals(response.getStatus().getCode(), "1")){
            logger.error("添加record, url: {}, params:{}, response: {}, message:{}", url, JSONUtilsEx.serialize(params), result, response.getStatus().getMessage());
        }
    }

    @Override
    public void modifyRecord(Record record, String newValue){
        String url = String.format("%s%s", BASE_URL, API_RECORD_MODIFY);
        Map<String, String> params = getDefaultParams();
        params.put("record_id", record.getId());
        params.put("sub_domain", record.getName());
        params.put("record_type", record.getType());
        params.put("record_line", record.getLine());
        params.put("record_line_id", record.getLine_id());
        params.put("value", newValue);
        params.put("mx", record.getMx());
        String result = HttpClientUtils.post(url, params);
        logger.debug("修改record返回结果：{}", result);
        ResponseRecordSave response = JSONUtilsEx.deserialize(result, ResponseRecordSave.class);
        if(!StringUtils.equals(response.getStatus().getCode(), "1")){
            logger.error("更新record, url: {}, params:{}, response: {}, message:{}", url, JSONUtilsEx.serialize(params), result, response.getStatus().getMessage());
        }
    }
}
