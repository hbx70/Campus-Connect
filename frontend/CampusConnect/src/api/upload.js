import request from '@/utils/request.js'

export const uploadImageService = (file) => {
    return request.post("/upload", file)
}