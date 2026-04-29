<script setup>

import { deletePostService, getAllUserLikedPostService, getPostService } from '@/api/post';
import { getUserInfoService, sendUpdateUserEmailOTPService, updateUserEmailCheckService, updateUserEmailService, updateUsernameService, uploadUserAvatarService } from '@/api/User';
import useUserInfoStore from '@/stores/userInfo';
const userInfoStore = useUserInfoStore();

import { Position, UploadFilled, Edit, CircleCheck, ArrowRight, ArrowLeft, Lock, Delete } from '@element-plus/icons-vue'

import dayjs from 'dayjs'

import { ElMessage, ElMessageBox } from 'element-plus';
import { ref } from 'vue';

const uploadRef = ref(null)
const selectedFile = ref(null)
const avatarURL = ref('')
const uploadAvatarLoading = ref(false);

const formatTime = (time) => {
    return dayjs(time).format("YYYY-MM-DD HH:mm:ss")
}

const parseJSON = (json) => {
    if (!json) return [];
    const coverImageArray = JSON.parse(json);
    return coverImageArray
}

const handleAvatarChange = (uploadFile) => {
    const rawFile = uploadFile.raw


    const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(rawFile.type)
    if (!isValidType) {
        ElMessage.error('Avatar picture must be JPG/JPEG/PNG/WEBP format!')
        return false
    }

    if (rawFile.size / 1024 / 1024 > 5) {
        ElMessage.error('Avatar picture size can not exceed 5MB!')
        return false
    }

    if (avatarURL.value) {
        URL.revokeObjectURL(avatarURL.value)
    }

    selectedFile.value = rawFile
    avatarURL.value = URL.createObjectURL(rawFile)
}

const handleAvatarExceed = (files) => {
    uploadRef.value.clearFiles()
    const file = files[0]
    uploadRef.value.handleStart(file)
}


const uploadUserAvatar = async () => {
    if (!selectedFile.value) {
        return
    }
    uploadAvatarLoading.value = true;
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    try {
        await uploadUserAvatarService(formData);
        const result = await getUserInfoService();
        userInfoStore.setInfo(result);
        selectedFile.value = null;
        avatarURL.value = '';
        ElMessage.success("upload successfully")
    } catch (e) { }
    uploadAvatarLoading.value = false;
}

const userNewInfo = ref({
    email: '',
    otp: '',
    username: ''
})

const isValid = ref({
    email: true,
    otp: true,
    username: true
})

const updateLoading = ref({
    email: false,
    username: false
})

const postData = ref();
const postTotal = ref(0);

const currentActiveStep = ref({
    email: 1,
    username: 1
});

const validateEmail = () => {
    const email = userNewInfo.value.email.trim()
    if (email === '') {
        isValid.value.email = false;
        throw new Error("Please enter email address")
    } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)) {
        isValid.value.email = false;
        throw new Error("Please enter valid email address")
    }
}

const validateOTP = () => {
    const otp = userNewInfo.value.otp.trim()
    if (otp === '' || otp.length !== 6) {
        isValid.value.otp = false;
        throw new Error("Please enter 6 digits verification code")
    }
}

const handleNextStep = async () => {
    if (currentActiveStep.value.email > 3) {
        return
    }
    updateLoading.value.email = true;
    if (currentActiveStep.value.email === 1) {
        try {
            validateEmail()
        } catch (e) {
            ElMessage({
                type: 'error',
                message: e.message
            })
            updateLoading.value.email = false;
            return
        }
        try {
            await updateUserEmailCheckService(userNewInfo.value.email.trim());
            await sendUpdateUserEmailOTPService(userNewInfo.value.email.trim());
            currentActiveStep.value.email += 1;
        } catch (e) {}
        updateLoading.value.email = false;
        return
    }
    if (currentActiveStep.value.email === 2) {
        try {
            validateOTP()
        } catch(e) {
            ElMessage({
                type: 'error',
                message: e.message
            })
            return
        }
        try {
            await updateUserEmailService(userNewInfo.value.email, userNewInfo.value.otp);
            const newUserInfo = await getUserInfoService()
            userInfoStore.setInfo(newUserInfo)
            currentActiveStep.value.email += 1;
            ElMessage.success("Update successfully")
        } catch (e) {
            isValid.value.otp = false;
        }
        updateLoading.value.email = false;
        return
    }
    if (currentActiveStep.value.email === 3) {
        currentActiveStep.value.email = 1
        userNewInfo.value.email = ''
        userNewInfo.value.otp = ''
        updateLoading.value.email = false;
        return
    }
    
}

