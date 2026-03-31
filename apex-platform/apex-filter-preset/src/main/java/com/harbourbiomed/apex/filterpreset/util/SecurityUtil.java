package com.harbourbiomed.apex.filterpreset.util;

import com.harbourbiomed.apex.auth.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 安全工具类，用于获取当前用户信息
 * 
 * @author Harbour BioMed
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final JwtUtil jwtUtil;

    /**
     * 从 Spring Security 上下文中获取当前用户 ID
     * 
     * @return 当前用户 ID
     * @throws IllegalStateException 如果用户未登录或 Token 无效
     */
    public Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = 
            SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("用户未登录");
        }

        // 从 Authentication 对象中获取用户 ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            // 从认证信息中获取用户名，然后查询获取用户 ID
            // 这里简化处理，实际项目中可以从自定义的 UserDetails 中获取
            String username = authentication.getName();
            log.debug("当前用户名: {}", username);
            // 注意：这里需要从数据库查询获取用户 ID，或者扩展 UserDetails 以包含用户 ID
            // 为简化，这里假设用户 ID 存储在 JWT 中
        }

        // 从 JWT Token 中获取用户 ID（更可靠的方式）
        // 由于 Spring Security 的 Authentication 已经通过 JWT 验证，我们需要重新从请求中提取 Token
        // 或者自定义 UserDetails 来包含用户 ID
        // 这里我们采用另一种方式：从请求头中提取 Token
        throw new IllegalStateException("无法获取当前用户 ID，请确保已正确配置安全上下文");
    }

    /**
     * 从请求中提取 Token 并获取用户 ID
     * 
     * @param request HTTP 请求
     * @return 用户 ID
     */
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            throw new IllegalStateException("未找到认证 Token");
        }

        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("从 Token 获取用户 ID 失败: {}", e.getMessage());
            throw new IllegalStateException("Token 无效或已过期");
        }
    }

    /**
     * 从请求中提取 Token
     * 
     * @param request HTTP 请求
     * @return Token 字符串
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
