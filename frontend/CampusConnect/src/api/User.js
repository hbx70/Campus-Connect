import request from '@/utils/request.js'

export const sendOTPService = (email) => {
    return request.post("/user/email/register?email=" + email)
}

export const registerCheckService = (username, email) => {
    return request.post("/user/register/check?username=" + username + "&email=" + email)
}

export const registerService = (registerData) => {
    return request.post("/user/register", registerData)
}

export const loginService = (loginData) => {
    return request.post("/user/login", loginData);
}

export const logoutService = () => {
    return request.post("/user/logout")
}

export const getUserInfoService = () => {
    return request.get("/user/info");
}

export const getAllParticipatePostService = () => {
    return request.get("/user/paticipatePost");
}

export const getAllCommentLikedService = () => {
    return request.get("/user/commentLiked")
}

export const getAllOwnedPostService = () => {
    return request.get("/user/ownedPost")
}

export const getAllLeavedCommentIdService = () => {
    return request.get("/user/leavedComment")
}

export const uploadUserAvatarService = (avatar) => {
    return request.patch("/user/avatar", avatar)
}

export const updateUserEmailCheckService = (email) => {
    return request.post("/user/email/check?email=" + email)
}

export const sendUpdateUserEmailOTPService = (email) => {
    return request.post("/user/email/change?email=" + email)
}

export const updateUserEmailService = (email, otp) => {
    return request.patch("/user/email?email=" + email + "&otp=" + otp)
}

export const updateUsernameService = (username) => {
    return request.patch("/user/username?username=" + username)
}

export const sendAccountHelpOTPService = (email) => {
    return request.post("/user/email/account?email=" + email);
}

export const verifyAccountHelpOTPService = (email, otp) => {
    return request.post("/user/recover/verify?email=" + email + "&otp=" + otp)
}

export const updatePasswordService = (password, token) => {
    return request.patch("/user/password", {password, token})
}

export const retrieveUsernameService = (token) => {
    return request.get("/user/recover/username?token=" + token)
}