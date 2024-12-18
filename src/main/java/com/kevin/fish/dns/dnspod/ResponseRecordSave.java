package com.kevin.fish.dns.dnspod;

import lombok.Data;

/**
 * @author kevin
 * @create 2022/1/24 12:11
 */
@Data
public class ResponseRecordSave extends Response{

    private Record record;
}
