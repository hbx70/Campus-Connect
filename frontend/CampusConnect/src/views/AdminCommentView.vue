<script setup>

import { getCommentAuditService } from '@/api/audit';
import dayjs from 'dayjs'
import { ref } from 'vue';
const formatTime = (time) => {
    return dayjs(time).format("YYYY-MM-DD HH:mm:ss")
}

const commentAuditData = ref([{}]);
const commentAuditTotal = ref(0)

const filter = ref({
    pageNum: 1,
    pageSize: 10,
    order: "DESC",
    adminName: '',
    content: '',
})

const getCommentAudit = async () => {
    const commentAuditList = await getCommentAuditService(filter.value.pageNum, filter.value.pageSize, filter.value.order, filter.value.adminName.trim(), filter.value.content.trim())
    commentAuditList.items.forEach(comment => {
        comment.createdAt = formatTime(comment.createdAt);
    });
    commentAuditData.value = commentAuditList.items;
    commentAuditTotal.value = commentAuditList.total;
}
getCommentAudit()

const handleSizeChange = (size) => {
    filter.value.pageSize = size;
    getCommentAudit()
}

const handleCurrentChange = (pageNum) => {
    filter.value.pageNum = pageNum;
    getCommentAudit()
}

</script>

<template>
    <div class="filterBlockContainer">
        <div class="filterContainer">
            <div class="radioGroupContainer">
                <p>Order</p>
                <el-radio-group v-model="filter.order" size="large" fill="#409eff" @change="getCommentAudit">
                    <el-radio-button label="Ascending" value="ASC" />
                    <el-radio-button label="Descending" value="DESC" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Admin Name</p>
                <el-input v-model="filter.adminName" style="width: 240px" placeholder="Search admin name" @input="getCommentAudit"/>
            </div>
            <div class="radioGroupContainer">
                <p>Content</p>
                <el-input v-model="filter.content" style="width: 240px" placeholder="Search comment content" @input="getCommentAudit"/>
            </div>
        </div>
        <div>
            <el-pagination
                v-model:current-page="filter.pageNum"
                v-model:page-size="filter.pageSize"
                :page-sizes="[10, 100, 200, 400]"
                :background="true"
                layout="total, sizes, prev, pager, next, jumper"
                :total="commentAuditTotal"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
        </div>
    </div>
    <el-table :data="commentAuditData" style="width: 100%" border stripe>
        <el-table-column fixed prop="createdAt" label="Operation Date" width="140" />
        <el-table-column prop="id" label="Operation ID" width="120" />
        <el-table-column prop="adminId" label="Admin ID" width="120" />
        <el-table-column prop="adminName" label="Admin Name" width="160" show-overflow-tooltip/>
        <el-table-column prop="adminAvatar" label="Admin Avatar" width="120" align="center">
            <template #default="{row}">
                <img :src="row.adminAvatar" alt="admin Avatar" class="adminAvatar">
            </template>
        </el-table-column>
        <el-table-column prop="commentId" label="Comment ID" width="120" show-overflow-tooltip/>
        <el-table-column prop="username" label="Username" width="160" show-overflow-tooltip/>
        <el-table-column label="User Avatar" width="120" align="center">
            <template #default="{row}">
                <img :src="row.userAvatar" alt="User Avatar" class="userAvatar">
            </template>
        </el-table-column>
        <el-table-column prop="commentContent" label="Content" width="500" show-overflow-tooltip/>
        <el-table-column prop="reason" label="Reason" min-width="500" />
        <el-table-column fixed="right" label="Action" width="120" align="center">
            <template #default="{row}">
                <el-tag type="danger" v-if="row.action==='DELETE'" size="large">DELETE</el-tag>
                <el-tag type="info" v-else size="large">{{ row.action }}</el-tag>
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