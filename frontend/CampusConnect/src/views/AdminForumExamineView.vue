<script setup>
import { approvePostService, getAllPostInfoService, rejectPostService } from '@/api/admin';
import { ref } from 'vue'
import { CloseBold, Select, Delete } from '@element-plus/icons-vue'

import dayjs from 'dayjs'
const formatTime = (time) => {
    return dayjs(time).format("YYYY-MM-DD HH:mm:ss")
}

const postData = ref([{}]);
const postTotal = ref(0);

const filter = ref({
    pageNum: 1,
    pageSize: 50,
    type: 'all',
    order: "DESC",
    status: 'all',
    username: ''
})

const parseJSON = (json) => {
    if (!json) return [];
    const coverImageArray = JSON.parse(json);
    return coverImageArray
}

const getAllPostInfo = async () => {
    const postInfoList = await getAllPostInfoService(filter.value.pageNum, filter.value.pageSize, filter.value.type === 'all' ? null : filter.value.type, filter.value.order, filter.value.status === 'all' ? null : filter.value.status, filter.value.username.trim());
    postInfoList.items.forEach(post => {
        post.createdAt = formatTime(post.createdAt);
        post.coverImage = parseJSON(post.coverImage);
    });
    postData.value = postInfoList.items
    postTotal.value = postInfoList.total
}
getAllPostInfo()

const actionLoading = ref(new Set());

const approveForum = async (forumId) => {
    actionLoading.value.add(forumId);
    try {
        await approvePostService(forumId);
        ElMessage({
            type: 'success',
            message: 'Approve completed'
        })
        getAllPostInfo()
    } catch (e) {}
    actionLoading.value.delete(forumId);
}

const rejectForum = async (forumId, reason) => {
    await rejectPostService(forumId, reason.trim() === '' ? "The post violated the community's requirements" : reason.trim());
    ElMessage({
        type: 'success',
        message: 'Reject completed'
    })
}

import { ElMessage, ElMessageBox } from 'element-plus'
import { deletePostService } from '@/api/post';

const confirmOps = (forumId) => {
    ElMessageBox.confirm(
        `Are you sure you want to APPROVE this forum?`,
        'Warning',
        {
            confirmButtonText: 'Approve',
            cancelButtonText: 'Cancel',
            type: 'warning',
        }
    )
    .then(() => {
        approveForum(forumId)
    })
    .catch(() => {
        ElMessage({
            type: 'info',
            message: 'Approve canceled',
        })
    })
}

const promptRejectForum = (forumId) => {
    let isDeleting = false;
    ElMessageBox.prompt('Please provide a reason for rejecting this post.', 'Reason', {
        confirmButtonText: 'Reject',
        confirmButtonType: 'danger',
        cancelButtonText: 'Cancel',
        closeOnClickModal: false,
        showClose: false,
        closeOnPressEscape: false,
        beforeClose: async (action, instance, done) => {
            if (isDeleting) return

            if (action === 'confirm') {
                isDeleting = true
                instance.confirmButtonLoading = true
                instance.confirmButtonText = 'Rejecting...',
                instance.showCancelButton = false
                try {
                    await rejectForum(forumId, instance.inputValue);
                    done();
                    getAllPostInfo()
                } catch (e) {}
                isDeleting = false
                instance.confirmButtonLoading = false
                instance.confirmButtonText = 'Reject',
                instance.showCancelButton = true,
                inputDisabled = false
            } else {
                done()
            }
        },
    })
    .catch(() => {
        ElMessage({
            type: 'info',
            message: 'Reject canceled',
        })
    })
}

const promptDeleteForum = (forumId) => {
    let isDeleting = false;
    ElMessageBox.prompt('Please provide a reason for deleting this post.', 'Reason', {
        confirmButtonText: 'Delete',
        confirmButtonType: 'danger',
        cancelButtonText: 'Cancel',
        closeOnClickModal: false,
        showClose: false,
        closeOnPressEscape: false,
        beforeClose: async (action, instance, done) => {
            if (isDeleting) return

            if (action === 'confirm') {
                isDeleting = true
                instance.confirmButtonLoading = true
                instance.confirmButtonText = 'Deleting...',
                instance.showCancelButton = false
                try {
                    await deletePost(forumId, instance.inputValue);
                    done();
                    getAllPostInfo()
                } catch (e) {}
                isDeleting = false
                instance.confirmButtonLoading = false
                instance.confirmButtonText = 'Delete',
                instance.showCancelButton = true,
                inputDisabled = false
            } else {                                
                done()
            }
        },
    })
    .catch(() => {
        ElMessage({
            type: 'info',
            message: 'Delete canceled',
        })
    })
}

