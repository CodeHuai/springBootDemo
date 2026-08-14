<#
.SYNOPSIS
    在本多模块工程里一键新建一个 Spring Boot 子模块。

.DESCRIPTION
    传入模块名,自动生成符合项目约定的完整骨架:
      - <模块名>/pom.xml            parent 指向 spring-boot-demo,不带 groupId、不带依赖版本
      - 启动类 / 测试类              包名 com.huai.<模块名>,风格对齐 helloworld_demo
      - application.yml              端口自动递增,避免与已有模块冲突
    并把 <module> 追加到根 pom.xml 的 <modules> 中。

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File scripts\new-module.ps1 user_demo
#>
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Name
)

$ErrorActionPreference = 'Stop'

# 项目根目录 = 脚本所在 scripts/ 的上一级
$Root    = Split-Path -Parent $PSScriptRoot
$RootPom = Join-Path $Root 'pom.xml'

if (-not (Test-Path $RootPom)) {
    throw "未在项目根找到 pom.xml: $RootPom (请把脚本放在项目的 scripts\ 目录下运行)"
}

# 1. 校验模块名:小写字母/数字/下划线,字母开头(与 helloworld_demo 风格一致)
if ($Name -notmatch '^[a-z][a-z0-9_]*$') {
    throw "模块名只能用小写字母、数字、下划线,且必须以字母开头(参考 helloworld_demo)。当前传入: $Name"
}

$ModuleDir = Join-Path $Root $Name
if (Test-Path $ModuleDir) {
    throw "目录已存在,为避免覆盖已拒绝: $ModuleDir"
}

$rootPomContent = Get-Content $RootPom -Raw
if ($rootPomContent -match "<module>$Name</module>") {
    throw "根 pom.xml 里已存在同名 module,为避免重复已拒绝: $Name"
}

# 2. 由模块名推导启动类名:xxx_demo -> XxxDemo -> XxxDemoApplication
$Pascal   = -join (($Name -split '_') | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) })
$AppClass = "${Pascal}Application"

# 3. 自动选端口:扫描各模块的 application.yml,取已用最大端口 + 1
$usedPorts = @()
Get-ChildItem -Path $Root -Directory | ForEach-Object {
    $yml = Join-Path $_.FullName 'src\main\resources\application.yml'
    if (Test-Path $yml) {
        $text = Get-Content $yml -Raw
        foreach ($m in [regex]::Matches($text, '(?m)^\s*port:\s*(\d+)')) {
            $usedPorts += [int]$m.Groups[1].Value
        }
    }
}
$Port = if ($usedPorts.Count -gt 0) { ($usedPorts | Measure-Object -Maximum).Maximum + 1 } else { 10086 }

# 4. 创建目录结构
$PkgDir     = Join-Path $ModuleDir "src\main\java\com\huai\$Name"
$TestPkgDir = Join-Path $ModuleDir "src\test\java\com\huai\$Name"
$ResDir     = Join-Path $ModuleDir 'src\main\resources'
New-Item -ItemType Directory -Path $PkgDir, $TestPkgDir, $ResDir -Force | Out-Null

# 无 BOM 的 UTF-8 写文件(与项目现有文件编码一致,避免 Maven 读取 XML 时因 BOM 报错)
function Write-File($Path, $Content) {
    $enc = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $enc)
}

# 5. pom.xml
$pom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <artifactId>$Name</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>$Name</name>
    <description>$Name 模块</description>

    <parent>
        <groupId>com.huai</groupId>
        <artifactId>spring-boot-demo</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>$Name</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
"@
Write-File (Join-Path $ModuleDir 'pom.xml') $pom

# 6. 启动类
$app = @"
package com.huai.$Name;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class $AppClass {

    public static void main(String[] args) {
        SpringApplication.run($AppClass.class, args);
    }

}
"@
Write-File (Join-Path $PkgDir "$AppClass.java") $app

# 7. 测试类
$test = @"
package com.huai.$Name;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ${AppClass}Tests {

    @Test
    void contextLoads() {
    }

}
"@
Write-File (Join-Path $TestPkgDir "${AppClass}Tests.java") $test

# 8. application.yml
$yml = @"
server:
  port: $Port
  servlet:
    context-path: /$Name
"@
Write-File (Join-Path $ResDir 'application.yml') $yml

# 9. 把 <module> 追加到根 pom 的 <modules>(正则吃掉 </modules> 前的空白,保证缩进统一)
$replacement    = "`r`n        <module>$Name</module>`r`n    </modules>"
$rootPomContent = [regex]::Replace($rootPomContent, '\r?\n[ \t]*</modules>', $replacement)
Write-File $RootPom $rootPomContent

# 10. 完成
Write-Host ""
Write-Host "== 模块 [$Name] 已生成 ==" -ForegroundColor Green
Write-Host ("   启动类 : com.huai.{0}.{1}" -f $Name, $AppClass)
Write-Host ("   端口   : {0}  (context-path: /{1})" -f $Port, $Name)
Write-Host ("   位置   : {0}" -f $ModuleDir)
Write-Host ""
Write-Host "下一步: 回到 IDEA,在 Maven 工具窗点击 [刷新 / Reload All Maven Projects],新模块即可被识别。"
Write-Host ""
