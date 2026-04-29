<script setup>
import { deletePostService, getPostService, getReadPostService, participatePostService, withdrawPostService } from '@/api/post';
import { User, ChatDotRound, Delete, Position } from '@element-plus/icons-vue'
import { compile, ref } from 'vue'

const tokenStore = useTokenStore()
const UIStore = useUIStore()

const forums = ref([]);

const pageNum = ref(1);
const total = ref(0);
const pageSize = ref(10);

const handleSizeChange = (size) => {
    pageSize.value = size
    getAllForum()
}
const handleCurrentChange = (num) => {
    pageNum.value = num;
    getAllForum()
}

const parseJSON = (json) => {
    if (!json) return [];
    const coverImageArray = JSON.parse(json);
    return coverImageArray
}

const getAllForum = async () => {
    const forumData = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        type: 'FORUM',
        status: 'APPROVED'
    }
    let result = await getReadPostService(forumData);
    result.items.forEach(forum => {
        const coverImageJSON = forum.coverImage
        forum.coverImage = parseJSON(coverImageJSON);
    });
    total.value = result.total;
    forums.value = result.items;
}
getAllForum()

const containerRef = ref(null)

const handleClick = (e) => {
    e.preventDefault()
}

import dayjs from 'dayjs'
const formatTime = (time) => {
    return dayjs(time).format("YYYY-MM-DD HH:mm:ss")
}

import { getAllCommentLikedService, getAllLeavedCommentIdService, getAllOwnedPostService, getAllParticipatePostService } from '@/api/User';
const currUserParticipate = ref(new Set());
const getAllParticipatePost = async () => {
    const postIdList = await getAllParticipatePostService();
    currUserParticipate.value = new Set(postIdList)
}

const isLiked = (postId) => {
    return currUserParticipate.value.has(postId);
}

const participatePost = async (forum) => {
    if (tokenStore.token === '') {
        UIStore.openLogin("Please Login");
        return
    }
    currUserParticipate.value.add(forum.id);
    forum.participants++;
    try {
        await participatePostService(forum.id);
    } catch (e) {
        currUserParticipate.value.delete(forum.id);
        forum.participants--;
    }
}

const withdrawPost = async (forum) => {
    currUserParticipate.value.delete(forum.id);
    forum.participants--;
    try {
        await withdrawPostService(forum.id);
    } catch (e) {
        currUserParticipate.value.add(forum.id);
        forum.participants++;
    }
}

const postCommentExpanded = ref(new Set());

const isExpanded = (forumId) => {
    return postCommentExpanded.value.has(forumId);
}

const toggleExpanded = (forumId) => {
    if (tokenStore.token === '') {
        UIStore.openLogin("Please Login")
        return
    }
    if (postCommentExpanded.value.has(forumId)) {
        postCommentExpanded.value.delete(forumId);
    } else {
        postCommentExpanded.value.add(forumId);
        if (postCommentInfo.value[forumId]) {
            return
        }
        postCommentInfo.value[forumId] = {
            items: [],
            orderType: "LIKES"
        }
        getRootComment(forumId);
    }
}

import { getChildCommentService, getRootCommentService, leaveRootCommentService, likeCommentService, replyCommentService, softDeleteCommentService, unlikeCommentService } from '@/api/comment.js';

const postCommentInfo = ref({});

const getRootComment = async (forumId) => {
    if (!postCommentInfo.value[forumId]) {
        postCommentInfo.value[forumId] = {
            items: [],
            orderType: "LIKES"
        }
    }
    const rootComment = await getRootCommentService(forumId, postCommentInfo.value[forumId].orderType)
    postCommentInfo.value[forumId].items = rootComment;
}

