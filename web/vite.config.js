import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 前后端联调统一走 Nginx（见 deploy/nginx/nginx.docker.conf）：
// 浏览器只访问 http://localhost:8090，Docker 内 Nginx 分别转发到宿主机 Vite(5173) 与后端(8080)，
// 故此处不配置 Vite 代理，前端代码统一使用相对路径 /api。
// host 需放开为 0.0.0.0，Docker 容器才能经 host.docker.internal 访问到 Vite。
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173
  }
})
