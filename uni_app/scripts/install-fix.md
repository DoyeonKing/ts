# npm install 失败修复步骤

## 原因简述
- **Invalid Version**：`package.json` 或 `package-lock.json` 里存在无效版本（如 `"*"`、或依赖里缺少 version）
- **EBUSY/EPERM**：其他程序（IDE、终端、杀毒）占用了 `node_modules` 里的文件
- **esbuild EFTYPE**：esbuild 的 Windows 可执行文件被拦截或损坏（常见于杀毒软件）

## 步骤 0：若报 “Invalid Version”
1. 确保已删除 `package-lock.json`（若存在）
2. 在项目根目录执行**完整清理后再装**（见步骤 2）

## 步骤 1：释放文件占用
1. 关闭 **微信开发者工具**
2. 关闭 **Cursor / VS Code**（或至少关闭 uni_app 文件夹）
3. 结束所有在 `uni_app` 下运行的终端（停止 `npm run dev:mp-weixin` 等）
4. 如装了 **360、火绒、Windows Defender**：临时关闭实时保护，或将项目目录加入白名单

## 步骤 2：清理后重装（在 PowerShell 里执行）

```powershell
cd D:\Desktop\thearter\version_01\uni_app

# 删除依赖和锁文件
Remove-Item -Recurse -Force node_modules -ErrorAction SilentlyContinue
Remove-Item -Force package-lock.json -ErrorAction SilentlyContinue

# 重新安装
npm install
```

若仍报 esbuild 相关错误，可先跳过安装脚本再试运行：

```powershell
npm install --ignore-scripts
npm run dev:mp-weixin
```

若项目能正常编译运行，可暂时不处理 esbuild；若构建报错缺少 esbuild，再往下看。

## 步骤 3：esbuild 仍失败时
1. 在 **Windows 安全中心** → 病毒和威胁防护 → 保护历史记录，看是否隔离了 `esbuild.exe`，有则恢复并添加排除项
2. 或单独重装 esbuild：
   ```powershell
   npm install esbuild --save-dev --ignore-scripts
   npm rebuild esbuild
   ```

## 步骤 4：仍不行时
把项目移到**非 OneDrive/网盘同步**的纯本机路径（如 `C:\dev\uni_app`）再执行步骤 2，有时同步盘会导致 exe 被锁或损坏。

---

## 报错 @oasis-engine 找不到（ENOENT miniprogram_npm/@oasis-engine/...）

项目已从 Oasis Engine 改为 Three.js（threejs-miniprogram），若微信开发者工具仍尝试加载旧的 `@oasis-engine`，说明构建产物里有残留。

**操作步骤（请先关闭微信开发者工具）：**

1. 在项目根目录执行：
   ```powershell
   cd D:\Desktop\thearter\version_01\uni_app
   npm run clean-mp-npm
   ```
   会删除 `unpackage/dist/dev/mp-weixin/miniprogram_npm` 和该目录下的 `node_modules`。

2. 若仍报同样错误，做**完整清理**（会删掉整个 mp-weixin 输出）：
   ```powershell
   npm run clean-mp-full
   ```
   然后重新在 HBuilderX 中 **运行 → 运行到小程序模拟器 → 微信开发者工具**。

3. 在微信开发者工具中 **工具 → 构建 npm**（此时只会安装 threejs-miniprogram，不再有 oasis）。

---

## 微信小程序「构建 npm」报 NPM packages not found 时

小程序要求 `package.json` 和 `node_modules` 在**小程序根目录**（即 `unpackage/dist/dev/mp-weixin`）内，uni-app 编译不会自动拷贝，需要先准备。

### 为什么 HBuilderX 一构建，微信里构建的 npm 就没了？

用 HBuilderX **运行 → 运行到小程序模拟器 → 微信开发者工具** 时，会**重新生成整个** `unpackage/dist/dev/mp-weixin` 目录，所以里面之前的 `package.json`、`node_modules` 和 **构建 npm** 生成的 `miniprogram_npm` 都会被清掉。这是预期行为，无法关闭。

所以只要用 HBuilderX「运行到微信」，**每次运行完后**都要补做下面两步，否则就会报 NPM 找不到或「缺少插件」。

---

### 方案 A：坚持用 HBuilderX「运行到微信」

每次 **HBuilderX 运行到微信** 完成后，按顺序做：

1. **在项目根目录执行**（Cursor 终端或 PowerShell）：
   ```powershell
   cd D:\Desktop\thearter\version_01\uni_app
   npm run prepare-mp-npm
   ```

2. **在微信开发者工具里**：菜单 **工具 → 构建 npm**。

3. 建议再 **项目 → 重新打开此项目**，然后预览/调试。

每次重新用 HBuilderX 运行到微信，都要重复 1、2（和可选的 3）。

---

### 方案 B：改用命令行构建（推荐，少重复操作）

不用 HBuilderX「运行到微信」，改用一条命令完成「编译 + 准备 npm」：

1. **在项目根目录执行**：
   ```powershell
   cd D:\Desktop\thearter\version_01\uni_app
   npm run weixin-build
   ```
   会先执行 `uni build --platform mp-weixin`，再自动执行 `prepare-mp-npm`。

2. **用微信开发者工具** 打开目录：`unpackage/dist/dev/mp-weixin`。

3. 在微信里 **工具 → 构建 npm**，之后正常预览/调试。

只有当你改完代码需要重新编译时，再执行一次 `npm run weixin-build`，然后再在微信里 **构建 npm**。平时只改页面、不重新编译时，不用再跑这两步。

---

### 构建 npm 成功仍提示「缺少插件」时

1. **看提示出现的位置**  
   - 若是**打开项目或构建时**在工具里提示：多半是 `project.config.json` 里 npm 相关配置没生效。请用 **HBuilderX 再运行一次到微信**（或再跑一次 `npm run weixin-build`），确保 `manifest.json` 里 mp-weixin 的 `nodeModules: true`、`packNpmManually` 被带到生成的 `project.config.json`，然后**再**在微信里 **工具 → 构建 npm**。  
   - 若是**运行到 3D 选座页**时在模拟器/真机里提示：可能是基础库版本不够（WebGL 需基础库 2.7.0+）。在微信开发者工具 **详情 → 本地设置** 里把 **调试基础库** 选成 2.7.0 或更高，或到微信公众平台后台设置最低基础库版本。

2. **确认顺序**  
   一定是：先有 `package.json` + `node_modules`（即先执行过 `prepare-mp-npm` 或 `weixin-build`）→ 再在微信里 **构建 npm** → 最后 **项目 → 重新打开此项目** 再试。

3. **仍不行**  
   把「缺少插件」的**完整原话**或截图（含是工具里还是真机/模拟器、哪一步出现）发出来，便于对症改配置或代码。
   cd D:\Desktop\thearter\version_01\uni_app
   npm run prepare-mp-npm