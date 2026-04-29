// 定义store
import { defineStore } from "pinia";
import { ref } from "vue";


// 两个参数
// 名字，唯一性
// 函数，内部可以定义状态的所有内容
// 返回值 函数
export const useTokenStore = defineStore('token', ()=>{
    // 定义状态内容
    // 1. 响应式变量
    const token = ref('')

    // 2. 定义一个函数，用于修改token的值
    const setToken = (newToken) => {
        token.value = newToken
    }

    // 3. 函数，用于移除token的值
    const removeToken = () => {
        token.value = '';
    }

    return {
        token, setToken, removeToken
    }
}, {
    persist: true // 持久化存储
});