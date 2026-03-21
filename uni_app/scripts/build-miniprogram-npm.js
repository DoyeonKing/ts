/**
 * 从 node_modules 复制 threejs-miniprogram 等包到 miniprogram_npm。
 * 使用：HBuilderX 运行到微信后，在 uni_app 根目录执行 node scripts/build-miniprogram-npm.js
 * 也可在微信开发者工具中直接：工具 → 构建 npm。
 */
const path = require('path');
const fs = require('fs');

const root = path.resolve(__dirname, '..');
const mpDir = path.join(root, 'unpackage', 'dist', 'dev', 'mp-weixin');
const miniprogramNpm = path.join(mpDir, 'miniprogram_npm');
const sourceModules = path.join(root, 'node_modules');

if (!fs.existsSync(mpDir)) {
  console.error('小程序输出目录不存在。请先在 HBuilderX 中运行到微信开发者工具。');
  process.exit(1);
}

function copyDirRecursive(src, dest) {
  if (!fs.existsSync(src)) return;
  fs.mkdirSync(dest, { recursive: true });
  for (const name of fs.readdirSync(src)) {
    const srcPath = path.join(src, name);
    const destPath = path.join(dest, name);
    const stat = fs.statSync(srcPath);
    if (stat.isDirectory()) {
      copyDirRecursive(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

const packages = [{ name: 'threejs-miniprogram' }];

fs.mkdirSync(miniprogramNpm, { recursive: true });

for (const p of packages) {
  const src = path.join(sourceModules, p.name);
  const dest = path.join(miniprogramNpm, p.name);
  if (!fs.existsSync(src)) {
    console.warn('跳过（未安装）:', p.name);
    continue;
  }
  console.log('复制:', p.name);
  copyDirRecursive(src, dest);
}

console.log('miniprogram_npm 已就绪。请在微信开发者工具中重新编译。');
process.exit(0);
