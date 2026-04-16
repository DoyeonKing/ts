/**
 * 把小程序需要的 package.json 复制到 unpackage/dist/dev/mp-weixin，
 * 并在该目录执行 npm install，这样微信开发者工具「构建 npm」才能找到依赖。
 * 使用：在 uni_app 根目录执行 node scripts/prepare-mp-npm.js
 * 或在先 npm run build:mp-weixin 后执行 npm run prepare-mp-npm
 */
const path = require('path');
const fs = require('fs');
const { execSync } = require('child_process');

const root = path.resolve(__dirname, '..');
const mpDir = path.join(root, 'unpackage', 'dist', 'dev', 'mp-weixin');
const srcPkg = path.join(root, 'scripts', 'mp-weixin-package.json');
const destPkg = path.join(mpDir, 'package.json');

if (!fs.existsSync(mpDir)) {
  console.error('小程序输出目录不存在，请先执行: npm run build:mp-weixin');
  process.exit(1);
}

if (!fs.existsSync(srcPkg)) {
  console.error('找不到 scripts/mp-weixin-package.json');
  process.exit(1);
}

fs.copyFileSync(srcPkg, destPkg);
console.log('已复制 package.json 到', mpDir);

console.log('正在执行 npm install ...');
execSync('npm install', { cwd: mpDir, stdio: 'inherit', shell: true });

function addIndexIfNeeded(pkgDir, logPrefix) {
  const pkgPath = path.join(pkgDir, 'package.json');
  if (!fs.existsSync(pkgPath)) return;
  const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
  const main = pkg.main;
  if (!main || main === 'index.js') return;
  const indexPath = path.join(pkgDir, 'index.js');
  const content = `module.exports = require('./${main.replace(/\\/g, '/')}');\n`;
  fs.writeFileSync(indexPath, content, 'utf8');
  console.log(logPrefix || '已生成 index.js:', path.relative(mpDir, indexPath));
}

function patchDir(nodeModulesDir, logPrefix) {
  if (!fs.existsSync(nodeModulesDir)) return;
  const threejs = path.join(nodeModulesDir, 'threejs-miniprogram');
  if (fs.existsSync(threejs)) addIndexIfNeeded(threejs, logPrefix);
}

patchDir(path.join(mpDir, 'node_modules'), '[node_modules] 已生成 index.js: ');

const miniprogramNpm = path.join(mpDir, 'miniprogram_npm');
if (fs.existsSync(miniprogramNpm)) {
  console.log('检测到 miniprogram_npm，正在补写 index.js ...');
  patchDir(miniprogramNpm, '[miniprogram_npm] 已生成 index.js: ');
}

console.log('完成。若尚未构建 npm，请到微信开发者工具中：工具 → 构建 npm。');
process.exit(0);
