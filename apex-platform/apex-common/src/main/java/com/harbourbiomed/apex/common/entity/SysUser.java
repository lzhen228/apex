package com.harbourbiomed.apex.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体
 *
 * @author Harbour BioMed
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser extends BaseEntity {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码哈希")
    private String passwordHash;

    @Schema(description = "显示名称")
    private String displayName;

    @TableField("role")
    @Schema(description = "角色")
    private String role;

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;
}
