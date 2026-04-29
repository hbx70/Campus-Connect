<script setup>
import { getUserAuditService } from '@/api/audit';
import { ref } from 'vue';

const userAuditData = ref([{}])
const userAuditTotal = ref(0);

const filter = ref({
    pageNum: 1,
    pageSize: 50,
    order: "DESC",
    action: 'all',
    adminName: ''
})

const getUserAudit = async () => {
    const userAuditList = await getUserAuditService(filter.value.pageNum, filter.value.pageSize, filter.value.order, filter.value.action === 'all' ? null : filter.value.action, filter.value.adminName.trim() === '' ? null : filter.value.adminName.trim());
    userAuditList.items.forEach(audit => {
        audit.opsCreatedAt = formatTime(audit.opsCreatedAt);
    });
    userAuditData.value = userAuditList.items;
    userAuditTotal.value = userAuditList.total;
}
getUserAudit()

import dayjs from 'dayjs'
const formatTime = (time) => {
    return dayjs(time).format("YYYY-MM-DD HH:mm:ss")
}

const handleSizeChange = (size) => {
    filter.value.pageSize = size;
    getUserAudit()
}

const handleCurrentChange = (pageNum) => {
    filter.value.pageNum = pageNum;
    getUserAudit()
}

</script>

<template>
    <div class="filterBlockContainer">
        <div class="filterContainer">
            <div class="radioGroupContainer">
                <p>Order</p>
                <el-radio-group v-model="filter.order" size="large" fill="#409eff" @change="getUserAudit">
                    <el-radio-button label="Ascending" value="ASC" />
                    <el-radio-button label="Descending" value="DESC" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Action</p>
                <el-radio-group v-model="filter.action" size="large" fill="#409eff" @change="getUserAudit">
                    <el-radio-button label="All" value="all" />
                    <el-radio-button label="Ban" value="BANNED" />
                    <el-radio-button label="Active" value="ACTIVE" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Admin Name</p>
                <el-input v-model="filter.adminName" style="width: 240px" placeholder="Search admin name" @input="getUserAudit"/>
            </div>
        </div>
        <div>
            <el-pagination
                v-model:current-page="filter.pageNum"
                v-model:page-size="filter.pageSize"
                :page-sizes="[50, 100, 200, 400]"
                :background="true"
                layout="total, sizes, prev, pager, next, jumper"
                :total="userAuditTotal"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
        </div>
    </div>
    <el-table :data="userAuditData" style="width: 100%" border stripe>
        <el-table-column fixed prop="opsCreatedAt" label="Operation Date" width="140" />
        <el-table-column prop="id" label="Operation ID" width="120" />
        <el-table-column prop="adminId" label="Admin ID" width="120" />
        <el-table-column prop="adminName" label="Admin Name" width="120" show-overflow-tooltip/>
        <el-table-column prop="adminAvatar" label="Admin Avatar" width="120" align="center">
            <template #default="{row}">
                <img :src="row.adminAvatar" alt="admin Avatar" class="adminAvatar">
            </template>
        </el-table-column>
        <el-table-column prop="userId" label="User ID" width="80" />
        <el-table-column prop="username" label="Username" width="120" show-overflow-tooltip/>
        <el-table-column label="User Avatar" width="120" align="center">
            <template #default="{row}">
                <img :src="row.userAvatar" alt="User Avatar" class="userAvatar">
            </template>
        </el-table-column>
        <el-table-column prop="email" label="User Email" width="240" show-overflow-tooltip/>
        <el-table-column prop="reason" label="Reason" min-width="600" />
        <el-table-column fixed="right" label="Action" width="120" align="center">
            <template #default="{row}">
                <el-tag type="danger" v-if="row.action==='BANNED'" size="large">BAN</el-tag>
                <el-tag type="success" v-else size="large">ACTIVE</el-tag>
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