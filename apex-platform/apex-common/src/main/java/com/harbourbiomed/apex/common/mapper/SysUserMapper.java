package com.harbourbiomed.apex.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.harbourbiomed.apex.common.entity.SysUser;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(String username);
}
