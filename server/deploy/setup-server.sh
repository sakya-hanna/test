#!/usr/bin/env bash
# ChatNotes 同步服务器部署脚本（目标: Ubuntu/Debian VPS, 设计 §12.4）
# 前置: 本仓库已构建 server/build/libs/chatnotes-server.jar，gen-certs.sh 已产出证书
# 用法: 在 VPS 上，把 jar/server.crt/server.key/ca.crt/本目录脚本 放到同一目录后:
#   sudo bash setup-server.sh <服务器IP> <token明文>
set -euo pipefail

HOST="${1:?用法: setup-server.sh <服务器IP> <token>}"
TOKEN="${2:?缺少 token}"

# 1) 专用用户与目录（jar 与证书只读，data 可写）
id -u chatnotes &>/dev/null || useradd --system --home /opt/chatnotes --shell /usr/sbin/nologin chatnotes
mkdir -p /opt/chatnotes/data
cp chatnotes-server.jar /opt/chatnotes/
cp server.crt server.key /opt/chatnotes/
chown -R chatnotes:chatnotes /opt/chatnotes
chmod 640 /opt/chatnotes/server.key

# 2) token 落盘（服务读取 token.txt；权限限服务账号）
printf '%s' "$TOKEN" > /opt/chatnotes/token.txt
chown chatnotes:chatnotes /opt/chatnotes/token.txt
chmod 600 /opt/chatnotes/token.txt

# 3) systemd
cp chatnotes-server.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now chatnotes-server

# 4) 防火墙（ufo 无则跳过）
if command -v ufw >/dev/null; then ufw allow 8443/tcp; fi

sleep 2
echo "== 状态 =="
systemctl --no-pager status chatnotes-server | head -8
echo "== 本机探活（-k 忽略自签 CA，公网客户端用 ca.crt 校验）=="
curl -sk "https://127.0.0.1:8443/health" && echo
echo "部署完成。手机端填: https://$HOST:8443  令牌为 token.txt 内容"
