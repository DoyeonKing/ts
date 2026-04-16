/**
 * 清除微信小程序构建产物中的 Oasis 残留，解决报错：
 *   ENOENT: ... miniprogram_npm/@oasis-engine/loader/dist/module.js
 * 使用：在 uni_app 根目录执行 node scripts/clean-mp-weixin-npm.js
 * 然后重新：运行到微信开发者工具 → 在工具内「构建 npm」。
 *
 * 参数：--full 会删除整个 mp-weixin 输出目录，强制下次完整重新编译（推荐若仍报错时使用）。
 */
const path = require('path');
const fs = require('fs');

const fullClean = process.argv.includes('--full');
const root = path.resolve(__dirname, '..');
const mpDir = path.join(root, 'unpackage', 'dist', 'dev', 'mp-weixin');
const miniprogramNpm = path.join(mpDir, 'miniprogram_npm');
const mpNodeModules = path.join(mpDir, 'node_modules');

function rmDirRecursive(dir) {
  if (!fs.existsSync(dir)) return;
  for (const name of fs.readdirSync(dir)) {
    const full = path.join(dir, name);
    const stat = fs.statSync(full);
    if (stat.isDirectory()) rmDirRecursive(full);
    else fs.unlinkSync(full);
  }
  fs.rmdirSync(dir);
  console.log('已删除:', path.relative(root, dir));
}

if (fullClean && fs.existsSync(mpDir)) {
  rmDirRecursive(mpDir);
  console.log('已完整清理 mp-weixin 输出。请重新在 HBuilderX 中「运行 → 运行到小程序模拟器 → 微信开发者工具」。');
  process.exit(0);
}

if (fs.existsSync(miniprogramNpm)) {
  rmDirRecursive(miniprogramNpm);
} else {
  console.log('miniprogram_npm 不存在，无需删除');
}

if (fs.existsSync(mpNodeModules)) {
  rmDirRecursive(mpNodeModules);
} else {
  console.log('mp-weixin/node_modules 不存在，无需删除');
}

console.log('清理完成。请重新运行到微信开发者工具，并在工具内点击「工具 → 构建 npm」。');
process.exit(0);