const handlePreviousStep = () => {
    if (currentActiveStep.value.email <= 1) {
        return
    }
    currentActiveStep.value.email -= 1
}

const handleInputCode = (e) => {
    isValid.value.otp = true;
    userNewInfo.value.otp = e.target.value.replace(/[^\d]/g, '');
    if (userNewInfo.value.otp.trim().length === 6) {
        handleNextStep();
    }
};

const clearErrorHint = (type) => {
    isValid.value[type] = true;
}

const validateUsername = () => {
    const username = userNewInfo.value.username.trim();
    if (username === '') {
        isValid.value.username = false
        throw new Error("Please enter your new username")
    } else if (username.length < 3 || username.length > 16) {
        isValid.value.username = false
        throw new Error("The length of the username must be between 3 and 16")
    }
}


const handleNextUsernameStep = async () => {
    if (currentActiveStep.value.username > 2) {
        return
    }
    updateLoading.value.username = true;
    if (currentActiveStep.value.username === 1) {
        try {
            validateUsername()
        } catch (e) {
            ElMessage({
                type: 'error',
                message: e.message
            })
            updateLoading.value.username = false
            return
        }
        try {
            await updateUsernameService(userNewInfo.value.username.trim())
            const newUserInfo = await getUserInfoService()
            userInfoStore.setInfo(newUserInfo)
            currentActiveStep.value.username += 1
        } catch (e) {}
        updateLoading.value.username = false;
        return
    }
    if (currentActiveStep.value.username === 2) {
        currentActiveStep.value.username = 1
        userNewInfo.value.username = ''
        updateLoading.value.username = false
        return
    }
}

const filter = ref({
    pageNum: 1,
    pageSize: 50,
    order: 'DESC',
    type: 'all',
    status: 'all',
    title: ''
})

const getAllPost = async () => {
    const requestData = {
        ...filter.value,
        type: filter.value.type === 'all' ? null : filter.value.type,
        status: filter.value.status === 'all' ? null : filter.value.status,
        userId: 1
    }
    const postListInfo = await getPostService(requestData);
    postListInfo.items.forEach(async (post) => {
        post.createdAt = formatTime(post.createdAt);
        post.approvedAt = post.approvedAt === null ? 'PENDING' : formatTime(post.approvedAt);
        post.coverImage = parseJSON(post.coverImage)
        post.likedUserInfo = await getAllUserLikedPostService(post.id)
    });
    postData.value = postListInfo.items;
    postTotal.value = postListInfo.total;
}
getAllPost()

const handleSizeChange = (size) => {
    filter.value.pageSize = size;
    getAllPost()
}

const handleCurrentChange = (pageNum) => {
    filter.value.pageNum = pageNum;
    getAllPost()
}

const confirmDeletePost = (postId) => {
    ElMessageBox.confirm(
        'Are you sure you want to delete this post?',
        'Warning',
        {
            confirmButtonText: 'Delete',
            confirmButtonType: 'danger',
            cancelButtonText: 'Cancel',
            type: 'warning',
        }
    )
        .then(() => {
            deletePost(postId);
            ElMessage({
                type: 'success',
                message: 'Delete completed',
            })
        })
        .catch(() => {
            ElMessage({
                type: 'info',
                message: 'Delete canceled',
            })
        })
}

