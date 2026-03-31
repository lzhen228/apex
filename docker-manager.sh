#!/bin/bash
# Apex 平台 Docker 管理脚本
# 提供便捷的 Docker Compose 操作命令

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 显示帮助信息
show_help() {
    echo -e "${BLUE}Apex 平台 Docker 管理脚本${NC}"
    echo ""
    echo "用法: ./docker-manager.sh [命令] [选项]"
    echo ""
    echo "命令:"
    echo "  start           启动所有服务（后台运行）"
    echo "  start-dev       启动开发环境"
    echo "  start-prod      启动生产环境"
    echo "  stop            停止所有服务"
    echo "  restart         重启所有服务"
    echo "  logs            查看所有服务日志"
    echo "  logs [service]  查看指定服务日志"
    echo "  build           重新构建所有服务"
    echo "  build [service] 重新构建指定服务"
    echo "  ps              查看服务状态"
    echo "  clean           清理所有容器、网络和数据卷"
    echo "  clean-images    清理所有镜像"
    echo "  status          查看详细状态"
    echo "  db-shell        进入 PostgreSQL 容器"
    echo "  redis-shell     进入 Redis 容器"
    echo "  db-backup       备份数据库"
    echo "  db-restore      恢复数据库"
    echo "  help            显示帮助信息"
    echo ""
    echo "示例:"
    echo "  ./docker-manager.sh start"
    echo "  ./docker-manager.sh logs apex-platform"
    echo "  ./docker-manager.sh build apex-platform"
    echo "  ./docker-manager.sh db-backup"
}

# 检查 Docker Compose 版本
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}错误: Docker 未安装${NC}"
        exit 1
    fi

    if ! docker compose version &> /dev/null; then
        echo -e "${RED}错误: Docker Compose 未安装${NC}"
        exit 1
    fi
}

# 启动服务
start_services() {
    local env=$1
    local compose_file="docker-compose.yml"
    
    if [ "$env" = "prod" ]; then
        compose_file="docker-compose.prod.yml"
        echo -e "${YELLOW}启动生产环境...${NC}"
    else
        echo -e "${YELLOW}启动开发环境...${NC}"
    fi

    docker compose -f "$compose_file" up -d
    echo -e "${GREEN}✓ 服务已启动${NC}"
}

# 停止服务
stop_services() {
    local env=$1
    local compose_file="docker-compose.yml"
    
    if [ "$env" = "prod" ]; then
        compose_file="docker-compose.prod.yml"
    fi

    echo -e "${YELLOW}停止服务...${NC}"
    docker compose -f "$compose_file" down
    echo -e "${GREEN}✓ 服务已停止${NC}"
}

# 重启服务
restart_services() {
    local env=$1
    local compose_file="docker-compose.yml"
    
    if [ "$env" = "prod" ]; then
        compose_file="docker-compose.prod.yml"
    fi

    echo -e "${YELLOW}重启服务...${NC}"
    docker compose -f "$compose_file" restart
    echo -e "${GREEN}✓ 服务已重启${NC}"
}

# 查看日志
view_logs() {
    local env=$1
    local service=$2
    local compose_file="docker-compose.yml"
    
    if [ "$env" = "prod" ]; then
        compose_file="docker-compose.prod.yml"
    fi

    if [ -z "$service" ]; then
        echo -e "${YELLOW}查看所有服务日志...${NC}"
        docker compose -f "$compose_file" logs -f
    else
        echo -e "${YELLOW}查看 $service 日志...${NC}"
        docker compose -f "$compose_file" logs -f "$service"
    fi
}

# 构建服务
build_services() {
    local env=$1
    local service=$2
    local compose_file="docker-compose.yml"
    
    if [ "$env" = "prod" ]; then
        compose_file="docker-compose.prod.yml"
    fi

    if [ -z "$service" ]; then
        echo -e "${YELLOW}重新构建所有服务...${NC}"
        docker compose -f "$compose_file" build --no-cache
    else
        echo -e "${YELLOW}重新构建 $service...${NC}"
        docker compose -f "$compose_file" build --no "$service"
    fi
    
    echo -e "${GREEN}✓ 构建完成${NC}"
}

