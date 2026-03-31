#!/bin/bash
# 后端热重载开发启动脚本
# Spring Boot DevTools 监听 classpath 变更自动重启
# 使用方式：./dev.sh

export JAVA_HOME=/opt/homebrew/opt/openjdk@21

cd "$(dirname "$0")"

if [ -f ./.env.local ]; then
  set -a
  . ./.env.local
  set +a
fi

echo "启动开发模式（热重载）..."
echo "代码修改后只需在 IDE 中触发重新编译（Build Project），应用会自动重启"
echo "或在另一个终端执行：JAVA_HOME=$JAVA_HOME mvn compile -pl apex-<模块名>"
echo ""

$JAVA_HOME/bin/java \
  -Dspring.devtools.restart.enabled=true \
  -Dspring.devtools.livereload.enabled=true \
  -jar apex-bootstrap/target/apex-bootstrap-1.0.0-SNAPSHOT.jar
