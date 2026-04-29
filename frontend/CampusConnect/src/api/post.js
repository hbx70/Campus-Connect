import request from '@/utils/request.js'

export const getReadPostService = (postData) => {
    return request.get("/post/read", { params: postData });
}

export const getPostService = (postData) => {
    return request.get("/post", { params: postData });
}

export const participatePostService = (postId) => {
    return request.post("/post/participate?postId=" + postId)
}

export const withdrawPostService = (postId) => {
    return request.delete("/post/withdraw?postId=" + postId)
}

export const deletePostService = (postId, reason) => {
    return request.delete("/post?id=" + postId + "&reason=" + reason)
}

export const requestAddPostService = (post) => {
    return request.post("/post", post)
}

export const getAllUserLikedPostService = (postId) => {
    return request.get("/post/user/likes?postId=" + postId)
}