import './assets/main.css'
import 'element-plus/dist/index.css'

import dayjs from 'dayjs'
import 'dayjs/locale/en'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import en from 'element-plus/es/locale/lang/en'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

dayjs.locale('en')
document.documentElement.lang = 'en'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: en })

app.mount('#app')
