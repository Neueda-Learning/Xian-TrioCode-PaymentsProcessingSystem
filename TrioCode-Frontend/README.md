# TrioCode-Frontend

基于 **Vue 3 + Vite + Vue Router + Element Plus** 构建的支付处理系统前端项目。
Frontend for the TrioCode Payments Processing System, built with **Vue 3 + Vite + Vue Router + Element Plus**.

---

## 目录 / Table of Contents

- [技术栈 / Tech Stack](#技术栈--tech-stack)
- [环境要求 / Prerequisites](#环境要求--prerequisites)
- [克隆并运行项目 / Clone & Run the Project](#克隆并运行项目--clone--run-the-project)
- [项目结构 / Project Structure](#项目结构--project-structure)
- [常用命令 / Available Scripts](#常用命令--available-scripts)
- [与后端联调 / Connecting to the Backend](#与后端联调--connecting-to-the-backend)
- [推荐 IDE 配置 / Recommended IDE Setup](#推荐-ide-配置--recommended-ide-setup)
- [常见问题 / Troubleshooting](#常见问题--troubleshooting)

---

## 技术栈 / Tech Stack

| 分类 / Category | 技术 / Technology |
| --- | --- |
| 框架 / Framework | Vue 3 (`<script setup>`) |
| 构建工具 / Build Tool | Vite |
| 路由 / Routing | Vue Router 4 |
| UI 组件库 / UI Library | Element Plus |
| HTTP 请求 / HTTP Client | Axios |

---

## 环境要求 / Prerequisites

**中文：**
- 安装 [Node.js](https://nodejs.org/)（建议 18.x 或更高版本，推荐使用 LTS 版本）
- 安装 npm（随 Node.js 一同安装）或其他包管理器（yarn / pnpm）
- 安装 [Git](https://git-scm.com/)

**English:**
- Install [Node.js](https://nodejs.org/) (v18.x or later recommended; LTS version preferred)
- npm is bundled with Node.js (or use yarn / pnpm if you prefer)
- Install [Git](https://git-scm.com/)

验证安装 / Verify installation:

```sh
node -v
npm -v
```

---

## 克隆并运行项目 / Clone & Run the Project

### 1. 克隆仓库 / Clone the repository

```sh
git clone <仓库地址 / repository-url>
cd Xian-TrioCode-PaymentsProcessingSystem/TrioCode-Frontend
```

### 2. 安装依赖 / Install dependencies

**中文：** 进入 `TrioCode-Frontend` 目录后，执行以下命令安装所有依赖包：
**English:** From inside the `TrioCode-Frontend` directory, install all dependencies:

```sh
npm install
```

### 3. 启动开发服务器 / Start the development server

```sh
npm run dev
```

**中文：** 启动成功后，终端会显示本地访问地址（默认是 `http://localhost:5173`），在浏览器中打开该地址即可查看项目。
**English:** Once started, the terminal will print a local URL (default `http://localhost:5173`). Open it in your browser to view the app.

### 4. 构建生产版本 / Build for production

```sh
npm run build
```

**中文：** 构建产物将输出到 `dist/` 目录，可直接部署到静态资源服务器（如 Nginx）。
**English:** The production build output is generated in the `dist/` directory and can be deployed to any static file server (e.g., Nginx).

### 5. 本地预览生产构建 / Preview the production build locally

```sh
npm run preview
```

---

## 项目结构 / Project Structure

```
TrioCode-Frontend/
├── public/               # 静态资源 / Static assets
├── src/
│   ├── api/              # 接口请求模块 / API request modules
│   ├── assets/           # 样式与图片资源 / Styles & images
│   ├── components/       # 公共组件 / Shared components
│   ├── router/           # 路由配置 / Vue Router configuration
│   ├── utils/            # 工具方法（如 axios 封装）/ Utilities (e.g. axios wrapper)
│   ├── views/            # 页面级组件 / Page-level views
│   ├── App.vue           # 根组件 / Root component
│   └── main.js           # 应用入口 / App entry point
├── .env.development      # 开发环境变量 / Development env variables
├── .env.production       # 生产环境变量 / Production env variables
├── vite.config.js        # Vite 配置（含开发代理）/ Vite config (incl. dev proxy)
└── package.json
```

---

## 常用命令 / Available Scripts

| 命令 / Command | 说明 (中文) | Description (English) |
| --- | --- | --- |
| `npm install` | 安装项目依赖 | Install project dependencies |
| `npm run dev` | 启动开发服务器（热更新） | Start dev server with hot-reload |
| `npm run build` | 构建生产环境代码 | Build for production |
| `npm run preview` | 本地预览生产构建结果 | Preview the production build locally |

---

## 与后端联调 / Connecting to the Backend

**中文：**
本项目通过 [`src/utils/request.js`](src/utils/request.js) 中封装的 axios 实例发起请求，`baseURL` 由环境变量 `VITE_API_BASE_URL` 控制（默认 `/api`）。
开发模式下，[`vite.config.js`](vite.config.js) 已配置代理，会将 `/api` 开头的请求转发到 `http://localhost:8080`（即 `TrioCode-Backend` 默认端口）。
请确保后端服务（`TrioCode-Backend`）已启动后再进行联调测试。

**English:**
Requests are sent through the axios instance defined in [`src/utils/request.js`](src/utils/request.js). The `baseURL` is controlled by the `VITE_API_BASE_URL` environment variable (defaults to `/api`).
In development mode, [`vite.config.js`](vite.config.js) proxies any request starting with `/api` to `http://localhost:8080` (the default port for `TrioCode-Backend`).
Make sure the backend (`TrioCode-Backend`) is running before testing integrated features.

---

## 推荐 IDE 配置 / Recommended IDE Setup

**中文：** [VS Code](https://code.visualstudio.com/) + [Vue - Official 插件](https://marketplace.visualstudio.com/items?itemName=Vue.volar)（请禁用 Vetur 插件）。
**English:** [VS Code](https://code.visualstudio.com/) + [Vue - Official extension](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (disable Vetur if installed).

---

## 常见问题 / Troubleshooting

**中文：**
- **`npm install` 报错 `EBADENGINE` 警告：** 该警告仅提示 Node.js 版本低于推荐版本，通常不影响使用；如遇到实际问题，请升级 Node.js 到较新的 LTS 版本。
- **端口被占用：** 可在 [`vite.config.js`](vite.config.js) 的 `server.port` 中修改开发服务器端口。
- **接口请求 404 / 跨域问题：** 请确认后端服务已启动，并检查 `vite.config.js` 中的代理目标地址（`server.proxy['/api'].target`）与后端实际端口一致。

**English:**
- **`EBADENGINE` warning during `npm install`:** This only indicates your Node.js version is below the recommended range; it usually doesn't break functionality. Upgrade to a recent LTS Node.js version if you encounter real issues.
- **Port already in use:** Change the dev server port via `server.port` in [`vite.config.js`](vite.config.js).
- **404 / CORS errors on API calls:** Ensure the backend is running and confirm the proxy target (`server.proxy['/api'].target`) in `vite.config.js` matches the backend's actual port.