const deletePost = async (postId) => {
    const oldPostData = [...postData.value]
    postData.value = postData.value.filter(post => post.id !== postId)
    postTotal.value -= 1
    try {
        await deletePostService(postId, null)
    } catch (e) {
        postData.value = oldPostData
        postTotal.value += 1
    }
}

</script>

<template>
    <el-container>
        <el-main>
            <div id="userInfoContainer">
                <el-descriptions title="My Information" border>
                    <el-descriptions-item :rowspan="2" :width="140" label="Photo" align="center">
                        <el-image :src="userInfoStore.info.userAvatar" alt="user avatar" fit="cover"
                            :preview-teleported="true" :zoom-rate="1.2" :max-scale="7" :min-scale="0.2"
                            :preview-src-list="[userInfoStore.info.userAvatar]" show-progress :initial-index="0" />
                    </el-descriptions-item>
                    <el-descriptions-item label="Username" :label-width="140">{{ userInfoStore.info.username
                        }}</el-descriptions-item>
                    <el-descriptions-item label="Email" :label-width="140">{{ userInfoStore.info.email
                        }}</el-descriptions-item>
                    <el-descriptions-item label="Role">
                        <el-tag size="large">{{ userInfoStore.info.role }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="Status">
                        <el-tag type="success" size="large" v-if="userInfoStore.info.userStatus === 'ACTIVE'">{{
                            userInfoStore.info.userStatus }}</el-tag>
                        <el-tag type="danger" size="large" v-else>{{ userInfoStore.info.userStatus }}</el-tag>
                    </el-descriptions-item>
                </el-descriptions>
            </div>
            <div id="updateInfoContainer">
                <div class="avatarContainer">
                    <p class="updateTitle">Update Profile Picture</p>
                    <div class="uploadContainer">
                        <el-upload ref="uploadRef" :on-change="handleAvatarChange" :on-exceed="handleAvatarExceed"
                            :force-re-upload="true" action="/dummy" drag :auto-upload="false" :show-file-list="false"
                            :multiple="false" :limit="1">
                            <template v-if="avatarURL === ''">
                                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                                <div class="el-upload__text">
                                    Drop file here or <em>click to upload</em>
                                </div>
                            </template>
                            <template v-else>
                                <el-image :src="avatarURL" fit="cover"></el-image>
                            </template>
                        </el-upload>
                        <div class="uploadOpsContainer">
                            <el-button type="primary" plain round size="large" :disabled="selectedFile === null"
                                :icon="Position" :loading="uploadAvatarLoading" @click="uploadUserAvatar">{{
                                    uploadAvatarLoading ? 'Uploading' : 'Upload Avatar'
                                }}</el-button>
                        </div>
                    </div>
                </div>
                <div class="emailAndUsernameContainer">
                    <div class="emailContainer">
                        <p class="updateTitle">Update My Email</p>
                        <div class="emailUpdateContainer">
                            <el-steps :space="200" :active="currentActiveStep.email" simple>
                                <el-step title="Enter" :icon="Edit" />
                                <el-step title="Verify" :icon="Lock" />
                                <el-step title="Done" :icon="CircleCheck" />
                            </el-steps>
                            <div class="emailUpdateBlock" v-if="currentActiveStep.email===1">
                                <div class="stepInfoContainer">
                                    <p class="stepTitle">Step 1</p>
                                    <p>Your new email address will replace your existing email address. All future notifications will be sent to the new email address.</p>
                                </div>
                                <input type="text" @input="clearErrorHint('email')" :class="{'error': !isValid.email}" v-model="userNewInfo.email" placeholder="Please enter your new email address">
                                <div class="stepBtnContainer">
                                    <el-button type="primary" plain round size="large" :loading="updateLoading.email" @click="handleNextStep">Next Step<el-icon><ArrowRight /></el-icon></el-button>
                                </div>
                            </div>
                            <div class="emailUpdateBlock" v-else-if="currentActiveStep.email===2">
                                <div class="stepInfoContainer">
                                    <p class="stepTitle">Step 2</p>
                                    <p>A verification code has been sent to {{ userNewInfo.email }}. Please enter it within <strong>5 minutes</strong> before it expires</p>
                                </div>
                                <div class="codeContainerWapper">
                                    <div class="codeContainer">
                                        <input v-model="userNewInfo.otp" type="tel" maxlength="6" class="realInput" autofocus @input="handleInputCode" />
                                        <div class="codeWapper">
                                            <div v-for="(item, index) in 6" :key="index" :class="['codeSlot', { 'active': userNewInfo.otp.length === index, 'filled': userNewInfo.otp.length > index, 'error': !isValid.otp }]">
                                                {{ userNewInfo.otp[index] }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="stepBtnContainer">
                                    <el-button type="info" plain round size="large" :disabled="updateLoading.email" @click="handlePreviousStep"><el-icon><ArrowLeft /></el-icon>Previous Step</el-button>
                                    <el-button type="primary" plain round size="large" :loading="updateLoading.email" @click="handleNextStep">Next Step<el-icon><ArrowRight /></el-icon></el-button>
                                </div>
                            </div>
                            <div class="emailUpdateBlock" v-else-if="currentActiveStep.email===3">
                                <div class="stepInfoContainer">
                                    <p class="stepTitle">Step 3</p>
                                    <p>Your email address has been successfully updated. All future notifications will be sent to your new email address.</p>
                                </div>
                                <p>{{ userNewInfo.email }}</p>
                                <div class="stepBtnContainer">
                                    <el-button type="primary" plain round size="large" :loading="updateLoading.email" @click="handleNextStep">Done</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="usernameContainer">
                        <p class="updateTitle">Update My Username</p>
                        <div class="usernameUpdateContinaer">
                            <el-steps :active="currentActiveStep.username" simple>
                                <el-step title="Enter" :icon="Edit" />
                                <el-step title="Done" :icon="CircleCheck" />
                            </el-steps>
                            <div class="usernameUpdateBlock" v-if="currentActiveStep.username===1">
                                <div class="stepInfoContainer">
                                    <p class="stepTitle">Step 1</p>
                                    <p>Your new username will replace existing username. You will need to use the new username for all future logins</p>
                                </div>
                                <input type="text" @input="clearErrorHint('username')" :class="{'error': !isValid.username}" v-model="userNewInfo.username" placeholder="Please enter your new username">
                                <div class="stepBtnContainer">
                                    <el-button type="primary" plain round size="large" :loading="updateLoading.username" @click="handleNextUsernameStep">Next Step<el-icon><ArrowRight /></el-icon></el-button>
                                </div>
                            </div>
                            <div class="usernameUpdateBlock" v-else-if="currentActiveStep.username===2">
                                <div class="stepInfoContainer">
                                    <p class="stepTitle">Step 2</p>
                                    <p>Your username has been successfully updated. You will need to use the new username for all future logins</p>
                                </div>
                                <p>{{ userNewInfo.username }}</p>
                                <div class="stepBtnContainer">
                                    <el-button type="primary" plain round size="large" :loading="updateLoading.username" @click="handleNextUsernameStep">Done<el-icon><ArrowRight /></el-icon></el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="myPostInfoContainer">
                <p class="title">My Post Information</p>
                <div class="myPostInfoTabelWapper">
                    <div class="filterBlockContainer">
                        <div class="filterContainer">
                            <div class="radioGroupContainer">
                                <p>Order</p>
                                <el-radio-group v-model="filter.order" size="large" fill="#409eff" @change="getAllPost">
                                    <el-radio-button label="Ascending" value="ASC" />
                                    <el-radio-button label="Descending" value="DESC" />
                                </el-radio-group>
                            </div>
                            <div class="radioGroupContainer">
                                <p>Type</p>
                                <el-radio-group v-model="filter.type" size="large" fill="#409eff" @change="getAllPost">
                                    <el-radio-button label="All" value="all" />
                                    <el-radio-button label="Forum" value="FORUM" />
                                    <el-radio-button label="Activity" value="ACTIVITY" />
                                </el-radio-group>
                            </div>
                            <div class="radioGroupContainer">
                                <p>Status</p>
                                <el-radio-group v-model="filter.status" size="large" fill="#409eff" @change="getAllPost">
                                    <el-radio-button label="All" value="all" />
                                    <el-radio-button label="Pending" value="PENDING" />
                                    <el-radio-button label="Approved" value="APPROVED" />
                                    <el-radio-button label="Rejected" value="REJECTED" />
                                </el-radio-group>
                            </div>
                            <div class="radioGroupContainer">
                                <p>Title</p>
                                <el-input v-model="filter.title" style="width: 240px" placeholder="Search title"
                                    @input="getAllPost" />
                            </div>
                        </div>
                        <div>
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
                                                    <el-tag type="primary" size="small"
                                                        v-if="props.row.role === 'ADMIN'">Admin</el-tag>
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
                                                <div class="imgContainer"
                                                    :class="'count-' + props.row.coverImage.length">
                                                    <div class="imgWapper" v-for="(img, index) in props.row.coverImage"
                                                        :key="index">
                                                        <el-image :preview-teleported="true" :src="img"
                                                            alt="forum image" :zoom-rate="1.2" :max-scale="7"
                                                            :min-scale="0.2" :preview-src-list="props.row.coverImage"
                                                            show-progress :initial-index="index" fit="cover"></el-image>
                                                        <div class="imgMask"></div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="userLikesGroup">
                                                <div v-show="props.row.likedUserInfo.length > 0">
                                                    <span class="material-symbols-outlined likes" v-if="props.row.type==='FORUM'">favorite</span>
                                                    <span class="material-symbols-outlined participant" v-else>groups</span>
                                                </div>
                                                <el-avatar-group collapse-avatars :max-collapse-avatars="15" collapse-avatars-tooltip>
                                                    <el-tooltip effect="dark" :content="avatar.username" placement="bottom" v-for="(avatar, index) in props.row.likedUserInfo" :key="index">
                                                        <el-avatar :src="avatar.userAvatar" />
                                                    </el-tooltip>
                                                </el-avatar-group>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="postOpsContainer" v-if="props.row.status === 'APPROVED'">
                                        <el-button type="danger" round plain :icon="Delete" @click="confirmDeletePost(props.row.id)">Delete</el-button>
                                    </div>
                                    <div class="postOpsContainer" v-else></div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column label="Created Date" prop="createdAt" width="140" />
                        <el-table-column label="Approved Date" prop="approvedAt" width="140" />
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
                                <el-tag type="danger" v-else-if="row.status === 'REJECTED'"
                                    size="large">REJECTED</el-tag>
                                <el-tag type="success" v-else size="large">APPROVED</el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
        </el-main>
    </el-container>
</template>

<style scoped>
.el-main {
    display: flex;
    flex-direction: column;
    gap: 30px;
    color: #303133;
}

#userInfoContainer .el-image {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-position: center;
    border: 1px solid #DCDFE6;
}

#updateInfoContainer {
    display: flex;
    gap: 20px;
}

