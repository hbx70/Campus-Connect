import request from '@/utils/request.js'

export const getUserAuditService = (pageNum, pageSize, order, action, adminName) => {
    return request.get("/audit/user", {params: {
        pageNum: pageNum,
        pageSize: pageSize,
        order: order,
        adminName: adminName,
        action: action
    }});
}

export const getPostAuditService = (pageNum, pageSize, order, action, adminName) => {
    return request.get("/audit/post", {params: {
        pageNum: pageNum,
        pageSize: pageSize,
        order: order,
        adminName: adminName,
        action: action
    }})
}

export const getCommentAuditService = (pageNum, pageSize, order, adminName, content) => {
    return request.get("/audit/comment", {params: {
        pageNum: pageNum,
        pageSize: pageSize,
        order: order,
        adminName: adminName,
        content: content
    }})
}