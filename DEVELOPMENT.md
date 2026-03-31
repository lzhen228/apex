# Apex 平台开发规范

> **版本**: v1.0.0 &nbsp;|&nbsp; **最后更新**: 2026-03-30

---

## 目录

- [代码规范](#代码规范)
  - [后端 Java 代码规范](#后端-java-代码规范)
  - [前端 TypeScript 代码规范](#前端-typescript-代码规范)
- [Git 提交规范](#git-提交规范)
  - [Commit Message 格式](#commit-message-格式)
  - [分支策略](#分支策略)
- [命名规范](#命名规范)
  - [包命名](#包命名)
  - [类命名](#类命名)
  - [方法命名](#方法命名)
  - [变量命名](#变量命名)
- [注释规范](#注释规范)
  - [JavaDoc 注释](#javadoc-注释)
  - [JSDoc 注释](#jsdoc-注释)
- [代码审查清单](#代码审查清单)

---

## 代码规范

### 后端 Java 代码规范

#### 1. 代码格式

- **缩进**: 4 个空格（不使用 Tab）
- **行宽**: 不超过 120 字符
- **大括号**: 左大括号不换行，右大括号独占一行
- **空行**: 类与类之间空 2 行，方法之间空 1 行

```java
// ✅ 推荐
public class UserService {
    
    private final UserRepository userRepository;
    
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}

// ❌ 避免
public class UserService{
    private final UserRepository userRepository;
    public User findById(Long id){return userRepository.findById(id).orElseThrow(()->new BusinessException("用户不存在"));}
}
```

#### 2. 命名规范

| 类型 | 命名规范 | 示例 |
|------|----------|------|
| 类名 | PascalCase | `UserService`, `OrderController` |
| 接口 | PascalCase（以 I 开头可选） | `UserService`, `IUserService` |
| 方法名 | camelCase | `findById`, `createOrder` |
| 变量名 | camelCase | `userName`, `orderList` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 包名 | 全小写，点分隔 | `com.harbourbiomed.apex.service` |

#### 3. 编码最佳实践

```java
// ✅ 使用 Optional 避免空指针
public Optional<User> findById(Long id) {
    return userRepository.findById(id);
}

// ✅ 使用 Stream 进行集合操作
List<String> names = users.stream()
    .filter(user -> user.getAge() > 18)
    .map(User::getName)
    .collect(Collectors.toList());

// ✅ 使用 try-with-resources
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // 数据库操作
}

// ✅ 使用 Lombok 简化代码
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private Integer age;
}

// ✅ 使用 @Valid 进行参数校验
public R<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
    return R.ok(userService.createUser(request));
}
```

#### 4. 异常处理

```java
// ✅ 自定义业务异常
public class BusinessException extends RuntimeException {
    private final ResultCode code;
    
    public BusinessException(ResultCode code) {
        super(code.getMessage());
        this.code = code;
    }
}

// ✅ 统一异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return R.fail(e.getCode());
    }
    
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(ResultCode.INTERNAL_ERROR);
    }
}
```

#### 5. 日志规范

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    
    // ✅ 使用占位符，避免字符串拼接
    public void updateUser(Long id, String name) {
        log.info("开始更新用户，userId: {}, newName: {}", id, name);
        
        try {
            // 业务逻辑
            log.debug("用户更新成功，userId: {}", id);
        } catch (Exception e) {
            log.error("用户更新失败，userId: {}, name: {}", id, name, e);
            throw e;
        }
    }
}
```

#### 6. 依赖注入

```java
// ✅ 推荐使用构造函数注入
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
}

// ✅ 或使用 @Autowired
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// ❌ 避免使用字段注入
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository; // 不推荐
}
```

---

### 前端 TypeScript 代码规范

#### 1. 代码格式

- **缩进**: 2 个空格
- **行宽**: 不超过 100 字符
- **分号**: 必须使用
- **引号**: 优先使用单引号，JSX 属性使用双引号

```typescript
// ✅ 推荐
interface User {
  id: number
  name: string
  age: number
}

const fetchUser = async (id: number): Promise<User> => {
  const response = await fetch(`/api/users/${id}`)
  return response.json()
}

// ❌ 避免
interface User{id:number;name:string}
const fetchUser=async(id:number)=>{const res=await fetch(`/api/users/${id}`);return res.json()}
```

#### 2. 命名规范

| 类型 | 命名规范 | 示例 |
|------|----------|------|
| 组件名 | PascalCase | `MatrixTable`, `UserInfo` |
| 函数名 | camelCase | `fetchUser`, `handleClick` |
| 变量名 | camelCase | `userName`, `isLoading` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 接口/类型 | PascalCase | `UserType`, `ApiResponse` |
| 文件名 | kebab-case 或 camelCase | `user-list.tsx`, `useAuth.ts` |

#### 3. React 组件规范

```typescript
// ✅ 使用函数组件 + Hooks
import { useState, useEffect, useCallback } from 'react'
import type { PropsWithChildren } from 'react'

interface ButtonProps extends PropsWithChildren {
  onClick?: () => void
  disabled?: boolean
  variant?: 'primary' | 'secondary'
}

export const Button: React.FC<ButtonProps> = ({
  onClick,
  disabled = false,
  variant = 'primary',
  children,
}) => {
  const handleClick = useCallback(() => {
    if (!disabled && onClick) {
      onClick()
    }
  }, [disabled, onClick])
  
  return (
    <button 
      onClick={handleClick} 
      disabled={disabled}
      className={`btn btn-${variant}`}
    >
      {children}
    </button>
  )
}
```

#### 4. TypeScript 类型定义

```typescript
// ✅ 使用 interface 定义对象类型
interface User {
  id: number
  name: string
  email?: string // 可选属性
}

// ✅ 使用 type 定义联合类型、交叉类型等
type Status = 'pending' | 'success' | 'error'

type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

// ✅ 使用泛型提高复用性
const fetchData = async <T>(url: string): Promise<T> => {
  const response = await fetch(url)
  return response.json()
}

// ✅ 使用 enum 定义常量枚举
enum Phase {
  Approved = 'Approved',
  Phase3 = 'Phase III',
  Phase2 = 'Phase II',
  Phase1 = 'Phase I',
  Preclinical = 'Preclinical'
}
```

#### 5. 状态管理规范

```typescript
// ✅ 使用 Zustand 管理客户端状态
import { create } from 'zustand'

interface UserStore {
  user: User | null
  setUser: (user: User) => void
  clearUser: () => void
}

export const useUserStore = create<UserStore>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
  clearUser: () => set({ user: null }),
}))

// ✅ 使用 React Query 管理服务端状态
import { useQuery } from '@tanstack/react-query'

export const useUserQuery = (id: number) => {
  return useQuery({
    queryKey: ['user', id],
    queryFn: () => userService.findById(id),
    staleTime: 5 * 60 * 1000, // 5 分钟
  })
}
```

#### 6. 错误处理

```typescript
// ✅ 使用 try-catch 处理异步错误
const handleSubmit = async () => {
  setIsLoading(true)
  
  try {
    const result = await userService.createUser(formData)
    message.success('创建成功')
    router.push(`/users/${result.id}`)
  } catch (error) {
    message.error(error.message || '创建失败')
  } finally {
    setIsLoading(false)
  }
}

// ✅ 使用全局错误处理
instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout()
    }
    return Promise.reject(error)
  }
)
```

---

## Git 提交规范

### Commit Message 格式

使用 Conventional Commits 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type 类型

| 类型 | 说明 | 示例 |
|------|------|------|
| feat | 新功能 | feat(auth): 添加 JWT 认证 |
| fix | 修复 Bug | fix(matrix): 修复矩阵查询空指针问题 |
| docs | 文档更新 | docs(api): 更新 API 文档 |
| style | 代码格式（不影响功能） | style: 统一代码缩进 |
| refactor | 重构 | refactor(user): 重构用户服务层 |
| perf | 性能优化 | perf(matrix): 优化矩阵查询性能 |
| test | 测试相关 | test(auth): 添加认证单元测试 |
| chore | 构建/工具链相关 | chore: 升级 Spring Boot 版本 |
| revert | 回滚提交 | revert: 回滚上一个提交 |

#### 提交示例

```bash
# 新功能
git commit -m "feat(competition): 添加矩阵导出功能"

# 修复 Bug
git commit -m "fix(matrix): 修复疾病筛选不生效的问题"

# 文档更新
git commit -m "docs(readme): 更新部署说明"

# 重构
git commit -m "refactor(backend): 使用 Optional 替代空值判断"
```

#### Commit Message 示例（详细）

```
feat(auth): 添加刷新令牌功能

- 添加 POST /api/v1/auth/refresh 接口
- 实现令牌自动刷新逻辑
- 添加刷新令牌黑名单机制

Closes #123
```

---

### 分支策略

采用 Git Flow 分支模型：

#### 主要分支

| 分支 | 说明 | 生命周期 |
|------|------|----------|
| main | 主分支，只包含生产环境代码 | 永久 |
| develop | 开发分支，包含最新开发代码 | 永久 |
| feature/* | 功能开发分支 | 临时 |
| bugfix/* | Bug 修复分支 | 临时 |
| hotfix/* | 紧急修复分支 | 临时 |
| release/* | 发布准备分支 | 临时 |

#### 分支操作流程

```bash
# 1. 从 develop 创建分支开发新功能
git checkout develop
git pull origin develop
git checkout -b feature/matrix-export

# 2. 开发完成后合并回 develop
git add .
git commit -m "feat(competition): 添加矩阵导出功能"
git checkout develop
git merge feature/matrix-export

# 3. 删除功能分支
git branch -d feature/matrix-export

# 4. 发布时从 develop 创建 release 分支
git checkout develop
git checkout -b release/v1.1.0

# 5. 测试通过后合并到 main 和 develop
git checkout main
git merge release/v1.1.0
git tag -a v1.1.0 -m "Release version 1.1.0"
git checkout develop
git merge release/v1.1.0

# 6. 紧急修复直接从 main 创建 hotfix 分支
git checkout main
git checkout -b hotfix/critical-fix
# ... 修复 ...
git checkout main
git merge hotfix/critical-fix
git checkout develop
git merge hotfix/critical-fix
```

---

## 命名规范

### 包命名

```java
// ✅ 推荐
com.harbourbiomed.apex.auth
com.harbourbiomed.apex.competition.service
com.harbourbiomed.apex.common.exception

// ❌ 避免
com.harbourbiomed.apex.AuthService
com.harbourbiomed.apex.competition_service
```

### 类命名

```java
// ✅ 推荐
public class UserService {}
public class OrderController {}
public class BusinessException extends RuntimeException {}
public interface UserRepository extends JpaRepository<User, Long> {}

// ❌ 避免
public class user_service {}
public class ordercontroller {}
```

### 方法命名

```java
// ✅ 推荐（动词开头）
public User findById(Long id) {}
public List<User> findAll() {}
public void createOrder(Order order) {}
public void updateOrder(Order order) {}
public void deleteOrder(Long id) {}
public boolean existsById(Long id) {}

// ✅ 布尔返回值以 is/has/can 开头
public boolean isValid() {}
public boolean hasPermission() {}
public boolean canAccess() {}

// ❌ 避免
public User get_user_by_id(Long id) {}
public void create_order(Order order) {}
```

### 变量命名

```java
// ✅ 推荐
String userName;
List<User> userList;
int maxRetryCount;
boolean isLoading;

// ✅ 常量使用大写下划线
private static final int MAX_RETRY_COUNT = 3;
private static final String DEFAULT_ROLE = "USER";

// ❌ 避免
String user_name;
List<User> UserList;
int max_retry_count;
```

---

## 注释规范

### JavaDoc 注释

```java
/**
 * 用户服务
 * 
 * 提供用户相关的业务逻辑处理，包括用户查询、创建、更新、删除等操作
 * 
 * @author 陈明聪
 * @since 1.0.0
 */
@Service
public class UserService {
    
    /**
     * 根据用户 ID 查询用户
     * 
     * @param id 用户 ID，不能为空
     * @return 用户信息
     * @throws BusinessException 当用户不存在时抛出
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
    }
    
    /**
     * 创建用户
     * 
     * @param request 用户创建请求
     * @return 创建成功的用户信息
     * @throws IllegalArgumentException 当参数校验失败时抛出
     */
    public User createUser(CreateUserRequest request) {
        // 实现逻辑
    }
    
    /**
     * 用户数据传输对象
     */
    @Data
    public static class UserDTO {
        /** 用户 ID */
        private Long id;
        
        /** 用户名 */
        private String username;
        
        /** 邮箱地址 */
        private String email;
        
        /** 创建时间 */
        private LocalDateTime createdAt;
    }
}
```

### JSDoc 注释

```typescript
/**
 * 用户服务类
 * 
 * 提供用户相关的 API 调用方法
 * 
 * @author 陈明聪
 * @since 1.0.0
 */
export class UserService {
  /**
   * 根据用户 ID 查询用户
   * 
   * @param id - 用户 ID
   * @returns 用户信息
   * @throws {Error} 当用户不存在时抛出错误
   * 
   * @example
   * ```typescript
   * const user = await userService.findById(1)
   * console.log(user.name)
   * ```
   */
  async findById(id: number): Promise<User> {
    const response = await fetch(`/api/users/${id}`)
    if (!response.ok) {
      throw new Error('用户不存在')
    }
    return response.json()
  }
  
  /**
   * 创建用户
   * 
   * @param data - 用户数据
   * @returns 创建成功的用户信息
   */
  async createUser(data: CreateUserRequest): Promise<User> {
    const response = await fetch('/api/users', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return response.json()
  }
}

/**
 * 用户类型定义
 */
export interface User {
  /** 用户 ID */
  id: number
  
  /** 用户名 */
  username: string
  
  /** 邮箱地址 */
（
  email?: string
  
  /** 创建时间 */
  createdAt: string
}
```

---

## 代码审查清单

### 通用项

- [ ] 代码符合项目编码规范
- [ ] 没有调试代码（console.log, debugger 等）
- [ ] 没有注释掉的代码
- [ ] 没有硬编码的敏感信息
- [ ] 有适当的单元测试
- [ ] 测试覆盖率符合要求

### 后端 Java 审查项

- [ ] 类和方法有 JavaDoc 注释
- [ ] 异常处理得当，不吞掉异常
- [ ] 数据库查询有适当的索引
- [ ] SQL 注入风险已处理
- [ ] 资源正确释放（try-with-resources）
- [ ] 使用 Optional 避免空指针
- [ ] 日志级别使用恰当
- [ ] 敏感信息不记录到日志
- [ ] 事务边界清晰
- [ ] 循环中没有数据库查询

### 前端 TypeScript 审查项

- [ ] 组件和函数有 JSDoc 注释
- [ ] Props 和 State 有明确的类型定义
- [ ] 没有 any 类型（除非必要）
- [ ] 不直接修改 state
- [ ] 事件处理函数使用 useCallback
- [ ] 大列表使用虚拟滚动
- [ ] 没有 console.log
- [ ] 错误边界处理得当
- [ ] API 调用有 loading 和 error 状态
- [ ] 样式使用 CSS Modules 或 styled-components

### 安全审查项

- [ ] 输入参数进行校验
- [ ] SQL 注入防护
- [ ] XSS 防护
- [ ] CSRF 防护
- [ ] 敏感数据加密
- [ ] 权限检查正确
- [ ] API 访问限流
- [ ] 日志不泄露敏感信息

### 性能审查项

- [ ] 大数据查询使用分页
- [ ] 缓存策略合理
- [ ] 不重复查询数据库
- [ ] 图片资源优化
- [ ] 前端资源压缩
- [ ] 避免不必要的重渲染
- [ ] 使用索引优化查询
- [ ] 批量操作代替循环操作

---

**和铂医药（Harbour BioMed）** © 2026
