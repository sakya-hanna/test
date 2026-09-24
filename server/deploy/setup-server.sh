#!/usr/bin/env bash
# ChatNotes 同步服务器部署脚本（目标: Ubuntu/Debian VPS, 设计 §12.4）
# 前置: 本仓库已构建 server/build/libs/chatnotes-server.jar，gen-certs.sh 已产出 server.p12
# 用法: 在 VPS 上，把 jar/server.p12/本目录脚本 放到同一目录后:
#   sudo bash setup-server.sh <服务器IP> <token明文> [PKCS12口令]
set -euo pipefail

HOST="${1:?用法: setup-server.sh <服务器IP> <token>}"
TOKEN="${2:?缺少 token}"
KS_PASS="${3:-chatnotes}"
[[ "$KS_PASS" =~ ^[A-Za-z0-9._-]+$ ]] || { echo "证书口令只能包含字母、数字、点、下划线和连字符" >&2; exit 1; }
test -f chatnotes-server.jar && test -f server.p12 || { echo "缺少 chatnotes-server.jar 或 server.p12" >&2; exit 1; }

# 1) 专用用户与目录（jar 与证书只读，data 可写）
id -u chatnotes &>/dev/null || useradd --system --home /opt/chatnotes --shell /usr/sbin/nologin chatnotes
mkdir -p /opt/chatnotes/data
cp chatnotes-server.jar /opt/chatnotes/
cp server.p12 /opt/chatnotes/
chown -R chatnotes:chatnotes /opt/chatnotes
chmod 600 /opt/chatnotes/server.p12
printf 'CHATNOTES_KEYSTORE_PASSWORD=%s\n' "$KS_PASS" > /opt/chatnotes/keystore.env
chown chatnotes:chatnotes /opt/chatnotes/keystore.env
chmod 600 /opt/chatnotes/keystore.env

# 2) token 落盘（服务读取 token.txt；权限限服务账号）
printf '%s' "$TOKEN" > /opt/chatnotes/token.txt
chown chatnotes:chatnotes /opt/chatnotes/token.txt
chmod 600 /opt/chatnotes/token.txt

# 3) systemd
cp chatnotes-server.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now chatnotes-server

# 4) 防火墙（ufw 无则跳过）
if command -v ufw >/dev/null; then ufw allow 8443/tcp; fi

sleep 2
echo "== 状态 =="
systemctl --no-pager status chatnotes-server | head -8
echo "== 本机探活（仅此处 -k；手机客户端验证嵌入的 CA 和证书 SAN）=="
curl -sk "https://127.0.0.1:8443/health" && echo
echo "部署完成。手机端填: https://$HOST:8443  令牌为 token.txt 内容"
