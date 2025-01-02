#!/bin/bash

# 检查 JENKINS_HOME 是否设置
if [ -z "$JENKINS_HOME" ]; then
    echo "JENKINS_HOME 环境变量未设置，请检查环境配置"
    exit 1
fi

# 检查是否传入 JAR 包名
if [ $# -lt 1 ]; then
    echo "错误: 缺少参数! 请传入 JAR 包名."
    exit 1
fi

# 获取传入的 JAR 包名
jar_name=$1

# 环境配置
git_repo_url="http://192.168.10.196/CQGSL/src/backend/jeecg-boot.git" # Git仓库路径
branch=develop # Git 分支
API_URL="http://192.168.88.106:29999/deploy/package" # 上传接口地址

# 根项目目录
base_project_path="${JENKINS_HOME}/project/gsl"
# 当前项目名称
project_name="jeecg-boot"
# 当前项目路径
project_dir="${base_project_path}/${branch}/${project_name}"

echo "################################# 当前工作目录: $(pwd) ####################################"

# 1. 检查项目目录是否存在
echo "检查项目目录: $project_dir"
if [ ! -d "$project_dir" ]; then
    echo "项目不存在，准备克隆代码..."

    # 创建项目目录
    mkdir -p "$project_dir" || { echo "错误: 创建目录失败: $project_dir"; exit 1; }

    # 进入项目目录并克隆代码
    cd "$project_dir" || { echo "错误: 进入目录失败: $project_dir"; exit 1; }
    echo "开始克隆 Git 仓库: $git_repo_url, 分支: $branch"
    git clone -b "$branch" "$git_repo_url" . || { echo "错误: Git 克隆失败"; exit 1; }
    echo "成功: Git 克隆完成"
else
    echo "项目已存在，准备更新代码..."
    # 进入项目目录
    cd "$project_dir" || { echo "错误: 进入目录失败: $project_dir"; exit 1; }

    # 拉取最新代码
    echo "拉取最新代码..."
    git pull origin "$branch" || { echo "错误: Git 拉取失败"; exit 1; }
    echo "成功: 代码更新完成"
fi

echo "################################# 开始构建 JAR 包: $jar_name ####################################"

# 2.检查并读取配置文件
config_file="${JENKINS_HOME}/script/jar_config.cfg"

# 检查配置文件是否存在
if [ ! -f "$config_file" ]; then
    echo "错误: 配置文件 '$config_file' 不存在，请检查路径是否正确"
    exit 1
fi

# 查找 JAR 包对应的模块名和目标路径
module_name=""
target_path=""

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
        module_name=$(echo "$V" | awk -F';' '{print $1}')
        target_path=$(echo "$V" | awk -F';' '{print $2}')
        # 找到后跳出循环
        break
    fi

# 从配置文件 ($config_file) 中读取数据
done < "$config_file"

# 检查是否找到了 JAR 包对应的配置
if [ -z "$module_name" ] || [ -z "$target_path" ]; then
    echo "错误: 配置文件中未找到 JAR 包 '$jar_name' 的配置信息"
    exit 1
fi

# 使用 Maven 构建指定模块的 JAR 包
echo "开始构建模块: $module_name"
# Maven 构建命令
mvn clean package -pl "$module_name" -am -Dmaven.test.skip=true

# 检查 Maven 构建是否成功
if [ $? -ne 0 ]; then
    echo "错误: Maven 构建失败"
    exit 1
fi

# 构建完成的提示
echo "成功: Maven 构建完成，模块 '$module_name' 的 JAR 包已生成"

echo "################################# 开始上传 JAR 包: $jar_name ####################################"
# 3. 上传 JAR 包。
# 拼接实际的文件路径，包括 target 目录（使用相对路径）
jar_file_path="$module_name/$target_path/$jar_name"

# 检查jar包是否存在
if [ ! -f "$jar_file_path" ]; then
    echo "错误: 构建后的 JAR 包 '$jar_name' 未找到，路径: $jar_file_path"
    exit 1
fi

# 响应体存储
response_body_file="/tmp/response_body.txt"
# 设置最大请求时间（根据需要修改）
max_time=30
# 获取当前时间戳（毫秒级）
timeStamp=$(date +%s%3N)
# 盐值（用于加密签名）
salt='eOPlbGS1Amn4zpww5ZVfIg=='
# 获取文件大小
file_size=$(stat -c %s "$jar_file_path")

# 拼接字符串并加盐
saltedData="${jar_name}${timeStamp}${file_size}${salt}"

# 生成签名（sign）：计算 MD5 哈希值
sign=$(echo -n "$saltedData" | md5sum | awk '{print $1}')

# 调用接口进行文件上传
echo "准备上传 JAR 包到接口：$API_URL"

# 调用接口进行文件上传
response=$(curl -s -X POST "$API_URL" \
  --header 'Accept: */*' \
  --header 'Connection: keep-alive' \
  --header 'Content-Type: multipart/form-data' \
  --max-time "$max_time" \
  --form "jarName=$jar_name" \
  --form "timeStamp=$timeStamp" \
  --form "ciphertext=$sign" \
  --form "file=@$jar_file_path" \
  --form "fileSize=$file_size" \
  -w "%{http_code}" -o "$response_body_file")

# 获取响应状态码
http_status_code="${response: -3}"

# 获取响应体
response_body=$(cat "$response_body_file")

# 清空响应文件内容
truncate -s 0 "$response_body_file"

# 判断HTTP状态码是否是200
if [[ "$http_status_code" -eq 200 ]]; then
  echo "发布成功！"
else
  echo "发布失败: $response_body"
  exit 1
fi