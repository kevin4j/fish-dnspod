package com.kevin.fish.dns.dnspod;

import lombok.Data;

/**
 * @author kevin
 * @create 2022/1/24 12:11
 */
@Data
public class ResponseStatus {

    private String code;

    private String message;

    private String created_at;

}
