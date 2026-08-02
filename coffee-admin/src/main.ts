import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './styles/index.scss'

const app = createApp(App)

app.use(ElementPlus, { locale: zhCn })
app.use(createPinia())
app.use(router)

app.mount('#app')
