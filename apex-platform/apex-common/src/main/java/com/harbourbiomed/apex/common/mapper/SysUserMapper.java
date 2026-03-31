package com.harbourbiomed.apex.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.harbourbiomed.apex.common.entity.SysUser;
import org.apache.ibatis.annotations.Select;

/**
 * 系统用户 Mapper
 *
 * @author Harbour BioMed
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(String username);
}
