@echo off
chcp 65001 >nul
echo 正在删除微信小程序构建目录（请确保已关闭微信开发者工具）...
set "MP=%~dp0unpackage\dist\dev\mp-weixin"
if exist "%MP%" (
    rd /s /q "%MP%"
    if exist "%MP%" (
        echo 删除失败，可能被占用。请关闭微信开发者工具后重试。
        pause
        exit /b 1
    )
    echo 已删除。请重新在 HBuilderX 中「运行到微信」或执行 npm run build:mp-weixin，再执行 npm run prepare-mp-npm，最后在微信里「构建 npm」。
) else (
    echo 目录不存在，无需删除。
)
pause