#updateInfoContainer .avatarContainer {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

#updateInfoContainer .updateTitle {
    font-size: 16px;
    font-weight: bold;
}

#updateInfoContainer .uploadContainer {
    width: fit-content;
    height: fit-content;
    display: flex;
    flex-direction: column;
    gap: 10px;
    border: 1px solid #DCDFE6;
    border-radius: 8px;
    padding: 20px;
}

.uploadContainer>div:first-of-type {
    width: 300px;
    height: 300px;
}

.uploadContainer .el-image {
    width: 100%;
    height: 100%;
    object-position: center;
}

:deep(.el-upload) {
    width: 100%;
    height: 100%;
}

:deep(.el-upload-dragger) {
    width: 100%;
    height: 100%;
    padding: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

#updateInfoContainer .uploadOpsContainer {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

#updateInfoContainer .emailAndUsernameContainer {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
}

#updateInfoContainer .emailContainer {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

#updateInfoContainer .emailUpdateContainer {
    padding: 20px;
    border: 1px solid #DCDFE6;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

#updateInfoContainer .emailUpdateBlock, #updateInfoContainer .usernameUpdateBlock{
    display: flex;
    flex-direction: column;
    gap: 20px;
}

#updateInfoContainer .stepInfoContainer {
    display: flex;
    flex-direction: column;
    gap: 5px;
    font-size: 14px;
    font-weight: 350;
    color: #909399;
}

