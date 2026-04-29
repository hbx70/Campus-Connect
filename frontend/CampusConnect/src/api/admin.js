import request from '@/utils/request.js'

export const getAllUserInoService = (pageNum, pageSize, order, role, userStatus, username) => {
    return request.get("/admin/user", {params: {
        pageNum: pageNum,
        pageSize: pageSize,
        order: order,
        role: role,
        status: userStatus,
        username: username
    }})
}

export const banUserService = (userId, reason) => {
    return request.patch("/user/ban", null, {
        params: {
            userId,
            reason
        }
    })
}

export const activeUserService = (userId) => {
    return request.patch("/user/active", null, {
        params: {
            userId
        }
    })
}

export const getAllPostInfoService = (pageNum, pageSize, type, order, postStatus, username) => {
    return request.get("/admin/post", {params: {
        pageNum: pageNum,
        pageSize: pageSize,
        type: type,
        order: order,
        status: postStatus,
        username: username
    }})
}

export const approvePostService = (postId) => {
    return request.patch("/post/approve", null, {
        params: {postId}
    });
}

export const rejectPostService = (postId, reason) => {
    return request.patch("/post/reject", {postId, reason})
}