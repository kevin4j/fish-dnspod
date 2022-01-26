package com.kevin.fish.dns.dnspod;

import lombok.Data;

import java.util.List;

/**
 * @author kevin
 * @create 2022/1/24 12:11
 */
@Data
public class ResponseRecordList extends Response{

    private Domain domain;

    private List<Record> records;
}
