# Apex 平台贡献指南

> **版本**: v1.0.0 &nbsp;|&nbsp; **最后更新**: 2026-03-30

---

## 目录

- [如何贡献](#如何贡献)
- [报告 Bug](#报告-bug)
- [提出功能请求](#提出功能请求)
- [Pull Request 流程](#pull-request-流程)

---

## 如何贡献

我们欢迎任何形式的贡献！无论你是修复 Bug、添加新功能、改进文档，还是提出建议，我们都非常感谢。

### 贡献方式

1. **修复 Bug**: 发现并修复现有问题
2. **添加新功能**: 实现新的功能特性
3. **改进文档**: 完善项目文档、API 文档等
4. **代码审查**: 帮助审查他人的代码
5. **测试**: 帮助进行测试和质量保证
6. **提供建议**: 提出改进建议和最佳实践

### 开始贡献前的准备

1. **阅读文档**:
   - [README.md](./README.md) - 项目概览
   - [DEVELOPMENT.md](./DEVELOPMENT.md) - 开发规范
   - [API.md](./API.md) - API 文档

2. **Fork 项目**:
   ```bash
   # 在 GitLab/GitHub 上 Fork 项目到你的账号
   ```

3. **克隆 Fork 的仓库**:
   ```bash
   git clone https://gitlab.com/your-username/apex.git
   cd apex
   ```

4. **添加上游仓库**:
   ```bash
   git remote add upstream https://gitlab.com/harbourbiomed/apex.git
   ```

5. **创建开发分支**:
   ```bash
   git checkout develop
   git pull upstream develop
   git checkout -b feature/your-feature-name
   ```

---

## 报告 Bug

### Bug 报告内容

一个好的 Bug 报告应该包含以下信息：

#### 基本信息

- **标题**: 简洁描述问题
- **版本**: 发生问题的版本号
- **环境**: 操作系统、浏览器版本（如果是前端问题）

#### 问题描述

- **复现步骤**: 详细列出如何复现问题
- **预期行为**: 期望发生什么
- **实际行为**: 实际发生了什么
- **截图/视频**: 如果可能，提供截图或录屏

#### 技术信息

- **错误日志**: 控制台错误日志或服务器日志
- **网络请求**: 如果是 API 问题，提供请求和响应详情

### Bug 报告模板

```markdown
### Bug 描述

简要描述问题

### 复现步骤

1. 进入 '...'
2. 点击 '....'
3. 滚动到 '....'
4. 看到错误

### 预期行为

描述期望发生什么

### 实际行为

描述实际发生了什么

### 截图

如果适用，添加截图帮助解释问题

### 环境

- 操作系统: [e.g. Windows 10, macOS 14]
- 浏览器: [e.g. Chrome 120, Firefox 120]
- 项目版本: [e.g. v1.0.0]

### 日志

控制台错误日志或服务器日志

### 附加信息

其他有助于解决问题的信息
```

### 如何提交 Bug 报告

1. 访问项目的 [Issues 页面](<repository-url>/issues)
2. 点击 "New Issue"
3. 选择 "Bug Report" 模板
4. 填写上述信息
5. 点击 "Submit issue"

---

## 提出功能请求

### 功能请求内容

一个好的功能请求应该包含：

#### 基本信息

- **标题**: 简洁描述功能
- **问题描述**: 为什么需要这个功能
- **解决方案**: 你希望如何实现这个功能
- **替代方案**: 是否有其他替代方案

#### 实现细节

- **用户故事**: 从用户角度描述功能
- **UI/UX 设计**: 如果涉及界面，提供设计思路
- **API 设计**: 如果涉及后端，提供 API 设计

### 功能请求模板

```markdown
### 功能描述

简要描述功能

### 问题背景

为什么需要这个功能？解决了什么痛点？

### 解决方案

详细描述你希望如何实现这个功能

### 用户故事

作为 [用户类型]
我希望 [功能描述]
这样 [用户价值]

### 实现细节

如果涉及 API，提供接口设计

### 附加信息

其他相关信息
```

### 如何提交功能请求

1. 访问项目的 [Issues 页面](<repository-url>/issues)
2. 点击 "New Issue"
3. 选择 "Feature Request" 模板
4. 填写上述信息
5. 点击 "Submit issue"

---

## Pull Request 流程

### PR 提交前检查清单

在提交 PR 前，请确保完成以下检查：

#### 代码质量

- [ ] 代码符合 [DEVELOPMENT.md](./DEVELOPMENT.md) 中的规范
- [ ] 没有编译错误或警告
- [ ] 没有调试代码（console.log, debugger 等）
- [ ] 代码已格式化
- [ ] 没有注释掉的代码

#### 测试

- [ ] 添加了适当的单元测试
- [ ] 所有测试通过
- [ ] 测试覆盖率符合要求（建议 > 80%）
- [ ] 手动测试了功能

#### 文档

- [ ] 更新了相关文档
- [ ] 添加了必要的注释
- [ ] API 变更更新了 [API.md](./API.md)

#### 提交信息

- [ ] 提交信息符合 [Conventional Commits](./DEVELOPMENT.md#commit-message-格式) 规范
- [ ] PR 标题清晰描述更改
- [ ] PR 描述详细说明更改内容

### PR 提交流程

#### 1. 同步上游代码

```bash
# 切换到 develop 分支
git checkout develop

# 拉取最新代码
git pull upstream develop

# 切换回你的功能分支
git checkout feature/your-feature-name

# 合并最新的 develop 代码
git merge develop

# 如果有冲突，解决冲突
# ...
```

#### 2. 提交代码

```bash
# 添加修改的文件
git add .

# 提交更改（遵循 Conventional Commits 规范）
git commit -m "feat(auth): 添加刷新令牌功能"

# 推送到你的 Fork 仓库
git push origin feature/your-feature-name
```

#### 3. 创建 Pull Request

1. 访问项目的 Pull Request 页面
2. 点击 "New Pull Request"
3. 选择你的功能分支
4. 填写 PR 信息

#### PR 描述模板

```markdown
## 变更类型

- [ ] Bug 修复（不破坏现有功能）
- [ ] 新功能（不破坏现有功能）
- [ ] 破坏性变更（破坏现有功能）
- [ ] 文档更新
- [ ] 性能优化
- [ ] 代码重构

## 变更描述

简要描述你的更改

## 相关 Issue

关联的 Issue 编号

- Closes #123
- Fixes #456

## 变更详情

### 后端变更

- [ ] 新增接口：列出新增的 API
- [ ] 修改接口：列出修改的 API
- [ ] 数据库变更：列出数据库迁移脚本

### 前端变更

- [ ] 新增页面/组件
- [ ] 修改页面/组件
- [ ] 样式变更

### 测试

- [ ] 单元测试
- [ ] 集成测试
- [ ] E2E 测试

## 截图

如果是界面相关更改，提供截图

## 检查清单

- [ ] 代码已通过测试
- [ ] 代码已格式化
- [ ] 文档已更新
- [ ] 没有引入安全漏洞

## 审查者

@reviewer1 @reviewer2
```

### PR 审查流程

#### 1. 自动检查

提交 PR 后，会自动运行以下检查：

- **CI/CD Pipeline**: 编译、测试、代码检查
- **CodeQL**: 安全扫描
- **SonarQube**: 代码质量分析

#### 2. 代码审查

- 至少需要 1 位审查者审批
- 审查者会检查代码质量、功能正确性、安全性等
- 根据审查意见修改代码

#### 3. 合并 PR

- 所有检查通过后，可以合并 PR
- 合并方式：Squash and merge
- 合并后删除功能分支

### PR 合并后

```bash
# 删除本地功能分支
git branch -d feature/your-feature-name

# 删除远程功能分支
git push origin --delete feature/your-feature-name

# 同步最新的 develop 分支
git checkout develop
git pull upstream develop
```

---

## 开发环境设置

### 后端开发环境

```bash
# 1. 克隆项目
git clone https://gitlab.com/your-username/apex.git
cd apex

# 2. 进入后端目录
cd apex-platform

# 3. 安装依赖
./mvnw install -DskipTests

# 4. 配置数据库
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
# 编辑 application-dev.yml，配置数据库连接

# 5. 启动应用
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 前端开发环境

```bash
# 1. 进入前端目录
cd apex-web

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

---

## 常见问题

### Q: 我的 PR 被拒绝了，怎么办？

A: 仔细阅读审查意见，根据反馈修改代码，然后重新提交。

### Q: 如何处理合并冲突？

A: 
```bash
git checkout feature/your-feature-name
git fetch upstream
git rebase upstream/develop
# 解决冲突
git add .
git rebase --continue
git push origin feature/your-feature-name --force
```

### Q: 如何为代码添加测试？

A: 参考现有的测试用例，编写单元测试和集成测试。确保测试覆盖率符合要求。

### Q: 贡献会被署名吗？

A: 是的，所有贡献者的名字会在提交记录和代码中保留。

---

## 贡献者规范

### 行为准则

- 尊重所有贡献者
- 保持专业和礼貌
- 接受建设性批评
- 专注于对社区最有利的事情

### 获得认可

- 所有贡献者会在项目中获得认可
- 重大贡献者会被邀请成为维护者
- 每次发布会列出贡献者

---

## 联系方式

- **项目负责人**: 陈明聪
- **技术支持**: tech-support@harbourbiomed.com
- **Issues**: [GitLab Issues](<repository-url>/issues)
- **Wiki**: [项目 Wiki](<repository-url>/wikis)

---

感谢你对 Apex 平台的贡献！

**和铂医药（Harbour BioMed）** © 2026
