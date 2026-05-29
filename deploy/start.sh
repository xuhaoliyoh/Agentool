#!/bin/bash

# agentool 本地开发环境启动脚本

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
}

# 准备环境配置
prepare_env() {
    if [ ! -f "$ENV_PATH" ]; then
        cp "$ENV_FILE" "$ENV_PATH"
        echo -e "${GREEN}已创建环境配置: $ENV_PATH${NC}"
    else
        echo -e "${GREEN}使用环境配置: $ENV_PATH${NC}"
    fi
}

# 启动服务
start_services() {
    echo -e "${BLUE}启动 agentool 服务...${NC}"

    # 设置开发环境的Docker Compose后缀
    export DOCKERFILE_SUFFIX=".dev"

    # 启动开发环境服务 (使用local和dev profile)
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_PATH" --profile local --profile dev up -d --build --remove-orphans

    echo -e "${GREEN}agentool 启动完成${NC}"
    echo
    echo -e "${BLUE}访问地址:${NC}"
    echo "  前端: http://localhost:3000"
    echo "  后端: http://localhost:8088"
    if [ "$MODE" = "dev" ]; then
        echo "  Adminer: http://localhost:8082"
    fi
}

# 主程序
main() {
    check_docker
    
    echo -e "${YELLOW}agentool 开发环境启动${NC}"
    set_development_mode
    prepare_env
    start_services
}

# 运行主程序
main "$@"
