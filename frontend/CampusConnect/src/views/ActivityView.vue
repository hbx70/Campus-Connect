<script setup>
import { deletePostService, getPostService, getReadPostService, participatePostService, withdrawPostService } from '@/api/post';
import { User, Star, Delete, Position, ChatLineRound } from '@element-plus/icons-vue'
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

import useUserInfoStore from '@/stores/userInfo'
const userInfoStore = useUserInfoStore();

const UIStore = useUIStore();

const tokenStore = useTokenStore();

const isAdmin = () => {
    return userInfoStore.info.role === "ADMIN";
}

const currOwnedActivityId = ref(new Set());

const getAllOwnedPost = async () => {
    const postIdList = await getAllOwnedPostService();
    currOwnedActivityId.value = new Set(postIdList);
}

const currUserParticipate = ref(new Set());
const getAllParticipatePost = async () => {
    const postIdList = await getAllParticipatePostService();
    currUserParticipate.value = new Set(postIdList)
}

const isParticipate = (activityId) => {
    return currUserParticipate.value.has(activityId);
}

const joinActivity = async (activity) => {
    if (tokenStore.token === '') {
        UIStore.openLogin("Please Login");
        drawerVisible.value = false;
        return
    }
    currUserParticipate.value.add(activity.id);
    activity.participants++;
    try {
        await participatePostService(activity.id);
        ElMessage({
            type: "success",
            message: "join successfully"
        })
    } catch (e) {
        currUserParticipate.value.delete(activity.id);
        activity.participants--;
        ElMessage({
            type: "error",
            message: e
        })
    }
}

const leaveActivity = async (activity) => {
    if (tokenStore.token === '') {
        UIStore.openLogin("Please Login");
        drawerVisible.value = false;
        return
    }
    currUserParticipate.value.delete(activity.id);
    activity.participants--;
    try {
        await withdrawPostService(activity.id);
        ElMessage({
            type: "success",
            message: "leave successfully"
        })
    } catch (e) {
        currUserParticipate.value.add(activity.id);
        activity.participants++;
        ElMessage({
            type: "error",
            message: e
        })
    }
}

const hasRightDelete = (activityId) => {
    if (isAdmin()) {
        return true
    }
    if (currOwnedActivityId.value.has(activityId)) {
        return true
    }
    return false;
}

