<script setup>
import { getUserInfoService } from '@/api/User';
import { useTokenStore } from '@/stores/token';
import { ref } from 'vue';
import { useRoute } from 'vue-router';

const tokenStore = useTokenStore();

const route = useRoute()
const activeIndex = route.path

const currentUserInfo = ref({})

const getUserInfo = async () => {
    const currentUserInfoData = await getUserInfoService();
    currentUserInfo.value = currentUserInfoData
}

const getCurrentUserInfo = () => {
    if (tokenStore.token !== '') {
        getUserInfo()
    }
}
getCurrentUserInfo()

</script>

<template>
    <div v-if="currentUserInfo.role==='ADMIN'">
        <el-menu :default-active="activeIndex" router mode="horizontal">
            <el-sub-menu index="/admin/user">
                <template #title>User Management</template>
                <el-menu-item index="/admin/user/audit">User Audit Record</el-menu-item>
                <el-menu-item index="/admin/user/info">User Information</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="/admin/forum">
                <template #title>Forum Management</template>
                <el-menu-item index="/admin/forum/audit">Forum Audit Record</el-menu-item>
                <el-menu-item index="/admin/forum/examine">Forum Examine</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/admin/comment">Comment Management</el-menu-item>
        </el-menu>
        <router-view></router-view>
    </div>
</template>