# 查看服务状态
view_status() {
    echo -e "${BLUE}服务状态:${NC}"
    docker compose ps
    
    echo ""
    echo -e "${BLUE}Docker 资源统计:${NC}"
    docker stats --no-stream 2>/dev/null || echo "没有运行中的容器"
}

# 清理资源
clean_resources() {
    local env=$1
    local compose_file="docker-compose.yml"
    
    if [ "$env" = "prod" ]; then
        compose_file="docker-compose.prod.yml"
    fi

    echo -e "${YELLOW}清理 Docker 资源...${NC}"
    read -p "确认删除所有容器、网络和数据卷？[y/N] " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker compose -f "$compose_file" down -v --rmi all
        echo -e "${GREEN}✓ 清理完成${NC}"
    else
        echo -e "${YELLOW}已取消${NC}"
    fi
}

# 清理镜像
clean_images() {
    echo -e "${YELLOW}清理未使用的镜像...${NC}"
    docker image prune -a -f
    echo -e "${GREEN}✓ 清理完成${NC}"
}

# 进入数据库容器
db_shell() {
    echo -e "${YELLOW}进入 PostgreSQL 容器...${NC}"
    docker compose exec postgres psql -U apex_user -d apex
}

# 进入 Redis 容器
redis_shell() {
    echo -e "${YELLOW}进入 Redis 容器...${NC}"
    docker compose exec redis redis-cli
}

# 备份数据库
db_backup() {
    local backup_dir="./backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"
    
    echo -e "${YELLOW}备份数据库到 $backup_dir...${NC}"
    
    # 备份数据库
    docker compose exec -T postgres pg_dump -U apex_user apex > "$backup_dir/database.sql"
    
    # 备份 Redis
    docker compose exec redis redis-cli --rdb /data/backup.rdb
    docker cp apex-redis:/data/backup.rdb "$backup_dir/redis.rdb"
    
    echo -e "${GREEN}✓ 备份完成: $backup_dir${NC}"
    echo "备份文件:"
    ls -lh "$backup_dir"
}

# 恢复数据库
db_restore() {
    local backup_dir=$1
    
    if [ -z "$backup_dir" ]; then
        echo -e "${RED}错误: 请指定备份目录${NC}"
        echo "用法: ./docker-manager.sh db-restore <backup-directory>"
        exit 1
    fi
    
    if [ ! -d "$backup_dir" ]; then
        echo -e "${RED}错误: 备份目录不存在: $backup_dir${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}从 $backup_dir 恢复数据库...${NC}"
    read -p "确认恢复数据库？这将覆盖现有数据！[y/N] " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # 恢复 PostgreSQL
        docker compose exec -T postgres psql -U apex_user apex < "$backup_dir/database.sql"
        
        # 恢复 Redis
        docker cp "$backup_dir/redis.rdb" apex-redis:/data/dump.rdb
        docker compose exec redis redis-cli --rdb /data/dump.rdb
        
        echo -e "${GREEN}✓ 恢复完成${NC}"
    else
        echo -e "${YELLOW}已取消${NC}"
    fi
}

# 主函数
main() {
    check_docker
    
    local command=$1
    shift || true
    
    case $command in
        start)
            start_services "dev"
            ;;
        start-dev)
            start_services "dev"
            ;;
        start-prod)
            start_services "prod"
            ;;
        stop)
            stop_services "dev"
            ;;
        restart)
            restart_services "dev"
            ;;
        logs)
            view_logs "dev" "$1"
            ;;
        build)
            build_services "dev" "$1"
            ;;
        ps)
            view_status
            ;;
        clean)
            clean_resources "dev"
            ;;
        clean-images)
            clean_images
            ;;
        status)
            view_status
            ;;
        db-shell)
            db_shell
            ;;
        redis-shell)
            redis_shell
            ;;
        db-backup)
            db_backup
            ;;
        db-restore)
            db_restore "$1"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo -e "${RED}未知命令: $command${NC}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
