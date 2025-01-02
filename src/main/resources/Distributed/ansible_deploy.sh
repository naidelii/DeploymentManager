#!/bin/bash
# 设置中文环境
export LANG="zh_CN.UTF-8"

# 检查是否传入 JAR 包名和路径参数
if [ $# -lt 2 ]; then
    echo "错误: 缺少参数! 请传入 JAR 包名和 JAR 包所在路径."
    exit 1
fi

# 获取传入的 JAR 包名和路径
jar_name="$1"
jar_path="$2"


# 配置文件路径，包含 JAR 包的配置信息
config_file="./deploy_config.cfg"
# 检查配置文件是否存在
if [ ! -f "$config_file" ]; then
    echo "错误: 配置文件 '$config_file' 不存在，请检查路径是否正确"
    exit 1
fi

# 初始化变量，用于存储匹配的 IP 地址和服务名称
deploy_ips=""
deploy_service=""

# 从配置文件中读取并查找 JAR 包的配置信息（逐行读取）
while IFS='=' read -r K V; do
    # 跳过注释行（以 '#' 开头的行）
    if [[ "$K" =~ ^# ]]; then
        continue
    fi

    # 去除空格并匹配 JAR 包名称
    K=$(echo "$K" | xargs)
    V=$(echo "$V" | xargs)

    # 找到匹配的 JAR 包（jar_name相同）
    if [ "$K" == "$jar_name" ]; then
        # 使用 awk 分割模块路径和目标路径
        deploy_ips=$(echo "$V" | awk -F';' '{print $1}')
        deploy_service=$(echo "$V" | awk -F';' '{print $2}')
        # 找到后跳出循环
        break
    fi

# 从配置文件 ($config_file) 中读取数据
done < "$config_file"

# 检查是否找到了 JAR 包对应的配置
if [ -z "$deploy_ips" ] || [ -z "$deploy_service" ]; then
    echo "错误: 配置文件中未找到 JAR 包 '$jar_name' 的配置信息"
    exit 1
fi

# 构造 ansible-playbook 命令，用于部署 JAR 包到指定的机器
ansible_playbook="./deploy_jar.yml"
ans_commd="ansible-playbook $ansible_playbook -e \"hosts=$deploy_ips jar_name=$jar_name jar_path=$jar_path deploy_service=$deploy_service\""

# 输出生成的 ansible-playbook 命令，供调试时查看
echo "执行的 ansible-playbook 命令: $ans_commd"

# 执行命令
eval $ans_commd
if [ $? -eq 0 ]; then
    echo "发布成功！"
else
    echo "错误: JAR 包 '$jar_name' 发布失败!"
    exit 1
fi