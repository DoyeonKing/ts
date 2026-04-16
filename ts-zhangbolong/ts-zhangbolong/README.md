# 小程序框架模板

这是一个包含前后端小程序和Spring Boot框架的空壳模板。

## 项目结构

```
miniprogram_template/
├── springboot/          # Spring Boot后端框架
├── uni_app/            # uni-app小程序前端
├── vue/                # Vue前端（用户端）
└── vue_admin/          # Vue管理后台
```

## 快速开始

### Spring Boot后端

1. 进入目录：`cd springboot`
2. 修改 `src/main/resources/application.yml` 中的数据库配置
3. 运行：`mvn spring-boot:run`
4. 访问：http://localhost:8080/swagger-ui.html 查看API文档

### uni-app小程序

1. 进入目录：`cd uni_app`
2. 安装依赖：`npm install`
3. 运行：`npm run dev:mp-weixin`（微信小程序）
4. 使用HBuilderX或微信开发者工具打开项目

### Vue前端

1. 进入目录：`cd vue`
2. 安装依赖：`npm install`
3. 运行：`npm run dev`
4. 访问：http://localhost:5173

### Vue管理后台

1. 进入目录：`cd vue_admin`
2. 安装依赖：`npm install`
3. 运行：`npm run dev`
4. 访问：http://localhost:5173

## 技术栈

- **后端**: Spring Boot 3.2.0, Spring Security, JWT, MySQL, JPA
- **小程序**: uni-app, Vue 2
- **前端**: Vue 3, Element Plus, Pinia, Vue Router, Vite

## 注意事项

1. 请根据实际需求修改数据库配置
2. 修改JWT密钥（在application.yml中配置）
3. 根据实际业务需求添加业务代码

