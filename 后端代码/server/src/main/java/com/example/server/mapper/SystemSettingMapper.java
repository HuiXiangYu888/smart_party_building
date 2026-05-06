package com.example.server.mapper;

import com.example.pojo.entity.SystemSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemSettingMapper {
    SystemSetting selectByKey(@Param("settingKey") String settingKey);
    int upsert(SystemSetting setting);
}