#updateInfoContainer .stepTitle {
    font-size: 20px;
    font-weight: 550;
    color: #303133;
}

#updateInfoContainer input {
    border: 1px solid #DCDFE6;
    border-radius: 4px;
    outline: none;
    font-size: 16px;
    padding: 5px 2px;
    transition: all 0.3s ease;
    width: 100%;
    box-sizing: border-box;
}

#updateInfoContainer input.error {
    border: 1px solid red;
}

#updateInfoContainer input:focus {
    border: 1px solid #409EFF;
}

#updateInfoContainer input::placeholder {
    color: #A8ABB2;
    font-size: 12px;
}

#updateInfoContainer .stepBtnContainer {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
}

.stepBtnContainer > :only-child {
  grid-column: span 2;
}

.codeContainerWapper {
    display: flex;
    justify-content: center;
}

.codeContainer {
    position: relative;
    width: 300px;
    margin: 20px 0;
}

.realInput {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
    z-index: 10;
    cursor: pointer;
}

.codeWapper {
    display: flex;
    justify-content: space-between;
}

.codeSlot {
    width: 40px;
    height: 50px;
    font-size: 24px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 2px solid #DCDFE6; 
    color: #303133;
    transition: all 0.2s ease;
}

.codeSlot.active,
.codeSlot.filled {
    border-bottom-color: #303133;
}

