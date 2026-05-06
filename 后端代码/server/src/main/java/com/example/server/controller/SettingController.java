package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.SystemSetting;
import com.example.server.mapper.SystemSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
public class SettingController {

    private static final String KEY_OPEN_APPLICATION = "OPEN_APPLICATION";

    @Autowired
    private SystemSettingMapper systemSettingMapper;

    @GetMapping("/open-application")
    public Result<Map<String, Object>> getOpenApplication() {
        SystemSetting s = systemSettingMapper.selectByKey(KEY_OPEN_APPLICATION);
        boolean open = s != null && "true".equalsIgnoreCase(s.getSettingValue());
        Map<String, Object> data = new HashMap<>();
        data.put("open", open);
        return Result.success(data);
    }

    @PutMapping("/open-application")
    public Result<String> setOpenApplication(@RequestParam("open") boolean open) {
        SystemSetting s = new SystemSetting();
        s.setSettingKey(KEY_OPEN_APPLICATION);
        s.setSettingValue(Boolean.toString(open));
        systemSettingMapper.upsert(s);
        return Result.success("ok");
    }
}


