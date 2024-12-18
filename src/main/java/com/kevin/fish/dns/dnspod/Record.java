package com.kevin.fish.dns.dnspod;

import lombok.Data;

/**
 * @author kevin
 * @create 2022/1/24 11:54
 */
@Data
public class Record {

    /**
     * 记录ID编号
     */
    private String id;

    /**
     * 子域名，如"www"
     */
    private String name;

    /**
     * 记录值, 如 IP:200.200.200.200, CNAME: cname.dnspod.com., MX: mail.dnspod.com.
     */
    private String value;

    /**
     * 记录类型，"A"
     */
    private String type;

    /**
     * 解析记录的线路，"默认"
     */
    private String line;

    /**
     * 解析记录的线路ID
     */
    private String line_id;

    /**
     * 记录的 MX 记录值, 非 MX 记录类型，默认为 0
     */
    private String mx;

    /**
     * 记录状态，"enable"
     */
    private String status;

}