.codeSlot.error {
    border-bottom: 2px solid red;
}

.codeSlot.active::after {
    content: "";
    width: 2px;
    height: 20px;
    background-color: #303133;
    animation: blink 1s infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

.usernameContainer {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.usernameUpdateContinaer {
    padding: 20px;
    border: 1px solid #DCDFE6;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    gap: 20px;
}














#myPostInfoContainer {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

#myPostInfoContainer > p {
    font-size: 16px;
    font-weight: bold;
}

.myPostInfoTabelWapper {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.filterBlockContainer {
    display: flex;
    flex-direction: column;
    gap: 20px;
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

.radioGroupContainer>p {
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

.userLikesGroup {
    display: flex;
    align-items: center;
    gap: 10px;
}

.userLikesGroup .material-symbols-outlined.likes {
    color: #F56C6C;
    font-size: 30px;
    font-variation-settings:
        'FILL' 1,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
}

.userLikesGroup .material-symbols-outlined.participant {
    font-size: 30px;
    font-variation-settings:
        'FILL' 0,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
}

:deep(.forumContent) {

    /* paragraph */
    p {
        line-height: 1.5;
    }

    >p {
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

.imgContainer>* {
    aspect-ratio: 1 / 1;
}

.imgContainer.count-1 {
    grid-template-columns: 1fr;
}

.imgContainer.count-1>* {
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
    background: rgba(0, 0, 0, 0);
    transition: all 0.2s ease;
    pointer-events: none;
}

.imgWapper:hover .imgMask {
    background: rgba(0, 0, 0, 0.06);
}

.imgWapper:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
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