import request from '@/utils/request.js'

export const getRootCommentService = (postId, orderBy) => {
    return request.get("/comment/root", {params: {
        postId: postId,
        orderBy: orderBy
    }})
}

export const getChildCommentService = (pageNum, pageSize, rootId) => {
    return request.get("/comment/child", {params: {
        pageNum: pageNum,
        pageSize: pageSize,
        parentId: rootId
    }})
}

export const likeCommentService = (commentId) => {
    return request.post("/comment/like?commentId=" + commentId)
}

export const unlikeCommentService = (commentId) => {
    return request.delete("/comment/unlike?commentId=" + commentId)
}

export const softDeleteCommentService = (commentId, reason) => {
    return request.patch("/comment?commentId=" + commentId + "&reason=" + reason)
}

export const leaveRootCommentService = (comment) => {
    return request.post("/comment", comment)
}

export const replyCommentService = (comment) => {
    return request.post("/comment/reply", comment)
}