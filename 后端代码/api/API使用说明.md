# 智慧党建系统API使用说明

## 概述

智慧党建系统提供RESTful API接口，支持用户端和管理端的各种功能操作。

## 基础信息

- **基础URL**: `http://127.0.0.1:8000`
- **认证方式**: JWT Token
- **数据格式**: JSON
- **字符编码**: UTF-8

## 认证相关API

### 1. 用户端登录

**接口地址**: `POST /auth/user/login`

**请求参数**:
```json
{
  "username": "2021001",  // 用户名（学号或身份证号）
  "password": "123456"    // 密码
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "张三",
    "userType": "MEMBER",
    "expiresIn": 7200000
  }
}
```

### 2. 管理端登录

**接口地址**: `POST /auth/admin/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "admin",
    "userType": "ADMIN",
    "expiresIn": 7200000
  }
}
```

### 3. 刷新令牌

**接口地址**: `POST /auth/refresh`

**请求参数**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 4. 用户登出

**接口地址**: `POST /auth/logout`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功"
}
```

## 用户相关API

### 1. 获取用户信息

**接口地址**: `GET /user/info`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "studentId": "2021001",
    "idNumber": "110101199001011234",
    "username": "张三",
    "mobile": "13800138000",
    "politicalStatus": "PARTY_MEMBER",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 2. 更新用户信息

**接口地址**: `PUT /user/info`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求参数**:
```json
{
  "username": "张三",
  "mobile": "13800138000"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功"
}
```

### 3. 修改密码

**接口地址**: `PUT /user/password`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求参数**:
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "密码修改成功"
}
```

## 活动相关API

### 1. 获取活动列表

**接口地址**: `GET /activities`

**请求参数**:
```
page: 1
size: 10
status: ACTIVE
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "title": "主题党日活动",
        "description": "学习党的理论知识",
        "startTime": "2024-01-01T09:00:00",
        "endTime": "2024-01-01T11:00:00",
        "status": "ACTIVE"
      }
    ]
  }
}
```

### 2. 获取活动详情

**接口地址**: `GET /activities/{id}`

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "title": "主题党日活动",
    "description": "学习党的理论知识",
    "startTime": "2024-01-01T09:00:00",
    "endTime": "2024-01-01T11:00:00",
    "status": "ACTIVE",
    "participants": [
      {
        "userId": 1,
        "username": "张三",
        "status": "PARTICIPATED"
      }
    ]
  }
}
```

### 3. 参加活动

**接口地址**: `POST /activities/{id}/join`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "参加成功"
}
```

## 学习相关API

### 1. 获取学习记录

**接口地址**: `GET /study/records`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求参数**:
```
page: 1
size: 10
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 50,
    "list": [
      {
        "id": 1,
        "resourceId": 1,
        "resourceTitle": "党史学习资料",
        "duration": 120,
        "startedAt": "2024-01-01T09:00:00",
        "endedAt": "2024-01-01T11:00:00"
      }
    ]
  }
}
```

### 2. 添加学习记录

**接口地址**: `POST /study/records`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求参数**:
```json
{
  "resourceId": 1,
  "duration": 120,
  "startedAt": "2024-01-01T09:00:00",
  "endedAt": "2024-01-01T11:00:00"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "添加成功"
}
```

## 入党相关API

### 1. 提交入党申请

**接口地址**: `POST /party/application`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求参数**:
```json
{
  "applicationDetails": "申请入党理由和经历",
  "supportingDocuments": ["document1.pdf", "document2.pdf"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "申请提交成功"
}
```

### 2. 获取申请状态

**接口地址**: `GET /party/application/status`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "currentStage": "POSITIVE_MEMBER",
    "status": "PENDING",
    "submittedAt": "2024-01-01T00:00:00"
  }
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 使用示例

### JavaScript示例

```javascript
// 用户登录
const loginData = {
  username: '2021001',  // 学号或身份证号
  password: '123456'
};

const response = await fetch('http://127.0.0.1:8000/auth/user/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(loginData)
});

const result = await response.json();
const accessToken = result.data.accessToken;

// 获取用户信息
const userInfo = await fetch('http://127.0.0.1:8000/user/info', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});
```

### Python示例

```python
import requests

# 用户登录
login_data = {
    'username': '2021001',  # 学号或身份证号
    'password': '123456'
}

response = requests.post('http://127.0.0.1:8000/auth/user/login', json=login_data)
result = response.json()
access_token = result['data']['accessToken']

# 获取用户信息
headers = {'Authorization': f'Bearer {access_token}'}
user_info = requests.get('http://127.0.0.1:8000/user/info', headers=headers)
```

## 注意事项

1. **认证要求**: 除登录接口外，所有接口都需要在请求头中携带有效的JWT Token
2. **Token过期**: Token有效期为2小时，过期后需要使用refreshToken刷新
3. **错误处理**: 请妥善处理各种错误情况，提供友好的用户提示
4. **数据验证**: 前端应进行基本的数据验证，后端会进行严格验证
5. **安全性**: 请确保Token的安全存储和传输
6. **用户名格式**: 用户名可以是学号或身份证号，系统会自动识别

## 更新日志

### v2.1.0 (2024-01-01)
- 简化登录方式：统一使用用户名登录
- 用户名支持学号或身份证号
- 更新接口地址为https://127.0.0.1:8000
- 优化登录逻辑和用户体验

### v2.0.0 (2024-01-01)
- 更新登录方式：支持学号登录和身份证号登录
- 删除微信授权登录
- 优化API响应格式
- 增强安全性

### v1.0.0 (2023-12-01)
- 初始版本发布
- 支持微信授权登录
- 基础功能实现