const deletePost = async (forumId, reason) => {
    await deletePostService(forumId, reason.trim() === '' ? "The post violated the community's requirements" : reason.trim())
    ElMessage({
        type: 'success',
        message: 'Delete completed',
    })
}

const handleSizeChange = (size) => {
    filter.value.pageSize = size;
    getAllPostInfo()
}

const handleCurrentChange = (pageNum) => {
    filter.value.pageNum = pageNum;
    getAllPostInfo()
}


</script>

<template>
    <div class="filterBlockContainer">
        <div class="filterContainer">
            <div class="radioGroupContainer">
                <p>Order</p>
                <el-radio-group v-model="filter.order" size="large" fill="#409eff" @change="getAllPostInfo">
                    <el-radio-button label="Ascending" value="ASC" />
                    <el-radio-button label="Descending" value="DESC" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Type</p>
                <el-radio-group v-model="filter.type" size="large" fill="#409eff" @change="getAllPostInfo">
                    <el-radio-button label="All" value="all" />
                    <el-radio-button label="Forum" value="FORUM" />
                    <el-radio-button label="Activity" value="ACTIVITY" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Status</p>
                <el-radio-group v-model="filter.status" size="large" fill="#409eff" @change="getAllPostInfo">
                    <el-radio-button label="All" value="all" />
                    <el-radio-button label="Pending" value="PENDING" />
                    <el-radio-button label="Approved" value="APPROVED" />
                    <el-radio-button label="Rejected" value="REJECTED" />
                </el-radio-group>
            </div>
            <div class="radioGroupContainer">
                <p>Username</p>
                <el-input v-model="filter.username" style="width: 240px" placeholder="Search username" @input="getAllPostInfo"/>
            </div>
        </div>
        <el-pagination
            v-model:current-page="filter.pageNum"
            v-model:page-size="filter.pageSize"
            :page-sizes="[50, 100, 200, 400]"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="postTotal"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
    </div>
    <el-table :data="postData" border stripe :preserve-expanded-content="false" style="width: 100%">
        <el-table-column type="expand">
            <template #default="props">
                <div class="postInfoContainerWapper">
                    <div class="postInfoContainer">
                        <div class="forumUserInfoContainer">
                            <img :src="props.row.userAvatar" alt="user avatar" class="forumUserAvatar">
                            <div class="forumUserInfoBox">
                                <div class="forumUsernameBox">
                                    <p class="forumUsername">{{ props.row.username }}</p>
                                    <el-tag type="primary" size="small" v-if="props.row.role === 'ADMIN'">Admin</el-tag>
                                    <el-tag type="danger" size="small"
                                        v-if="props.row.userStatus === 'BANNED'">Banned</el-tag>
                                </div>
                                <p class="forumCreatedTime">{{ formatTime(props.row.createdAt) }}</p>
                            </div>
                        </div>
                        <div class="forumContentContainer">
                            <div class="forumContent" v-html="props.row.content">
                            </div>
                            <div class="imgContainerWapper" v-if="props.row.coverImage.length !== 0">
                                <div class="imgContainer" :class="'count-' + props.row.coverImage.length">
                                    <div class="imgWapper" v-for="(img, index) in props.row.coverImage" :key="index">
                                        <el-image :preview-teleported="true" :src="img" alt="forum image" :zoom-rate="1.2" :max-scale="7"
                                            :min-scale="0.2" :preview-src-list="props.row.coverImage" show-progress
                                            :initial-index="index" fit="cover"></el-image>
                                        <div class="imgMask"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="postOpsContainer" v-if="props.row.status === 'PENDING'">
                        <el-button type="success" round plain :icon="Select" @click="confirmOps(props.row.id)" :loading="actionLoading.has(props.row.id)">Approve</el-button>
                        <el-button type="danger" round plain :icon="CloseBold" @click="promptRejectForum(props.row.id)" :disabled="actionLoading.has(props.row.id)">Reject</el-button>
                    </div>
                    <div class="postOpsContainer" v-else-if="props.row.status === 'APPROVED'">
                        <el-button type="danger" round plain :icon="Delete" @click="promptDeleteForum(props.row.id)">Delete</el-button>
                    </div>
                    <div class="postOpsContainer" v-else>
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column label="Created Date" prop="createdAt" width="120" />
        <el-table-column label="Forum ID" prop="id" width="100" show-overflow-tooltip />
        <el-table-column label="Username" prop="username" width="180" show-overflow-tooltip />
        <el-table-column label="User Role" prop="role" width="120" align="center">
            <template #default="{ row }">
                <el-tag type="primary" v-if="row.role === 'ADMIN'" size="large">ADMIN</el-tag>
                <el-tag type="info" v-else size="large">USER</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="Title" prop="title" min-width="250" show-overflow-tooltip />
        <el-table-column label="Type" prop="type" width="120" align="center">
            <template #default="{ row }">
                <el-tag type="info" v-if="row.type === 'FORUM'" size="large">FORUM</el-tag>
                <el-tag type="warning" v-else size="large">ACTIVITY</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="Likes" prop="participants" width="100" show-overflow-tooltip />
        <el-table-column label="Comments" prop="comments" width="100" show-overflow-tooltip />
        <el-table-column label="Status" prop="status" width="120" align="center" fixed="right">
            <template #default="{ row }">
                <el-tag type="info" v-if="row.status === 'PENDING'" size="large">PENDING</el-tag>
                <el-tag type="danger" v-else-if="row.status === 'REJECTED'" size="large">REJECTED</el-tag>
                <el-tag type="success" v-else size="large">APPROVED</el-tag>
            </template>
        </el-table-column>
    </el-table>
