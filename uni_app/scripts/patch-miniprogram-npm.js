/**
 * 在 miniprogram_npm 里为没有 index.js 的包补写 index.js（微信会固定找 index.js）。
 * 在微信开发者工具里点完「构建 npm」后，在 uni_app 根目录执行：node scripts/patch-miniprogram-npm.js
 */
const path = require('path');
const fs = require('fs');

const root = path.resolve(__dirname, '..');
const mpDir = path.join(root, 'unpackage', 'dist', 'dev', 'mp-weixin');
const miniprogramNpm = path.join(mpDir, 'miniprogram_npm');

if (!fs.existsSync(miniprogramNpm)) {
  console.error('miniprogram_npm 不存在。请先在微信开发者工具中：工具 → 构建 npm。');
  process.exit(1);
}

function addIndexIfNeeded(pkgDir) {
  const pkgPath = path.join(pkgDir, 'package.json');
  if (!fs.existsSync(pkgPath)) return;
  const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
  const main = pkg.main;
  if (!main || main === 'index.js') return;
  const indexPath = path.join(pkgDir, 'index.js');
  const content = `module.exports = require('./${main.replace(/\\/g, '/')}');\n`;
  fs.writeFileSync(indexPath, content, 'utf8');
  console.log('已生成 index.js:', path.relative(mpDir, indexPath));
}

// threejs-miniprogram 等：若 main 不是 index.js 则补写
const threejs = path.join(miniprogramNpm, 'threejs-miniprogram');
if (fs.existsSync(threejs)) addIndexIfNeeded(threejs);

console.log('miniprogram_npm 已补全 index.js，请重新编译或刷新开发者工具。');
process.exit(0);
