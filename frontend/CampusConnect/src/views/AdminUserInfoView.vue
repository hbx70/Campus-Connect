<script setup>
import { activeUserService, banUserService, getAllUserInoService } from '@/api/admin';
import {Lock, Unlock} from '@element-plus/icons-vue'
import { ref } from 'vue';

const userInfoData = ref([{}]);
const userInfoTotal = ref(0);

const filter = ref({
    pageNum: 1,
    pageSize: 50,
    order: 'DESC',
    role: 'all',
    status: 'all',
    username: ''
})

const getAllUserInfo = async () => {
    const userInfoList = await getAllUserInoService(filter.value.pageNum, filter.value.pageSize, filter.value.order, filter.value.role === 'all' ? null : filter.value.role, filter.value.status === 'all' ? null : filter.value.status, filter.value.username.trim());
    userInfoList.items.forEach(user => {
        user.createdAt = formatTime(user.createdAt);
    });
    userInfoData.value = userInfoList.items;
    userInfoTotal.value = userInfoList.total;
}
getAllUserInfo()

import dayjs from 'dayjs'
const formatTime = (time) => {
    return dayjs(time).format("YYYY-MM-DD HH:mm:ss")
}

import { ElMessage, ElMessageBox } from 'element-plus'
const promptBanUser = (userId) => {
    ElMessageBox.prompt('Please provide a reason for banning this user.', 'Reason', {
        confirmButtonText: 'Ban',
        confirmButtonType: 'danger',
        cancelButtonText: 'Cancel',
    })
    .then(async ({ value }) => {
        banUser(userId, value)
    })
    .catch(() => {
        ElMessage({
            type: 'info',
            message: 'Ban canceled',
        })
    })
}

const banUser = async (userId, reason) => {
    actionLoading.value.add(userId);
    try {
        await banUserService(userId, reason.trim() === '' ? "The user violated the community's requirements" : reason.trim())
        ElMessage({
            type: 'success',
            message: 'Ban completed',
        })
    } catch (e) {}
    actionLoading.value.delete(userId)
    getAllUserInfo()
}

const actionLoading = ref(new Set());

const isActionLoading = (userId) => {
    return actionLoading.value.has(userId)
}


const activeUser = async (userId) => {
    actionLoading.value.add(userId);
    try {
        await activeUserService(userId);
        ElMessage({
            type: 'success',
            message: "Active completed"
        })
    } catch (e) {}
    actionLoading.value.delete(userId);
    getAllUserInfo()
}

const handleSizeChange = (size) => {
    filter.value.pageSize = size;
    getAllUserInfo()
}

const handleCurrentChange = (pageNum) => {
    filter.value.pageNum = pageNum;
    getAllUserInfo()
}


</script>

<template>
    <div class="filterBlockContainer">
        <div class="filterContainer">
            <div class="radioGroupContainer">
                <p>Order</p>
                <el-radio-group v-model="filter.order" size="large" fill="#409eff" @change="getAllUserInfo">
                    <el-radio-button label="Ascending" value="ASC" />
                    <el-radio-button label="Descending" value="DESC" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Role</p>
                <el-radio-group v-model="filter.role" size="large" fill="#409eff" @change="getAllUserInfo">
                    <el-radio-button label="All" value="all" />
                    <el-radio-button label="User" value="USER" />
                    <el-radio-button label="Admin" value="ADMIN" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Status</p>
                <el-radio-group v-model="filter.status" size="large" fill="#409eff" @change="getAllUserInfo">
                    <el-radio-button label="All" value="all" />
                    <el-radio-button label="Banned" value="BANNED" />
                    <el-radio-button label="Active" value="ACTIVE" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Username</p>
                <el-input v-model="filter.username" style="width: 240px" placeholder="Please enter username" @input="getAllUserInfo"/>
            </div>
        </div>
        <div>
            <el-pagination
                v-model:current-page="filter.pageNum"
                v-model:page-size="filter.pageSize"
                :page-sizes="[50, 100, 200, 400]"
                :background="true"
                layout="total, sizes, prev, pager, next, jumper"
                :total="userInfoTotal"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
        </div>
    </div>
    <el-table :data="userInfoData" style="width: 100%" border stripe>
        <el-table-column fixed prop="createdAt" label="Registration date" width="140" />
        <el-table-column prop="id" label="User ID" width="120" />
        <el-table-column prop="username" label="Username" width="240" show-overflow-tooltip/>
        <el-table-column label="User Avatar" width="120" align="center">
            <template #default="{row}">
                <img :src="row.userAvatar" alt="User Avatar" class="userAvatar">
            </template>
        </el-table-column>
        <el-table-column prop="email" label="User Email" min-width="300" show-overflow-tooltip/>
        <el-table-column label="Role" width="120" align="center">
            <template #default="{row}">
                <el-tag type="primary" v-if="row.role==='ADMIN'" size="large">ADMIN</el-tag>
                <el-tag type="info" v-else size="large">USER</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="Status" width="120" align="center">
            <template #default="{row}">
                <el-tag type="danger" v-if="row.userStatus==='BANNED'" size="large">BANNED</el-tag>
                <el-tag type="success" v-else size="large">ACTIVE</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="Action" width="120" align="center" fixed="right">
            <template #default="{row}">
                <el-button type="danger" :loading="isActionLoading(row.id)" :icon="Lock" circle v-if="row.userStatus === 'ACTIVE'" @click="promptBanUser(row.id)"/>
                <el-button type="success" :loading="isActionLoading(row.id)" :icon="Unlock" circle v-else @click="activeUser(row.id)"/>
            </template>
        </el-table-column>
    </el-table>
</template>

<style scoped>

.userAvatar, .adminAvatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    object-fit: cover;
    object-position: center;
    border: 1px solid #CDD0D6;
}

.filterBlockContainer {
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 10px;
}

.filterContainer {
    display: flex;
    gap: 10px;
}

.radioGroupContainer {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.radioGroupContainer > p {
    color: #303133;
}

.radioGroupContainer .el-input {
    height: 100%;
}

</style>