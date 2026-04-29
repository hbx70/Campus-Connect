// stores/ui.js
import { ElMain, ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
    const accountOpsVisible = ref(false)
    const currentAccountOps = ref('')

    const openLogin = (msg) => {
        if (msg) {
            ElMessage({
                type: 'error',
                message: msg
            })
        }
        accountOpsVisible.value = true
        currentAccountOps.value = 'login'
    }

    const openRegister = () => {
        accountOpsVisible.value = true
        currentAccountOps.value = 'register'
    }

    const jumpAccountOpsTo = (location) => {
        currentAccountOps.value = location;
    }

    const closeAccountOps = () => {
        accountOpsVisible.value = false
    }

    return {
        accountOpsVisible,
        currentAccountOps,
        openLogin,
        openRegister,
        jumpAccountOpsTo,
        closeAccountOps
    }
})