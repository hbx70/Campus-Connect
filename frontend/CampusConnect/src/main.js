import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlust from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { createPersistedState } from 'pinia-plugin-persistedstate'

const app = createApp(App)

const pinia = createPinia();

const persistedstate = createPersistedState();

pinia.use(persistedstate)

app.use(pinia)
app.use(router)
app.use(ElementPlust)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
