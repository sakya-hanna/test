#!/usr/bin/env bash
# ChatNotes 配置备份/恢复（解决测试/重装后要重新配置 LLM 和向量检索的问题）
#
#   ./device-config.sh backup   从设备拉取非密配置（base_url/model/embed_*）
#   ./device-config.sh restore  恢复配置到设备；若 config-backup/api_key.txt
#                               存在则连同 Key 一起恢复（走 app 的明文迁移
#                               路径，首次启动自动用 Keystore 重新加密）
#
# Key 明文只存在本机 config-backup/（已入 .gitignore），不进仓库。
# 首次使用：在 app 里配置一次，然后把 API Key 粘贴保存到
# config-backup/api_key.txt，以后全靠脚本。
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
BK="$DIR/config-backup"
PKG=com.willam.chatnotes
MODE="${1:-backup}"
mkdir -p "$BK"

if [ "$MODE" = "backup" ]; then
  adb exec-out run-as $PKG cat shared_prefs/config.xml > "$BK/config.xml" 2>/dev/null \
    || { echo "设备上无配置（app 未配置或未安装）"; exit 1; }
  # 剥掉密文字段，只留可长期复用的非密配置
  sed -i '/_encrypted/d' "$BK/config.xml"
  echo "已备份非密配置到 $BK/config.xml："
  grep -o 'name="[^"]*"' "$BK/config.xml" | sort
  [ -f "$BK/api_key.txt" ] && echo "API Key 已在本机保管（api_key.txt）" \
    || echo "提示：把 API Key 保存到 $BK/api_key.txt 即可连 Key 一起恢复"

elif [ "$MODE" = "restore" ]; then
  [ -f "$BK/config.xml" ] || { echo "没有备份（先运行 backup）"; exit 1; }
  KEY=""
  [ -f "$BK/api_key.txt" ] && KEY=$(tr -d '\r\n' < "$BK/api_key.txt")
  # Build the restore prefs with python (sed -i is unreliable in git-bash on Windows).
  # cygpath converts the MSYS path so Windows python can open it.
  BKWIN=$(cygpath -w "$BK" 2>/dev/null || echo "$BK")
  python - "$BKWIN" "$KEY" <<'PYEOF'
import sys, os
bk, key = sys.argv[1], sys.argv[2]
base = open(os.path.join(bk, 'config.xml'), encoding='utf-8').read()
if key:
    esc = key.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
    extra = ('    <string name="api_key">%s</string>\n'
             '    <string name="embed_key">%s</string>\n') % (esc, esc)
    base = base.replace('</map>', extra + '</map>')
open(os.path.join(bk, 'config.restore.xml'), 'w', encoding='utf-8', newline='\n').write(base)
PYEOF
  adb push "$BKWIN\\config.restore.xml" /data/local/tmp/cn-config.xml >/dev/null
  adb exec-out run-as $PKG sh -c 'mkdir -p shared_prefs; cp /data/local/tmp/cn-config.xml shared_prefs/config.xml; rm -f /data/local/tmp/cn-config.xml'
  adb shell am force-stop $PKG
  rm -f "$BK/config.restore.xml"
  if [ -n "$KEY" ]; then echo "配置与 Key 已恢复（app 启动时自动重新加密）"
  else echo "非密配置已恢复；API Key 需在设置里重新输入一次，或先保存到 config-backup/api_key.txt"; fi

else
  echo "用法: $0 [backup|restore]"; exit 1
fi
