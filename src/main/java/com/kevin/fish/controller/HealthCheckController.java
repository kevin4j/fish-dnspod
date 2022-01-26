package com.kevin.fish.controller;

import com.kevin.fish.job.DnsRefreshJob;
import com.kevin.fish.utils.JSONUtilsEx;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author kevin
 * @create 2022/1/25 11:50
 */
@RestController
@RequestMapping("/")
@ResponseBody
public class HealthCheckController {

    @RequestMapping("/healthCheck")
    public String check(){
        return "success";
    }

    @RequestMapping("/stats")
    public String stats(){
        return JSONUtilsEx.serialize(DnsRefreshJob.execResult);
    }
}
