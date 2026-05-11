#!/bin/bash

# AgentX本地开发环境启动脚本
# 专用于开发者进行本地开发和调试

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
ENV_PATH="$SCRIPT_DIR/.env"

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # 无颜色

# 项目信息
echo -e "${BLUE}"
echo "   ▄▄▄        ▄████  ▓█████  ███▄    █ ▄▄▄█████▓▒██   ██▒"
echo "  ▒████▄     ██▒ ▀█▒ ▓█   ▀  ██ ▀█   █ ▓  ██▒ ▓▒▒▒ █ █ ▒░"
echo "  ▒██  ▀█▄  ▒██░▄▄▄░ ▒███   ▓██  ▀█ ██▒▒ ▓██░ ▒░░░  █   ░"
echo "  ░██▄▄▄▄██ ░▓█  ██▓ ▒▓█  ▄ ▓██▒  ▐▌██▒░ ▓██▓ ░  ░ █ █ ▒ "
echo "   ▓█   ▓██▒░▒▓███▀▒ ░▒████▒▒██░   ▓██░  ▒██▒ ░ ▒██▒ ▒██▒"
echo -e "   ▒▒   ▓▒█░ ░▒   ▒  ░░ ▒░ ░░ ▒░   ▒ ▒   ▒ ░░   ▒▒ ░ ░▓ ░ ${NC}"
echo -e "${GREEN}            智能AI助手平台 - 开发环境启动工具${NC}"
echo -e "${BLUE}========================================================${NC}"
echo

# 检查Docker环境
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}错误: Docker未安装，请先安装Docker${NC}"
        exit 1
    fi

    if ! docker compose version &> /dev/null; then
        echo -e "${RED}错误: Docker Compose未安装或版本过低${NC}"
        exit 1
    fi
}

# 设置开发模式配置
set_development_mode() {
    MODE="dev"
    ENV_FILE="$SCRIPT_DIR/.env.local.example"
    
    echo -e "${GREEN}🔥 启动开发模式${NC}"
    echo "  - 内置数据库 + 消息队列"
    echo "  - 代码热重载支持"
    echo "  - 数据库管理工具 (Adminer)"
    echo "  - 调试端口开放"
    echo
}

# 准备环境配置
prepare_env() {
    if [ ! -f "$ENV_PATH" ]; then
        echo -e "${YELLOW}创建环境配置文件...${NC}"
        cp "$ENV_FILE" "$ENV_PATH"
        echo -e "${GREEN}✅ 已创建 .env 文件，基于模板: $ENV_FILE${NC}"
        
    else
        echo -e "${GREEN}✅ 使用现有 .env 配置文件: $ENV_PATH${NC}"
    fi
}

cleanup_legacy_mcp_containers() {
    echo -e "${BLUE}清理旧 MCP Gateway 容器...${NC}"
    LEGACY_CONTAINERS=$(docker ps -aq --filter "name=mcp-gateway" || true)
    if [ -n "$LEGACY_CONTAINERS" ]; then
        docker rm -f $LEGACY_CONTAINERS >/dev/null
        echo -e "${GREEN}✅ 已关闭并移除旧 MCP Gateway 容器${NC}"
    else
        echo -e "${GREEN}✅ 未发现旧 MCP Gateway 容器${NC}"
    fi
}

# 启动服务
start_services() {
    echo -e "${BLUE}启动AgentX服务...${NC}"
    echo "部署模式: $MODE"
    echo

    # 设置开发环境的Docker Compose后缀
    export DOCKERFILE_SUFFIX=".dev"

    # 启动开发环境服务 (使用local和dev profile)
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_PATH" --profile local --profile dev up -d --build --remove-orphans

    echo
    echo -e "${GREEN}🎉 AgentX启动完成！${NC}"
    echo
    echo -e "${BLUE}服务访问地址:${NC}"
    echo "  前端: http://localhost:3000"
    echo "  后端API: http://localhost:8088"
    echo "  API网关: http://localhost:8081"
    
    if [ "$MODE" = "dev" ]; then
        echo "  数据库管理: http://localhost:8082"
    fi
    
    echo
    echo -e "${BLUE}默认登录账号:${NC}"
    echo "  管理员: admin@agentx.ai / admin123"
    
    if [ "$MODE" = "local" ] || [ "$MODE" = "dev" ]; then
        echo "  测试用户: test@agentx.ai / test123"
    fi
    
    echo
    echo -e "${YELLOW}常用命令:${NC}"
    echo "  查看日志: docker compose -f deploy/docker-compose.yml --env-file deploy/.env logs -f"
    echo "  停止服务: docker compose -f deploy/docker-compose.yml --env-file deploy/.env down"
    echo "  重启服务: docker compose -f deploy/docker-compose.yml --env-file deploy/.env restart"
    echo "  查看状态: docker compose -f deploy/docker-compose.yml --env-file deploy/.env ps"
}

# 主程序
main() {
    check_docker
    
    echo -e "${YELLOW}AgentX 开发环境启动${NC}"
    echo "本脚本适用于开发者进行本地开发"
    echo "如需生产环境部署，请参考: docs/deployment/PRODUCTION_DEPLOY.md"
    echo
    
    set_development_mode
    prepare_env
    cleanup_legacy_mcp_containers
    start_services
}

# 运行主程序
main "$@"