const promptDeletePost = (activityId) => {
    ElMessageBox.prompt('Please provide a reason for deleting this activity.', 'Reason', {
        confirmButtonText: 'Delete',
        confirmButtonType: 'danger',
        cancelButtonText: 'Cancel',
    })
    .then(({ value }) => {
        deletePost(activityId, value)
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

const confirmDeletePost = (activityId) => {
    ElMessageBox.confirm(
        'Are you sure you want to delete this activity?',
        'Warning',
        {
            confirmButtonText: 'Delete',
            confirmButtonType: 'danger',
            cancelButtonText: 'Cancel',
            type: 'warning',
        }
    )
    .then(() => {
        deletePost(activityId, null);
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

const deletePost = async (activityId, reason) => {
    const oldActivity = [...activity.value.items]
    activity.value.items = activity.value.items.filter(activity => activity.id !== activityId)
    drawerVisible.value = false;
    try {
        if (reason === null) {
            await deletePostService(activityId, null)
        } else {
            await deletePostService(activityId, reason.trim() === '' ? "The post violated the community's requirements" : reason.trim())
        }
    } catch (e) {
        activity.value.items = oldActivity
    }
}


const activity = ref({
    pageNum: 1,
    pageSize: 10,
    total: 0,
    items: []
})

const handleSizeChange = (size) => {
    activity.value.pageSize = size;
    getAllActicities()
}

const handleCurrentChange = (pageNum) => {
    activity.value.pageNum = pageNum;
    getAllActicities()
}

import dayjs from 'dayjs'
import { getAllCommentLikedService, getAllLeavedCommentIdService, getAllOwnedPostService, getAllParticipatePostService } from '@/api/User';
import { getChildCommentService, getRootCommentService, leaveRootCommentService, likeCommentService, replyCommentService, softDeleteCommentService, unlikeCommentService } from '@/api/comment';
import { useTokenStore } from '@/stores/token';
import { useUIStore } from '@/stores/UIstore';
const formatTime = (time) => {
    return dayjs(time).format("YYYY-MM-DD HH:mm:ss")
}

const parseJSON = (json) => {
    if (!json) return [];
    const coverImageArray = JSON.parse(json);
    return coverImageArray
}

const getAllActicities = async () => {
    const activityData = {
        pageNum: activity.value.pageNum,
        pageSize: activity.value.pageSize,
        type: "ACTIVITY",
        status: "APPROVED"
    }
    const activities = await getReadPostService(activityData);
    activities.items.forEach(activity => {
        activity.coverImage = parseJSON(activity.coverImage)
        activity.createdAt = formatTime(activity.createdAt)
    });
    activity.value.total = activities.total;
    activity.value.items = activities.items;
}

getAllActicities()

const activityMainContainerRef = ref(null);

const handleAnchorClick = (e) => {
    e.preventDefault()
}

const selectedActivity = ref({});
const drawerVisible = ref(false);

const openDrawer = (activity) => {
    selectedActivity.value = activity;
    drawerVisible.value = true;
    if (!activityCommentInfo.value[activity.id]) {
        getRootComment(activity.id);
    }
}

const activityCommentInfo = ref({});

const getRootComment = async (activityId) => {
    if (tokenStore.token === '') {
        return
    }
    if (!activityCommentInfo.value[activityId]) {
        activityCommentInfo.value[activityId] = {
            items: [],
            orderType: "LIKES"
        }
    }
    const rootComment = await getRootCommentService(activityId, activityCommentInfo.value[activityId].orderType)
    activityCommentInfo.value[activityId].items = rootComment;
}

const isEmptyComments = (activityId) => {
    const isEmpty = !activityCommentInfo.value[activityId] || activityCommentInfo.value[activityId].items.length === 0;
    return isEmpty
}

const toggleOrderType = (activityId) => {
    if (activityCommentInfo.value[activityId].orderType === "LIKES") {
        activityCommentInfo.value[activityId].orderType = "TIME"
    } else {
        activityCommentInfo.value[activityId].orderType = "LIKES"
    }
    getRootComment(activityId)
}

const hasRightDeleteComment = (comment) => {
    if (comment.isDeleted === 1) {
        return false;
    }
    if (isAdmin()) {
        return true;
    }
    if (currLeavedCommentId.value.has(comment.id)) {
        return true;
    }
    return false;
}

const promptDeleteComment = (comment) => {
    ElMessageBox.prompt('Please provide a reason for deleting this comment.', 'Reason', {
        confirmButtonText: 'Delete',
        confirmButtonType: 'danger',
        cancelButtonText: 'Cancel',
    })
        .then(({ value }) => {
            deleteComment(comment, value)
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

const confirmDeleteComment = (comment) => {
    ElMessageBox.confirm(
        'Are you sure you want to delete this comment?',
        'Warning',
        {
            confirmButtonText: 'Delete',
            confirmButtonType: 'danger',
            cancelButtonText: 'Cancel',
            type: 'warning',
        }
    )
        .then(() => {
            deleteComment(comment, null)
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

const deleteComment = async (comment, reason) => {
    comment.isDeleted = 1;
    try {
        if (reason === null) {
            await softDeleteCommentService(comment.id, null)
        } else {
            await softDeleteCommentService(comment.id, reason.trim() === '' ? "The comment violated the community's requirements" : reason.trim())
        }
    } catch (e) {
        comment.isDeleted = 0;
    }
}

const curUserCommentLiked = ref(new Set());

const getAllCommentLiked = async () => {
    const commentIdList = await getAllCommentLikedService();
    curUserCommentLiked.value = new Set(commentIdList);
}

const isCommentLiked = (commentId) => {
    return curUserCommentLiked.value.has(commentId);
}

const likeComment = async (comment) => {
    if (comment.isDeleted === 1) {
        ElMessage({
            type: 'error',
            message: 'This comment has been deleted'
        })
        return
    }
    curUserCommentLiked.value.add(comment.id)
    comment.likes++;
    try {
        await likeCommentService(comment.id);
    } catch (e) {
        curUserCommentLiked.value.delete(comment.id);
        comment.likes--;
    }
}

const unlikeComment = async (comment) => {
    if (comment.isDeleted === 1) {
        ElMessage({
            type: 'error',
            message: 'This comment has been deleted'
        })
        return
    }
    curUserCommentLiked.value.delete(comment.id);
    comment.likes--;
    try {
        await unlikeCommentService(comment.id);
    } catch (e) {
        curUserCommentLiked.value.add(comment.id);
        comment.likes++;
    }
}

const childCommentInfo = ref({});
const expandedChildComments = ref(new Set());

const toggleChildExpanded = (rootId) => {
    if (expandedChildComments.value.has(rootId)) {
        expandedChildComments.value.delete(rootId)
    } else {
        expandedChildComments.value.add(rootId);
        if (childCommentInfo.value[rootId]) {
            return
        }
        childCommentInfo.value[rootId] = {
            items: [],
            pageNum: 1,
            size: 10,
            total: 0
        }
        getChildComment(rootId);
    }
}

const getChildComment = async (rootId) => {
    if (!childCommentInfo.value[rootId]) {
        childCommentInfo.value[rootId] = {
            items: [],
            pageNum: 1,
            size: 10,
            total: 0
        }
    }
    const childComment = await getChildCommentService(childCommentInfo.value[rootId].pageNum, childCommentInfo.value[rootId].size, rootId)
    childCommentInfo.value[rootId].items = childComment.items
    childCommentInfo.value[rootId].total = childComment.total
}

const handleCommentSizeChange = (rootCommentId, size) => {
    childCommentInfo.value[rootCommentId].size = size;
    getChildComment(rootCommentId);
}

const handleCommentCurrentChange = (rootCommentId, pageNum) => {
    childCommentInfo.value[rootCommentId].pageNum = pageNum;
    getChildComment(rootCommentId);
}


const replyCommentContent = ref({});
const replyCommentExpanded = ref(new Set());
const replyCommentReplying = ref(new Set())

const isReplyCommentExpanded = (commentId) => {
    return replyCommentExpanded.value.has(commentId);
}

const isReplyCommentReplying = (commentId) => {
    return replyCommentReplying.value.has(commentId)
}

const toggleReplyCommentExpanded = (commentId) => {
    if (isReplyCommentExpanded(commentId)) {
        replyCommentExpanded.value.delete(commentId);
    } else {
        replyCommentExpanded.value.add(commentId);
    }
}

const currLeavedCommentId = ref(new Set());

const getAllLeavedCommentId = async () => {
    const commentIdList = await getAllLeavedCommentIdService();
    currLeavedCommentId.value = new Set(commentIdList);
}

const replyToComment = async (selectedActivity, rootComment, replyToComment) => {
    const rootCommentId = rootComment.id;
    const replyToCommentId = replyToComment.id;
    const content = replyCommentContent.value[replyToCommentId];
    if (!content || content.trim() === "") {
        ElMessage({
            type: "error",
            message: "Please enter comment content"
        })
        return
    }
    replyCommentReplying.value.add(replyToCommentId)
    try {
        await replyCommentService({
            content: content,
            parentId: rootCommentId,
            replyToCommentId: replyToCommentId
        })
        getAllLeavedCommentId();
        getChildComment(rootCommentId);
        rootComment.parentComments += 1;
        selectedActivity.comments += 1;
        ElMessage({
            type: 'success',
            message: 'reply successful'
        })
    } catch (e) {
        ElMessage({
            type: 'error',
            message: e
        })
    }
    replyCommentExpanded.value.delete(replyToCommentId);
    replyCommentContent.value[replyToCommentId] = '';
    replyCommentReplying.value.delete(replyToCommentId);
}

const replyPostCommentContent = ref({});
const replyPostCommentExpanded = ref(new Set());
const replyPostCommentReplying = ref(new Set())

const isReplyPostCommentExpanded = (postId) => {
    return replyPostCommentExpanded.value.has(postId);
}

const isReplyPostCommentReplying = (forumId) => {
    return replyPostCommentReplying.value.has(forumId)
}

const toggleReplyPostCommentExpanded = (postId) => {
    if (tokenStore.token === '') {
        UIStore.openLogin("Please Login");
        drawerVisible.value = false;
        return
    }
    if (isReplyPostCommentExpanded(postId)) {
        replyPostCommentExpanded.value.delete(postId);
    } else {
        replyPostCommentExpanded.value.add(postId);
    }
}

const replyToPost = async (forum) => {
    const forumId = forum.id;
    const content = replyPostCommentContent.value[forumId]
    if (!content || content.trim() === "") {
        ElMessage({
            type: "error",
            message: "Please enter comment content"
        })
        return
    }
    replyPostCommentReplying.value.add(forumId);
    try {
        await leaveRootCommentService({
            postId: forumId,
            content: content
        })
        getAllLeavedCommentId();
        getRootComment(forumId);
        forum.comments += 1;
        ElMessage({
            type: 'success',
            message: 'reply successful'
        })
    } catch (e) {
        ElMessage({
            type: 'error',
            message: e
        })
    }
    replyPostCommentExpanded.value.delete(forumId);
    replyPostCommentContent.value[forumId] = '';
    replyPostCommentReplying.value.delete(forumId);
}

const getCurrentUserOpsInfo = () => {
    if (tokenStore.token !== '') {
        try {
            getAllOwnedPost();
            getAllCommentLiked();
            getAllParticipatePost();
            getAllLeavedCommentId();
        } catch (e) {
            ElMessage({
                type: 'error',
                message: e
            })
        }
    }
}
getCurrentUserOpsInfo()

</script>

<template>
    <el-drawer v-model="drawerVisible" size="50%" :title="selectedActivity.title" direction="rtl" class="demo-drawer">
        <div class="drawerActivityContainer">
            <div class="drawerActivityInfoContainer">
                <div class="userInfoContainer">
                    <img :src="selectedActivity.userAvatar" alt="user avatar">
                    <div class="activityUserInfoBlock">
                        <div class="activityUsernameBox">
                            <p>{{ selectedActivity.username }}</p>
                            <el-tag type="primary" size="small" v-if="selectedActivity.role === 'ADMIN'">Admin</el-tag>
                            <el-tag type="danger" size="small"
                                v-if="selectedActivity.userStatus === 'BANNED'">Banned</el-tag>
                        </div>
                        <p class="createdAtTime">{{ selectedActivity.createdAt }}</p>
                    </div>
                </div>
                <div class="avtivityOpsInfoContinaer">
                    <div class="avtivityOpsInfoBox"
                        style="background-color: rgb(209, 237, 196); border: 1px solid rgb(149, 212, 117)">
                        <span class="material-symbols-outlined">groups</span>
                        {{ selectedActivity.participants }}
                    </div>
                    <div class="avtivityOpsInfoBox"
                        style="background-color: rgb(222, 223, 224); border: 1px solid rgb(177, 179, 184)">
                        <span class="material-symbols-outlined">comment</span>
                        {{ selectedActivity.comments }}
                    </div>
                    <el-button type="danger" plain round :icon="Delete"
                        v-if="hasRightDelete(selectedActivity.id)" @click="isAdmin() ? promptDeletePost(selectedActivity.id) : confirmDeletePost(selectedActivity.id)">Delete</el-button>
                </div>
            </div>
            <div class="drawerImgContainer">
                <el-carousel trigger="click">
                    <el-carousel-item v-for="(img, index) in selectedActivity.coverImage" :key="index">
                        <el-image :src="img" alt="cover image" :preview-teleported="true" :zoom-rate="1.2"
                            :max-scale="7" :min-scale="0.2" :preview-src-list="selectedActivity.coverImage"
                            show-progress :initial-index="index" fit="cover"></el-image>
                    </el-carousel-item>
                </el-carousel>
            </div>
            <div class="drawerContentContainer" v-html="selectedActivity.content">
            </div>
            <div class="drawerActivityOpsContainer">
                <el-button :type="isParticipate(selectedActivity.id) ? 'danger' : 'primary'" plain round :icon="Star" size="large" @click="isParticipate(selectedActivity.id) ? leaveActivity(selectedActivity) : joinActivity(selectedActivity)">{{ isParticipate(selectedActivity.id) ? "Leave Activity" : "Join Activity"}}</el-button>
                <el-button type="primary" :plain="!isReplyPostCommentExpanded(selectedActivity.id)" round :icon="Position" size="large"
                    @click="toggleReplyPostCommentExpanded(selectedActivity.id)">Leave Comment</el-button>
            </div>
            <el-divider>
                <el-icon>
                    <ChatLineRound />
                </el-icon>
            </el-divider>
            <div class="replyToPostContainer" v-show="isReplyPostCommentExpanded(selectedActivity.id)">
                <div class="commentUserInfoContainer">
                    <img :src="userInfoStore.info.userAvatar" alt="user avatar">
                    <div class="commentUserInfoBlock">
                        <div class="commentUsernameBox">
                            <p class="commentUsername">{{ userInfoStore.info.username }}</p>
                            <el-tag type="primary" size="small"
                                v-if="userInfoStore.info.role === 'ADMIN'">Admin</el-tag>
                            <el-tag type="danger" size="small"
                                v-if="userInfoStore.info.userStatus === 'BANNED'">Banned</el-tag>
                        </div>
                        <p class="commentCreatedTime">Replying to "{{ selectedActivity.title }}"</p>
                    </div>
                </div>
                <div class="replyToPostOpsContainer">
                    <textarea v-model="replyPostCommentContent[selectedActivity.id]"></textarea>
                    <el-button @click="replyToPost(selectedActivity)" type="primary" round :icon="Position"
                        :loading="isReplyPostCommentReplying(selectedActivity.id)">Reply</el-button>
                </div>
            </div>
            <div class="drawerCommentContainer">
                <el-empty :description="tokenStore.token === '' ? 'Please Log in for more information' : 'There is no comment'" v-if="isEmptyComments(selectedActivity.id)" />
                <div class="commentContainerWapper" v-else>
                    <div class="orderTypeContainer">
                        <span class="orderTypeText"
                            :class="{ active: activityCommentInfo[selectedActivity.id].orderType === 'LIKES' }"
                            @click="toggleOrderType(selectedActivity.id)">Top</span>
                        <div class="typeDivider"></div>
                        <span class="orderTypeText"
                            :class="{ active: activityCommentInfo[selectedActivity.id].orderType === 'TIME' }"
                            @click="toggleOrderType(selectedActivity.id)">Newest</span>
                    </div>
                    <div class="commentContainer">
                        <div v-for="(comment, idx) in activityCommentInfo[selectedActivity.id].items" :key="comment.id"
                            class="commentBlockContainer">
                            <div class="commentBlock">
                                <div class="commentUserInfoContainerWapper">
                                    <div class="commentUserInfoContainer">
                                        <img :src="comment.userAvatar" alt="user avatar">
                                        <div class="commentUserInfoBlock">
                                            <div class="commentUsernameBox">
                                                <p class="commentUsername">{{ comment.username }}</p>
                                                <el-tag type="primary" size="small"
                                                    v-if="comment.role === 'ADMIN'">Admin</el-tag>
                                                <el-tag type="danger" size="small"
                                                    v-if="comment.userStatus === 'BANNED'">Banned</el-tag>
                                            </div>
                                            <p class="commentCreatedTime">{{
                                                formatTime(comment.createdAt) }}
                                            </p>
                                        </div>
                                    </div>
                                    <div class="commentDeleteBtnBox">
                                        <el-button v-if="hasRightDeleteComment(comment)"
                                            @click="isAdmin() ? promptDeleteComment(comment) : confirmDeleteComment(comment)"
                                            type="danger" plain :icon="Delete" circle />
                                    </div>
                                </div>
                                <div class="commentContentContainer">
                                    <el-tag type="info" v-if="comment.isDeleted === 1">deleted comment</el-tag>
                                    <p v-else>{{ comment.content }}</p>
                                </div>
                                <div class="commentOpsContainer">
                                    <el-row>
                                        <el-col :span="8">
                                            <div class="commentOpsBox"
                                                @click="isCommentLiked(comment.id) ? unlikeComment(comment) : likeComment(comment)">
                                                <span class="material-symbols-outlined"
                                                    :class="{ active: isCommentLiked(comment.id) }">favorite</span>
                                                {{ comment.likes }}
                                            </div>
                                        </el-col>
                                        <el-col :span="8">
                                            <div class="commentOpsBox"
                                                :class="{ active: expandedChildComments.has(comment.id) }"
                                                @click="toggleChildExpanded(comment.id)">
                                                <span class="material-symbols-outlined">comment</span>{{
                                                    comment.parentComments }}
                                            </div>
                                        </el-col>
                                        <el-col :span="8">
                                            <div class="commentOpsBox"
                                                :class="{ active: isReplyCommentExpanded(comment.id) }"
                                                @click="toggleReplyCommentExpanded(comment.id)">
                                                <span class="material-symbols-outlined">reply</span>reply
                                            </div>
                                        </el-col>
                                    </el-row>
                                </div>
                            </div>

                            <div class="replyToCommentContainer" v-show="isReplyCommentExpanded(comment.id)">
                                <div class="commentUserInfoContainer">
                                    <img :src="userInfoStore.info.userAvatar" alt="user avatar">
                                    <div class="commentUserInfoBlock">
                                        <p>
                                            <span class="commentAuthor">{{ userInfoStore.info.username }}</span>
                                            <span class="replyArrow">→</span>
                                            <span class="replyUser">@{{ comment.username }}</span>
                                        </p>
                                        <p class="commentCreatedTime">Replying to {{ comment.username }}</p>
                                    </div>
                                </div>
                                <div class="replyToPostOpsContainer">
                                    <textarea v-model="replyCommentContent[comment.id]"
                                        :placeholder="`@${comment.username}`"></textarea>
                                    <el-button @click="replyToComment(selectedActivity, comment, comment)"
                                        type="primary" round :icon="Position"
                                        :loading="isReplyCommentReplying(comment.id)">Reply</el-button>
                                </div>
                            </div>

                            <div class="childCommentContainer" v-if="expandedChildComments.has(comment.id)">
                                <div v-for="(childComment, idx) in childCommentInfo[comment.id].items"
                                    :key="childComment.id" class="childCommentBlockContainerWapper">

                                    <div class="childCommentBlockContainer"
                                        @click="toggleReplyCommentExpanded(childComment.id)">
                                        <div class="childCommentInfoBlock">
                                            <div class="commentUserInfoContainer">
                                                <img :src="childComment.userAvatar" alt="user avatar">
                                                <div class="commentUserInfoBlock">
                                                    <p>
                                                        <span class="commentAuthor">{{
                                                            childComment.username
                                                            }}</span>
                                                        <span class="replyArrow">→</span>
                                                        <span class="replyUser">@{{
                                                            childComment.replyToUsername
                                                            }}</span>
                                                    </p>
                                                    <p class="commentCreatedTime">{{
                                                        formatTime(childComment.createdAt) }}</p>
                                                </div>
                                            </div>
                                            <div class="commentContentContainer">
                                                <el-tag type="info" v-if="childComment.isDeleted === 1">deleted
                                                    comment</el-tag>
                                                <p v-else>{{ childComment.content }}</p>
                                            </div>
                                        </div>

                                        <div class="childCommentDelLikedBlock">
                                            <div class="childDeleteBox">
                                                <el-button v-if="hasRightDeleteComment(childComment)"
                                                    @click.stop="isAdmin() ? promptDeleteComment(childComment) : confirmDeleteComment(childComment)"
                                                    type="danger" size="small" plain :icon="Delete" circle />
                                            </div>
                                            <div class="childLikeBox"
                                                @click.stop="isCommentLiked(childComment.id) ? unlikeComment(childComment) : likeComment(childComment)">
                                                <span class="material-symbols-outlined"
                                                    :class="{ active: isCommentLiked(childComment.id) }">favorite</span>
                                                {{ childComment.likes }}
                                            </div>
                                        </div>
                                    </div>

                                    <div class="replyToChildCommentContainer"
                                        v-show="isReplyCommentExpanded(childComment.id)">
                                        <div class="commentUserInfoContainer">
                                            <img :src="userInfoStore.info.userAvatar" alt="user avatar">
                                            <div class="commentUserInfoBlock">
                                                <p>
                                                    <span class="commentAuthor">{{ userInfoStore.info.username }}</span>
                                                    <span class="replyArrow">→</span>
                                                    <span class="replyUser">@{{ childComment.username }}</span>
                                                </p>
                                                <p class="commentCreatedTime">Replying to {{ childComment.username
                                                    }}</p>
                                            </div>
                                        </div>
                                        <div class="replyToPostOpsContainer">
                                            <textarea v-model="replyCommentContent[childComment.id]"
                                                :placeholder="`@${childComment.username}`"></textarea>
                                            <el-button @click="replyToComment(selectedActivity, comment, childComment)"
                                                type="primary" round :icon="Position"
                                                :loading="isReplyCommentReplying(childComment.id)">Reply</el-button>
                                        </div>
                                    </div>
                                </div>
                                <el-pagination v-model:current-page="childCommentInfo[comment.id].pageNum"
                                    v-model:page-size="childCommentInfo[comment.id].size" class="commentPagination"
                                    size="small" :page-sizes="[10, 20, 50, 100]"
                                    :hide-on-single-page="childCommentInfo[comment.id].total === 0"
                                    layout="total, sizes, prev, pager, next" :total="childCommentInfo[comment.id].total"
                                    @size-change="(size) => handleCommentSizeChange(comment.id, size)"
                                    @current-change="(pageNum) => handleCommentCurrentChange(comment.id, pageNum)" />
                            </div>
                            <el-divider />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </el-drawer>
    <el-container>
        <el-main>
            <el-row :gutter="15">
                <el-col :span="24" v-if="activity.total === 0">
                    <div>
                        <el-empty :image-size="200"></el-empty>
                    </div>
                </el-col>

                <el-col :span="15">
                    <div id="activityContainer" ref="activityMainContainerRef">
                        <div v-for="(activity, index) in activity.items" :key="index" :id="`part${index}`"
                            class="activityBlock" @click="openDrawer(activity)">
                            <div class="imgContainer">
                                <el-image :src="activity.coverImage[0]" alt="cover image" :zoom-rate="1.2"
                                    :max-scale="7" :min-scale="0.2" :preview-src-list="activity.coverImage"
                                    show-progress :initial-index="0" fit="cover"></el-image>
                                <div class="activityBriefInfoContainerWapper">
                                    <div class="activityBriefInfoContainer">
                                        <div class="activityUserInfoContainer">
                                            <img :src="activity.userAvatar" alt="user avatar">
                                            <div class="activityUserInfoBlock">
                                                <div class="activityUsernameBox">
                                                    <p>{{ activity.username }}</p>
                                                    <el-tag type="primary" size="small"
                                                        v-if="activity.role === 'ADMIN'">Admin</el-tag>
                                                    <el-tag type="danger" size="small"
                                                        v-if="activity.userStatus === 'BANNED'">Banned</el-tag>
                                                </div>
                                                <p class="createdAtTime">{{ activity.createdAt }}</p>
                                            </div>
                                        </div>
                                        <div class="activityTitleContainer">
                                            {{ activity.title }}
                                        </div>
                                        <div class="activityAttrContainer">
                                            <div class="activityAttrBlock">
                                                <span class="material-symbols-outlined">groups</span>
                                                <span>Current Participants:</span>
                                                <span>{{ activity.participants }}</span>
                                            </div>
                                            <div class="activityAttrBlock">
                                                <span class="material-symbols-outlined">comment</span>
                                                <span>Current Comments:</span>
                                                <span>{{ activity.comments }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-col>

                <el-col :span="9" id="activityNavContainer">
                    <el-anchor :container="activityMainContainerRef" direction="vertical" type="underline"
                        @click="handleAnchorClick">
                        <el-anchor-link v-for="(activity, index) in activity.items" :key="index"
                            :href="`#part${index}`">
                            <template #default>
                                <div class="anchor-item">
                                    <div class="anchorInfoBox">
                                        <el-icon>
                                            <User />
                                        </el-icon>
                                        {{ activity.username }}
                                    </div>
                                    <div class="anchorInfoBox">
                                        <el-icon>
                                            <Star />
                                        </el-icon>
                                        {{ activity.title }}
                                    </div>
                                </div>
                            </template>
                        </el-anchor-link>
                    </el-anchor>
                    <el-pagination v-model:current-page="activity.pageNum" v-model:page-size="activity.pageSize"
                        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="activity.total"
                        @size-change="handleSizeChange" @current-change="handleCurrentChange" id="totalPagination" />
                </el-col>
            </el-row>
        </el-main>
    </el-container>
</template>

<style scoped>

#activityNavContainer {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

#activityNavContainer .el-anchor {
    max-height: 100%;
    overflow: auto;
}

#totalPagination {
    width: 100%;
    display: flex;
    justify-content: flex-end;
}

#activityContainer {
    height: calc(100vh - 40px);
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 50px;
    padding-right: 30px;
    box-sizing: border-box;
}

.activityBlock {
    width: 100%;
    aspect-ratio: 16/9;
    min-height: fit-content;
    color: #303133;
    box-sizing: border-box;
}

.imgContainer {
    width: 100%;
    height: 100%;
    overflow: hidden;
    position: relative;
    border-radius: 8px;
    border: 1px solid #DCDFE6;
    box-sizing: border-box;
    cursor: pointer;
    transition: all 0.3s ease;
}

.imgContainer .el-image {
    height: 100%;
    width: 100%;
    object-position: center;
    transform: scale(1);
    transition: all 0.3s ease;
}

.imgContainer .activityBriefInfoContainerWapper {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
    background: rgba(0, 0, 0, 0.4);
    opacity: 0;
    transition: all 0.3s ease;
}

.imgContainer:hover .el-image {
    transform: scale(1.05);
    filter: brightness(0.6);
}

.imgContainer:hover .activityBriefInfoContainerWapper {
    opacity: 1;
}

.imgContainer:hover .activityBriefInfoContainer {
    transform: translateY(0);
}

.activityBriefInfoContainerWapper .activityBriefInfoContainer {
    transform: translateY(20px);
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 5% 2%;
    color: white;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
    transition: all 0.3s ease;
}

.activityBriefInfoContainer .activityUserInfoContainer {
    display: flex;
    align-items: center;
    gap: 10px;
}

.activityUserInfoContainer img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    object-position: center;
    border: 1px solid #303133;
    border-radius: 50%;
}

.activityUserInfoContainer .activityUserInfoBlock {
    display: flex;
    flex-direction: column;
    gap: 5px;
    font-size: 22px;
    font-weight: 500;
}

.activityUserInfoContainer .activityUsernameBox {
    display: flex;
    align-items: center;
    gap: 5px;
}

.activityUserInfoContainer .createdAtTime {
    font-size: 14px;
    font-weight: 350;
    color: rgba(255, 255, 255, 0.7);
    font-style: italic;
}

.activityBriefInfoContainer .activityTitleContainer {
    font-size: 18px;
}

.activityBriefInfoContainer .activityAttrContainer {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 60px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.7);
}

.activityAttrContainer .activityAttrBlock {
    display: flex;
    align-items: center;
    gap: 8px;
}

.activityAttrContainer .material-symbols-outlined {
    font-variation-settings:
        'FILL' 0,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
    font-size: 21px;
}




.drawerActivityContainer {
    display: flex;
    flex-direction: column;
    gap: 20px;
    color: #303133;
}

.drawerActivityInfoContainer {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
}

.drawerActivityInfoContainer .userInfoContainer {
    display: flex;
    gap: 10px;
    align-items: center;
}

.drawerActivityInfoContainer img {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    object-fit: cover;
    object-position: center;
    border: 1px solid #DCDFE6;
}

.drawerActivityInfoContainer .activityUserInfoBlock {
    display: flex;
    flex-direction: column;
}

.drawerActivityInfoContainer .activityUsernameBox {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 18px;
    font-weight: 500;
}

.drawerActivityInfoContainer .createdAtTime {
    font-size: 14px;
    font-weight: 300;
    color: #909399;
    ;
    font-style: italic;
}

.drawerActivityInfoContainer .avtivityOpsInfoContinaer {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
}

.drawerActivityInfoContainer .avtivityOpsInfoBox {
    display: flex;
    gap: 5px;
    align-items: center;
    border-radius: 8px;
    min-width: 60px;
    justify-content: center;
    padding: 5px 0;
}

.drawerActivityInfoContainer .material-symbols-outlined {
    font-variation-settings:
        'FILL' 0,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
    font-size: 18px;
}

.drawerImgContainer {
    width: 100%;
    min-height: fit-content;
    aspect-ratio: 5/2;
    border-radius: 8px;
    border: 1px solid #DCDFE6;
    overflow: hidden;
}

.drawerImgContainer .el-carousel {
    width: 100%;
    height: 100%;
}

:deep(.el-carousel__container) {
    width: 100%;
    height: 100%;
}

.drawerImgContainer .el-image {
    width: 100%;
    height: 100%;
    object-position: center;
}


:deep(.drawerContentContainer) {
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




.anchor-item {
    display: flex;
    flex-direction: column;
}

.anchor-item .anchorInfoBox {
    display: flex;
    align-items: center;
    gap: 5px;
}















.commentContainerWapper {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.commentContainerWapper .orderTypeContainer {
    display: flex;
    align-items: center;
    justify-content: end;
    gap: 8px;
}

.orderTypeContainer .orderTypeText {
    font-size: 16px;
    color: #C0C4CC;
    transition: color 0.3s ease;
    cursor: pointer;
}

.orderTypeContainer .orderTypeText.active {
    color: #303133;
    font-weight: 450;
}

.orderTypeContainer .orderTypeText:hover {
    color: #409EFF;
}

.orderTypeContainer .typeDivider {
    width: 1.8px;
    height: 14px;
    background-color: #C0C4CC;
}

.commentContainer {
    display: flex;
    flex-direction: column;
}

.commentBlockContainer {
    display: flex;
    flex-direction: column;
}

.commentContainer .commentBlock {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.commentContainer .commentUserInfoContainerWapper {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
}

.commentUserInfoContainer {
    display: flex;
    align-items: center;
    gap: 10px;
}

.commentUserInfoContainer img {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: 1px solid #CDD0D6;
    object-fit: cover;
    object-position: center;
}

.commentUserInfoContainer .commentUserInfoBlock {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.commentUserInfoContainer .commentUsernameBox {
    display: flex;
    gap: 2px;
    align-items: center;
}

.commentUserInfoContainer .commentUsername {
    font-weight: 500;
}

.commentUserInfoBlock .commentCreatedTime {
    font-size: 12px;
    font-weight: 300;
    color: #909399;
    font-style: italic;
}

.commentContentContainer {
    margin-left: 52px;
}

.commentOpsContainer {
    margin-left: 52px;
}

.childCommentDelLikedBlock {
    display: flex;
    gap: 10px;
    align-items: center;
}

.commentOpsContainer .commentOpsBox,
.childLikeBox {
    display: flex;
    align-items: center;
    gap: 5px;
    color: #909399;
    cursor: pointer;
    transition: color 0.3s ease;
    width: fit-content;
}

.commentOpsContainer .commentOpsBox.active {
    color: #409EFF;
}

.commentOpsBox .material-symbols-outlined {
    font-variation-settings:
        'FILL' 0,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
    font-size: 20px;
    transition: all 0.3s ease;
}

.commentOpsBox .material-symbols-outlined.active {
    color: #F56C6C;
    font-variation-settings:
        'FILL' 1,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
}

.commentOpsContainer .commentOpsBox:hover,
.childLikeBox:hover {
    color: #409EFF;
}




.childCommentContainer {
    display: flex;
    flex-direction: column;
    /* gap: 8px; */
    margin-top: 8px;
    margin-left: 52px;
}

.childCommentContainer .childCommentBlockContainerWapper {
    display: flex;
    flex-direction: column;
}

.childCommentContainer .childCommentBlockContainer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: background-color 0.3s ease;
    border-radius: 6px;
    padding: 4px 4px 4px 0;
    cursor: pointer;
}

.childCommentContainer .childCommentInfoBlock {
    display: flex;
    flex-direction: column;
    gap: 3px;
}

.childCommentBlockContainer:hover {
    background-color: #F5F7FA;
}

.childCommentBlockContainer.active {
    background-color: #EEF1F6;
}

.commentUserInfoBlock .commentAuthor {
    font-weight: 500;
}

.commentUserInfoBlock .replyArrow {
    color: #C0C4CC;
    margin: 0 6px;
}

.commentUserInfoBlock .replyUser {
    color: #409EFF;
    font-weight: 450;
}

.childLikeBox .material-symbols-outlined {
    font-variation-settings:
        'FILL' 0,
        'wght' 350,
        'GRAD' 200,
        'opsz' 48;
    font-size: 22px;
    transition: all 0.3s ease;
}

.childLikeBox .material-symbols-outlined.active {
    color: #F56C6C;
    font-variation-settings:
        'FILL' 1,
        'wght' 350,
        'GRAD' 200,
        'opsz' 48;
}

.commentPagination {
    margin-top: 20px;
}



.replyToPostContainer,
.replyToCommentContainer,
.replyToChildCommentContainer {
    display: flex;
    flex-direction: column;
}

.replyToCommentContainer {
    margin-left: 52px;
}

.replyToPostOpsContainer {
    display: flex;
    align-items: flex-end;
    gap: 10px;
    margin-left: 52px;
}

.replyToPostOpsContainer textarea {
    width: 100%;
    height: 60px;
    overflow: auto;
    resize: none;
    outline: none;
    border: 1px solid #D4D7DE;
    border-radius: 8px;
    padding: 5px;
    transition: all 0.3s ease;
    font-family: "Inter", sans-serif;
}

.replyToPostOpsContainer textarea:focus {
    border: 1px solid #409EFF;
}

.replyToPostOpsContainer textarea::placeholder {
    color: #909399;
}
</style>