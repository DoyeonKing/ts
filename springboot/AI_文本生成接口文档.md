# AI 文本生成接口文档（成员 B / Day1）

## 1. 接口信息

- 接口名称：AI 文本生成
- 请求方式：`POST`
- 请求地址：`/api/ai/generate-text`
- 完整地址示例：`http://localhost:8080/api/ai/generate-text`
- 鉴权：当前无需登录（已在安全配置中放行 `/api/ai/**`）
- 数据格式：`application/json`

## 2. 请求参数

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `type` | `string` | 是 | 生成类型，只能是 `review`、`plot`、`actor` |
| `playId` | `number` | 否 | 关联剧目 ID |
| `userInput` | `string` | 否 | 用户补充描述（建议写清楚你的感受和偏好） |

### 参数校验规则

- `type` 不能为空
- `type` 仅允许：`review|plot|actor`

## 3. 请求示例

### 3.1 观后感（review）

```json
{
  "type": "review",
  "playId": 3,
  "userInput": "哈利波特很梦幻，很神奇，我特别喜欢魔法世界"
}
```

### 3.2 剧情分析（plot）

```json
{
  "type": "plot",
  "playId": 4,
  "userInput": "我想从主题和人物成长角度看这部剧"
}
```

### 3.3 演员评价（actor）

```json
{
  "type": "actor",
  "playId": 1,
  "userInput": "更关注台词感染力和舞台表现"
}
```

## 4. 响应结构

统一响应包装：

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "text": "生成结果文本",
    "mock": false,
    "mockReason": null
  }
}
```

`data` 字段说明：

| 字段 | 类型 | 说明 |
|---|---|---|
| `text` | `string` | 生成结果文本 |
| `mock` | `boolean` | `true`=走演示文案；`false`=真实调用大模型 |
| `mockReason` | `string/null` | 当 `mock=true` 时给出原因（例如：`app.ai.llm.api-key 为空`） |

## 5. 成功响应示例

### 5.1 真实模型返回（mock=false）

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "text": "这部作品最打动人的地方在于...",
    "mock": false,
    "mockReason": null
  }
}
```

### 5.2 演示文案返回（mock=true）

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "text": "【演示文案】...",
    "mock": true,
    "mockReason": "app.ai.llm.api-key 为空"
  }
}
```

## 6. 常见问题与排查

### 6.1 为什么一直是 `mock: true`？

优先检查配置诊断接口：

- `GET /api/ai/config-status`
- 示例地址：`http://localhost:8080/api/ai/config-status`

关键字段：

- `hasApiKey`
- `hasBaseUrl`
- `hasModel`
- `missing`

只要其中任意一项缺失，`generate-text` 就会走 mock。

### 6.2 JSON 解析报错（`JSON parse error`）

通常是请求体格式错误：

- 字段之间漏了逗号
- 字符串没用英文双引号
- 把中文直接写在数字字段后面

## 7. 调用示例（curl）

```bash
curl -X POST "http://localhost:8080/api/ai/generate-text" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "review",
    "playId": 3,
    "userInput": "哈利波特很梦幻，很神奇，我特别喜欢魔法世界"
  }'
```

## 8. 版本信息

- 文档版本：v1.0
- 维护人：成员 B
- 更新时间：2026-03-23