</template>


<style scoped>

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

.postInfoContainerWapper {
    display: flex;
    justify-content: space-between;
    gap: 200px;
    margin: 20px 50px;
}

.postInfoContainer {
    width: 100%;
    padding: 10px;
    border: 1px solid #CDD0D6;
    border-radius: 8px;
    /* margin: 20px 50px; */
}

.forumUserInfoContainer {
    display: flex;
    gap: 10px;
    align-items: center;
}

.forumUserAvatar {
    width: 50px;
    height: 50px;
    object-fit: cover;
    object-position: center;
    border-radius: 50%;
    border: 1px solid #CDD0D6;
}

.forumUserInfoBox {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.forumUsernameBox {
    display: flex;
    align-items: center;
    gap: 5px;
}

.forumUserInfoBox .forumUsername {
    font-size: 18px;
    font-weight: 500;
}

.forumUserInfoBox .forumCreatedTime {
    font-size: 14px;
    font-weight: 300;
    color: #909399;
    font-style: italic;
}

.forumContentContainer {
    margin-left: 62px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.imgContainerWapper {
    width: 100%;
    height: fit-content;
    box-sizing: border-box;
    /* overflow: hidden; */
}

:deep(.forumContent) {
    /* paragraph */
    p {
        line-height: 1.5;
    }

    > p {
        margin-bottom: 10px;
    }

    /* Link */
    a {
        color: #6a00f5;
        cursor: pointer;
    }

    a:visited {
        color: #6a00f5;
    }

    a:hover {
        color: #5800cc;
    }

    /* Code Block */
    pre {
        background: #2e2b29;
        border-radius: 0.5rem;
        color: #fff;
        font-family: 'JetBrainsMono', monospace;
        margin: 1.5rem 0;
        padding: 0.75rem 1rem;

        code {
            background: none;
            color: inherit;
            font-size: 0.8rem;
            padding: 0;
        }
    }
}

.imgContainer {
    display: grid;
    gap: 4px;
    width: 100%;
}

.imgContainer > * {
    aspect-ratio: 1 / 1;
}

.imgContainer.count-1 {
    grid-template-columns: 1fr;
}

.imgContainer.count-1 > * {
    aspect-ratio: 16 / 9;
}

.imgContainer.count-2,
.imgContainer.count-4 {
    grid-template-columns: repeat(2, 1fr);
}

.imgContainer.count-3,
.imgContainer.count-5,
.imgContainer.count-6,
.imgContainer.count-7,
.imgContainer.count-8,
.imgContainer.count-9 {
    grid-template-columns: repeat(3, 1fr);
}

.imgWapper {
    position: relative;
    overflow: hidden;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
}


.imgContainer .el-image {
    width: 100%;
    height: 100%;
    object-position: center;
}

.imgMask {
    position: absolute;
    inset: 0;
    background: rgba(0,0,0,0);
    transition: all 0.2s ease;
    pointer-events: none;
}

.imgWapper:hover .imgMask {
    background: rgba(0,0,0,0.06);
}

.imgWapper:hover {
    box-shadow: 0 4px 10px rgba(0,0,0,0.12);
}

.postOpsContainer {
    display: flex;
    flex-direction: column;
    gap: 10px;
    width: 500px;
    padding: 10px;
}

.postOpsContainer .el-button {
    width: 100%;
    height: 50px;
    margin: 0;
}

</style>