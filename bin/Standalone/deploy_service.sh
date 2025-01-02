#!/bin/bash

# 检查是否传入 JAR 包名和路径参数
if [ $# -lt 2 ]; then
    echo "错误: 缺少参数! 请传入 JAR 包名和 JAR 包所在路径."
    exit 1
fi

# 获取传入的 JAR 包名和路径
jar_name="$1"
jar_path="$2"

# 检查 JAR 包文件是否存在
jar_file="$jar_path/$jar_name"
if [ ! -f "$jar_file" ]; then
    echo "错误: JAR 文件 $jar_file 不存在."
    exit 1
fi

echo "准备备份 JAR 文件: $jar_name"
# 备份目录为 JAR 文件所在路径下的 backup 目录
backup_dir="$jar_path/backup"

# 创建备份目录（如果不存在）
mkdir -p "$backup_dir"
if [ $? -ne 0 ]; then
    echo "错误: 创建备份目录失败: $backup_dir"
    exit 1
fi

# 获取当前时间戳，用于备份文件名
update_date=$(date +%Y%m%d_%H%M%S)

# 使用时间戳命名备份文件
backup_file="$backup_dir/$jar_name-$update_date"

# 进行备份
cp "$jar_file" "$backup_file"

# 检查备份是否成功
if [ $? -eq 0 ]; then
    echo "备份成功"
else
    echo "错误: JAR 文件备份失败: $backup_file"
    exit 1
fi

# 提取服务名称，去除 .jar 后缀
service_name=$(basename "$jar_name" .jar)
echo "提取的服务名称: $service_name"

# 检查对应的 service 文件是否存在
service_file="/usr/lib/systemd/system/$service_name.service"
if [ ! -f "$service_file" ]; then
    echo "错误: 服务文件 $service_file 不存在."
    exit 1
fi

# 重启相关服务（先停止，再启动）
echo "正在重启 $service_name 服务..."
systemctl restart "$service_name"

# 检查服务重启是否成功
if [ $? -eq 0 ]; then
    echo "$service_name 服务重启成功."
else
    echo "$service_name 服务重启失败."
    exit 1
fi