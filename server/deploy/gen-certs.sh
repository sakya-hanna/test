#!/usr/bin/env bash
# ChatNotes 自建 CA + 服务器证书签发（设计文档 §12.4：裸 IP 部署，无 Let's Encrypt）
# 用法: bash gen-certs.sh <服务器IP或域名> [输出目录]
# 产物:
#   ca.crt / ca.key           自建 CA（ca.key 只存本地安全处，绝不放服务器）
#   server.crt / server.key   服务器证书（放服务器，Ktor 直接用）
#   ca.crt 需要拷到手机供 app 信任（network security config）
set -euo pipefail

HOST="${1:?用法: gen-certs.sh <服务器IP或域名> [输出目录]}"
OUT="${2:-certs}"
DAYS="${DAYS:-825}"   # 约 27 个月，到期前重跑本脚本换发即可，无需改 app
KS_PASS="${KS_PASS:-chatnotes}"

mkdir -p "$OUT"; cd "$OUT"

# 1) CA（已存在则复用：同一 CA 换发服务器证书不用更新 app）
if [ -f ca.crt ]; then
  echo "复用已有 CA: $OUT/ca.crt"
else
  openssl req -x509 -newkey rsa:3072 -nodes -days 3650 \
    -keyout ca.key -out ca.crt -subj "/CN=ChatNotes Root CA" \
    -addext "basicConstraints=critical,CA:TRUE" \
    -addext "keyUsage=critical,keyCertSign,cRLSign"
  chmod 600 ca.key
  echo "已生成 CA（ca.key 务必妥善保管，勿上传/勿放服务器）"
fi

# 2) 服务器证书（SAN 必须含实际访问的 IP/域名，否则 Android 握手失败）
openssl req -newkey rsa:2048 -nodes \
  -keyout server.key -out server.csr -subj "/CN=$HOST"

# git-bash 下 mktemp 的 /tmp 路径 Windows 原生 openssl 读不到，用本地固定名
cat > san.cnf <<EOF
subjectAltName = DNS:$HOST, IP:$HOST
basicConstraints = critical,CA:FALSE
keyUsage = critical,digitalSignature,keyEncipherment
extendedKeyUsage = serverAuth
EOF
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -days "$DAYS" -sha256 -extfile san.cnf -out server.crt
rm -f san.cnf

# 3) PKCS12 keystore（Ktor SSL 直接加载）
openssl pkcs12 -export -in server.crt -inkey server.key \
  -out server.p12 -name chatnotes -passout pass:"$KS_PASS"

echo "完成。产物："
echo "  服务器侧: $OUT/server.p12 (口令 KS_PASS 环境变量，默认 chatnotes) → Ktor SSL 引用"
echo "  手机侧:   $OUT/ca.crt                        → 拷入 app/src/main/res/raw/chatnotes_ca.pem 后重新构建 app"
echo "  有效期 $DAYS 天；到期前重跑: bash gen-certs.sh $HOST $OUT"
