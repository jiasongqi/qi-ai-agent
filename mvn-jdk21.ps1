# ============================================
# 项目专用 JDK 21 Maven 运行脚本
# ============================================
# 用法:
#   .\mvn-jdk21.ps1 compile          # 编译
#   .\mvn-jdk21.ps1 spring-boot:run  # 运行项目
#   .\mvn-jdk21.ps1 test             # 运行测试
#   .\mvn-jdk21.ps1 clean package    # 打包
# ============================================

$JDK21_HOME = "C:/Program Files/Microsoft/jdk-21.0.10.7-hotspot"

# 验证 JDK 21 是否存在
if (-not (Test-Path "$JDK21_HOME/bin/java.exe")) {
    Write-Error "错误: 未找到 JDK 21，请确认路径: $JDK21_HOME"
    exit 1
}

# 仅当前进程设置环境变量，不影响系统 PATH
$env:JAVA_HOME = $JDK21_HOME
$env:PATH = "$JDK21_HOME/bin;$env:PATH"

# 显示当前使用的 Java 版本
Write-Host "========================================" -ForegroundColor Green
Write-Host "使用 JDK 21 运行 Maven" -ForegroundColor Green
& "$JDK21_HOME/bin/java.exe" -version 2>&1 | ForEach-Object { Write-Host $_ -ForegroundColor Cyan }
Write-Host "========================================" -ForegroundColor Green

# 执行 Maven 命令
if ($args.Count -eq 0) {
    Write-Host "提示: 请传入 Maven 目标，例如: .\mvn-jdk21.ps1 compile" -ForegroundColor Yellow
    .\mvnw.cmd --version
} else {
    .\mvnw.cmd @args
}