const isEmptyComments = (forumId) => {
    const isEmpty = !postCommentInfo.value[forumId] || postCommentInfo.value[forumId].items.length === 0;
    return isEmpty
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

const toggleOrderType = (forumId) => {
    if (postCommentInfo.value[forumId].orderType === "LIKES") {
        postCommentInfo.value[forumId].orderType = "TIME"
    } else {
        postCommentInfo.value[forumId].orderType = "LIKES"
    }
    getRootComment(forumId)
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

const currOwnedPostId = ref(new Set());

const getAllOwnedPost = async () => {
    const postIdList = await getAllOwnedPostService();
    currOwnedPostId.value = new Set(postIdList);
}

import useUserInfoStore from '@/stores/userInfo'
const userInfoStore = useUserInfoStore();
const isAdmin = () => {
    return userInfoStore.info.role === "ADMIN";
}

const hasRightDeletePost = (postId) => {
    if (isAdmin()) {
        return true;
    }
    if (currOwnedPostId.value.has(postId)) {
        return true;
    }
    return false;
}

import { ElMessage, ElMessageBox } from 'element-plus'
import { useTokenStore } from '@/stores/token';
import { useUIStore } from '@/stores/UIstore';
const promptDeletePost = (forumId) => {
    ElMessageBox.prompt('Please provide a reason for deleting this post.', 'Reason', {
        confirmButtonText: 'Delete',
        confirmButtonType: 'danger',
        cancelButtonText: 'Cancel',
    })
        .then(({ value }) => {
            deletePost(forumId, value)
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

const confirmDeletePost = (forumId) => {
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
            deletePost(forumId, null);
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

const deletePost = async (forumId, reason) => {
    const oldForums = [...forums.value]
    forums.value = forums.value.filter(forum => forum.id !== forumId)
    try {
        if (reason === null) {
            await deletePostService(forumId, null)
        } else {
            await deletePostService(forumId, reason.trim() === '' ? "The post violated the community's requirements" : reason.trim())
        }
    } catch (e) {
        forums.value = oldForums
    }
}


const currLeavedCommentId = ref(new Set());

const getAllLeavedCommentId = async () => {
    const commentIdList = await getAllLeavedCommentIdService();
    currLeavedCommentId.value = new Set(commentIdList);
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
        UIStore.openLogin("Please Login")
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

const replyToComment = async (forum, rootComment, replyToComment) => {
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
    replyCommentExpanded.value.delete(replyToCommentId);
    replyCommentContent.value[replyToCommentId] = '';
    replyCommentReplying.value.delete(replyToCommentId);
}

const getCurrentUserOpsInfo = () => {
    if (tokenStore.token !== '') {
        try {
            getAllParticipatePost();
            getAllOwnedPost()
            getAllCommentLiked();
            getAllLeavedCommentId()
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
    <el-container>
        <el-main>
            <el-row :gutter="15">
                <el-col :span="24" v-if="total === 0">
                    <div>
                        <el-empty :image-size="200" />
                    </div>
                </el-col>

                <el-col :span="15">
                    <div id="forumBlock" ref="containerRef">
                        <div v-for="(forum, idx) in forums" :key="forum.id" :id="`part${idx}`" class="forumBox">
                            <div class="forumUserInfoOpsContainer">
                                <div class="forumUserInfoContainer">
                                    <img :src="forum.userAvatar" alt="user avatar" class="forumUserAvatar">
                                    <div class="forumUserInfoBox">
                                        <div class="forumUsernameBox">
                                            <p class="forumUsername">{{ forum.username }}</p>
                                            <el-tag type="primary" size="small"
                                                v-if="forum.role === 'ADMIN'">Admin</el-tag>
                                            <el-tag type="danger" size="small"
                                                v-if="forum.userStatus === 'BANNED'">Banned</el-tag>
                                        </div>
                                        <p class="forumCreatedTime">{{ formatTime(forum.createdAt) }}</p>
                                    </div>
                                </div>
                                <div class="forumUserOpsContainer">
                                    <el-button v-if="hasRightDeletePost(forum.id)"
                                        @click="isAdmin() ? promptDeletePost(forum.id) : confirmDeletePost(forum.id)"
                                        type="danger" :icon="Delete" plain round>Delete</el-button>
                                </div>
                            </div>
                            <div class="forumContentContainer">
                                <div class="forumContent" v-html="forum.content">
                                </div>
                                <div class="imgContainerWapper" v-if="forum.coverImage.length !== 0">
                                    <div class="imgContainer" :class="'count-' + forum.coverImage.length">
                                        <div class="imgWapper" v-for="(img, index) in forum.coverImage" :key="index">
                                            <el-image :src="img" alt="forum image" :zoom-rate="1.2" :max-scale="7" :min-scale="0.2" :preview-src-list="forum.coverImage" show-progress :initial-index="index" fit="cover"></el-image>
                                            <div class="imgMask"></div>
                                        </div>
                                    </div>
                                </div>
                                <div class="forumOpsContainer">
                                    <el-row>
                                        <el-col :span="8">
                                            <div class="forumOpsBox"
                                                @click="isLiked(forum.id) ? withdrawPost(forum) : participatePost(forum)">
                                                <span class="material-symbols-outlined"
                                                    :class="{ active: isLiked(forum.id) }">favorite</span>
                                                {{ forum.participants }}
                                            </div>
                                        </el-col>
                                        <el-col :span="8">
                                            <div class="forumOpsBox" :class="{ active: isExpanded(forum.id) }"
                                                @click="toggleExpanded(forum.id)">
                                                <span class="material-symbols-outlined">comment</span>
                                                {{ forum.comments }}
                                            </div>
                                        </el-col>
                                        <el-col :span="8">
                                            <div class="forumOpsBox" @click="toggleReplyPostCommentExpanded(forum.id)" :class="{ active: isReplyPostCommentExpanded(forum.id) }">
                                                <span class="material-symbols-outlined">reply</span>
                                                reply
                                            </div>
                                        </el-col>
                                    </el-row>
                                </div>

                                <div class="replyToPostContainer" v-show="isReplyPostCommentExpanded(forum.id)">
                                    <div class="commentUserInfoContainer">
                                        <img :src="userInfoStore.info.userAvatar" alt="user avatar">
                                        <div class="commentUserInfoBlock">
                                            <div class="commentUsernameBox">
                                                <p class="commentUsername">{{ userInfoStore.info.username }}</p>
                                                <el-tag type="primary" size="small"v-if="userInfoStore.info.role === 'ADMIN'">Admin</el-tag>
                                                <el-tag type="danger" size="small" v-if="userInfoStore.info.userStatus === 'BANNED'">Banned</el-tag>
                                            </div>
                                            <p class="commentCreatedTime">Replying to "{{ forum.title }}"</p>
                                        </div>
                                    </div>
                                    <div class="replyToPostOpsContainer">
                                        <textarea v-model="replyPostCommentContent[forum.id]"></textarea>
                                        <el-button @click="replyToPost(forum)" type="primary" round :icon="Position" :loading="isReplyPostCommentReplying(forum.id)">Reply</el-button>
                                    </div>
                                </div>

                                <div v-if="isExpanded(forum.id)">
                                    <el-empty description="There is no comment" v-if="isEmptyComments(forum.id)" />
                                    <div class="commentContainerWapper" v-else>
                                        <div class="orderTypeContainer">
                                            <span class="orderTypeText"
                                                :class="{ active: postCommentInfo[forum.id].orderType === 'LIKES' }"
                                                @click="toggleOrderType(forum.id)">Top</span>
                                            <div class="typeDivider"></div>
                                            <span class="orderTypeText"
                                                :class="{ active: postCommentInfo[forum.id].orderType === 'TIME' }"
                                                @click="toggleOrderType(forum.id)">Newest</span>
                                        </div>
                                        <div class="commentContainer">
                                            <div v-for="(comment, idx) in postCommentInfo[forum.id].items"
                                                :key="comment.id" class="commentBlockContainer">
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
                                                        <el-tag type="info" v-if="comment.isDeleted === 1">deleted
                                                            comment</el-tag>
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
                                                                <div class="commentOpsBox" :class="{ active: expandedChildComments.has(comment.id) }" @click="toggleChildExpanded(comment.id)">
                                                                    <span class="material-symbols-outlined">comment</span>{{ comment.parentComments }}
                                                                </div>
                                                            </el-col>
                                                            <el-col :span="8">
                                                                <div class="commentOpsBox" :class="{ active: isReplyCommentExpanded(comment.id) }" @click="toggleReplyCommentExpanded(comment.id)">
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
                                                                <span class="commentAuthor">{{userInfoStore.info.username}}</span>
                                                                <span class="replyArrow">→</span>
                                                                <span class="replyUser">@{{comment.username}}</span>
                                                            </p>
                                                            <p class="commentCreatedTime">Replying to {{ comment.username }}</p>
                                                        </div>
                                                    </div>
                                                    <div class="replyToPostOpsContainer">
                                                        <textarea v-model="replyCommentContent[comment.id]" :placeholder="`@${comment.username}`"></textarea>
                                                        <el-button @click="replyToComment(forum, comment, comment)" type="primary" round :icon="Position" :loading="isReplyCommentReplying(comment.id)">Reply</el-button>
                                                    </div>
                                                </div>

                                                <div class="childCommentContainer"
                                                    v-if="expandedChildComments.has(comment.id)">
                                                    <div v-for="(childComment, idx) in childCommentInfo[comment.id].items" :key="childComment.id" class="childCommentBlockContainerWapper">

                                                        <div class="childCommentBlockContainer" @click="toggleReplyCommentExpanded(childComment.id)">
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
                                                                    <el-tag type="info"
                                                                        v-if="childComment.isDeleted === 1">deleted
                                                                        comment</el-tag>
                                                                    <p v-else>{{ childComment.content }}</p>
                                                                </div>
                                                            </div>
                            
                                                            <div class="childCommentDelLikedBlock">
                                                                <div class="childDeleteBox">
                                                                    <el-button v-if="hasRightDeleteComment(childComment)"
                                                                        @click.stop="isAdmin() ? promptDeleteComment(childComment) : confirmDeleteComment(childComment)"
                                                                        type="danger" size="small" plain :icon="Delete"
                                                                        circle />
                                                                </div>
                                                                <div class="childLikeBox"
                                                                    @click.stop="isCommentLiked(childComment.id) ? unlikeComment(childComment) : likeComment(childComment)">
                                                                    <span class="material-symbols-outlined"
                                                                        :class="{ active: isCommentLiked(childComment.id) }">favorite</span>
                                                                    {{ childComment.likes }}
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div class="replyToChildCommentContainer" v-show="isReplyCommentExpanded(childComment.id)">
                                                            <div class="commentUserInfoContainer">
                                                                <img :src="userInfoStore.info.userAvatar" alt="user avatar">
                                                                <div class="commentUserInfoBlock">
                                                                    <p>
                                                                        <span class="commentAuthor">{{userInfoStore.info.username}}</span>
                                                                        <span class="replyArrow">→</span>
                                                                        <span class="replyUser">@{{childComment.username}}</span>
                                                                    </p>
                                                                    <p class="commentCreatedTime">Replying to {{ childComment.username }}</p>
                                                                </div>
                                                            </div>
                                                            <div class="replyToPostOpsContainer">
                                                                <textarea v-model="replyCommentContent[childComment.id]" :placeholder="`@${childComment.username}`"></textarea>
                                                                <el-button @click="replyToComment(forum, comment, childComment)" type="primary" round :icon="Position" :loading="isReplyCommentReplying(childComment.id)">Reply</el-button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <el-pagination
                                                        v-model:current-page="childCommentInfo[comment.id].pageNum"
                                                        v-model:page-size="childCommentInfo[comment.id].size"
                                                        class="commentPagination" size="small"
                                                        :page-sizes="[10, 20, 50, 100]"
                                                        :hide-on-single-page="childCommentInfo[comment.id].total === 0"
                                                        layout="total, sizes, prev, pager, next"
                                                        :total="childCommentInfo[comment.id].total"
                                                        @size-change="(size) => handleCommentSizeChange(comment.id, size)"
                                                        @current-change="(pageNum) => handleCommentCurrentChange(comment.id, pageNum)" />
                                                </div>
                                                <el-divider />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-col>

                <el-col :span="9" id="postNavContainer">
                    <el-anchor :container="containerRef" direction="vertical" type="underline" @click="handleClick">
                        <el-anchor-link v-for="(forum, idx) in forums" :key="forum.id" :href="`#part${idx}`">
                            <template #default>
                                <div class="anchor-item">
                                    <div class="anchorInfoBox">
                                        <el-icon>
                                            <User />
                                        </el-icon>
                                        {{ forum.username }}
                                    </div>
                                    <div class="anchorInfoBox">
                                        <el-icon>
                                            <ChatDotRound />
                                        </el-icon>
                                        {{ forum.title }}
                                    </div>
                                </div>
                            </template>
                        </el-anchor-link>
                    </el-anchor>
                    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
                        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                        @size-change="handleSizeChange" @current-change="handleCurrentChange" id="totalPagination" />
                </el-col>
            </el-row>
        </el-main>
    </el-container>
</template>

<style scoped>

#postNavContainer {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

#postNavContainer .el-anchor {
    max-height: 100%;
    overflow: auto;
}

#totalPagination {
    width: 100%;
    display: flex;
    justify-content: flex-end;
}

#forumBlock {
    height: calc(100vh - 40px);
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 50px;
}

#forumBlock .forumBox {
    width: 100%;
    padding-right: 100px;
    min-height: fit-content;
    color: #303133;
    display: flex;
    flex-direction: column;
    gap: 10px;
    box-sizing: border-box;
}

#forumBlock .forumUserInfoOpsContainer {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
}

#forumBlock .forumUserInfoContainer {
    display: flex;
    gap: 10px;
    align-items: center;
}

#forumBlock .forumUserAvatar {
    width: 50px;
    height: 50px;
    object-fit: cover;
    object-position: center;
    border-radius: 50%;
    border: 1px solid #CDD0D6;
}

#forumBlock .forumUserInfoBox {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

#forumBlock .forumUsernameBox {
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

#forumBlock .forumContentContainer {
    margin-left: 62px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.forumContentContainer .imgContainerWapper {
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

.forumOpsContainer .el-col {
    width: 100%;
}

.forumOpsContainer .forumOpsBox {
    display: flex;
    align-items: center;
    gap: 5px;
    color: #909399;
    cursor: pointer;
    transition: color 0.3s ease;
    width: fit-content;
}

.forumOpsBox.active {
    color: #409EFF;
}

.forumOpsContainer .forumOpsBox:hover {
    color: #409EFF;
}

.forumOpsContainer .material-symbols-outlined {
    font-variation-settings:
        'FILL' 0,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
    font-size: 22px;
    transition: all 0.3s ease;
}

.material-symbols-outlined.active {
    color: #F56C6C;
    font-variation-settings:
        'FILL' 1,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
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
    font-variation-settings:
        'FILL' 1,
        'wght' 350,
        'GRAD' 200,
        'opsz' 48;
}

.commentPagination {
    margin-top: 20px;
}



.replyToPostContainer, .replyToCommentContainer, .replyToChildCommentContainer {
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

:deep(.tiptap) {
    padding: 5px;
    min-height: 150px;
    outline: none;
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
</style>