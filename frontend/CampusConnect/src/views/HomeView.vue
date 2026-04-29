<script setup>
import { onUnmounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Loading, Check, Position } from '@element-plus/icons-vue'
import { getUserInfoService, loginService, logoutService, registerCheckService, registerService, retrieveUsernameService, sendAccountHelpOTPService, sendOTPService, updatePasswordService, verifyAccountHelpOTPService } from '@/api/User'

import { useTokenStore } from '@/stores/token'
const tokenStore = useTokenStore();
import useUserInfoStore from '@/stores/userInfo'
const userInfoStore = useUserInfoStore();

const getUserInfo = async () => {
    const result = await getUserInfoService();
    userInfoStore.setInfo(result);
}

const getCurrentUserInfo = () => {
    if (tokenStore.token !== '') {
        try {
            getUserInfo()
        } catch (e) {
            ElMessage({
                type: 'error',
                message: e
            })
        }
    }
    userInfoStore.removeInfo();
}
getCurrentUserInfo()

const route = useRoute()
const router = useRouter()
const goTo = (path) => {
    if (path === '/user') {
        if (tokenStore.token === '') {
            UIStore.openLogin("Please Login")
            return
        }
    }
    router.push(path)
}

const item = {
    date: '2016-05-02',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
}
const tableData = ref(Array.from({ length: 20 }).fill(item))

const handleCreatedCommand = (command) => {
    if (tokenStore.token === '') {
        UIStore.openLogin("Please log in first to post your " + command)
        return
    }
    currentEditForm.value = command;
    formDialog.value = true
    editor.value.commands.setContent(postForm.value[command].content)
}

const formLabelWidth = '80px'

const formDialog = ref(false)

const loading = ref(false)

const postFormRules = {
    title: [
        { required: true, message: "Please enter forum title", trigger: "blur" },
        { max: 80, message: "The length of the title must be within 80", trigger: "blur" }
    ],
    content: [
        { required: true, message: "Please enter forum content", trigger: "change" },
    ],
}

const currentEditForm = ref('');

const createInitForumForm = () => ({
    title: '',
    content: '<ul><li><p><span data-name="memo" data-type="emoji">📝</span> Start writing...</p></li><li><p><span data-name="national_park" data-type="emoji">🏞</span> You may add image by paste or drop image into this box</p></li><li><p><span data-name="check_mark_button" data-type="emoji">✅</span> After publishing, the notification will be sent...</p></li></ul><p></p>',
    type: 'FORUM',
    coverImage: []
})

const createInitActivityForm = () => ({
    title: '',
    content: '<ul><li><p><span data-name="memo" data-type="emoji">📝</span> Start writing...</p></li><li><p><span data-name="national_park" data-type="emoji">🏞</span> You may add image by paste or drop image into this box</p></li><li><p><span data-name="exclamation" data-type="emoji">❗</span>The first image will be the cover image</p></li><li><p><span data-name="check_mark_button" data-type="emoji">✅</span> After publishing, the notification will be sent...</p></li></ul><p></p>',
    type: 'ACTIVITY',
    coverImage: []
})

const postForm = ref({
    forum: createInitForumForm(),
    activity: createInitActivityForm()
})

const validateForum = () => {
    if (postForm.value[currentEditForm.value].title.trim() === '') {
        throw new Error("Please enter forum title");
    }
    if (editor.value.getText().trim() === '') {
        throw new Error('Please enter post content');
    }
    if (currentEditForm.value === 'activity' && postForm.value[currentEditForm.value].coverImage.length === 0) {
        throw new Error("Please upload cover image")
    }
    if (postForm.value[currentEditForm.value].coverImage.length > 9) {
        throw new Error('Maximum 9 images');
    }
}

const handleSubmitForum = async () => {
    try {
        validateForum()
    } catch (e) {
        ElMessage({
            type: 'error',
            message: e.message
        })
        return
    }
    loading.value = true
    try {
        postForm.value[currentEditForm.value].coverImage = postForm.value[currentEditForm.value].coverImage.filter(img => !img.loading).map(img => img.url)
        await requestAddPostService(postForm.value[currentEditForm.value])
        ElNotification({
            title: 'Success',
            message: "Wait for the administrator's review and receive an email notification",
            duration: 0,
            type: 'success'
        })
        formDialog.value = false;
        setTimeout(() => {
            let initFormFunction;
            if (currentEditForm.value === 'forum') {
                initFormFunction = createInitForumForm;
            } else {
                initFormFunction = createInitActivityForm;
            }
            postForm.value[currentEditForm.value] = initFormFunction();
            editor.value.commands.setContent(postForm.value[currentEditForm.value].content)
        }, 400)
    } catch (e) {
        ElMessage({
            type: 'error',
            message: e
        })
    }
    loading.value = false
}

const handleCloseDialog = (done) => {
    if (loading.value) {
        return
    }
    ElMessageBox.confirm('Do you want to publish?')
    .then(() => {
        handleSubmitForum()
    })
    .catch(() => {
        // catch error
    })
}

const cancelForm = () => {
    ElMessageBox.confirm(
        '<p>Would you like to save your current progress as a draft?</p><p style="color: #909399; font-size: 12px; font-weight: 200;">Images saved in drafts may be periodically cleaned up</p>',
        'Confirm',
        {
            distinguishCancelAndClose: true,
            confirmButtonText: 'Save as Draft',
            cancelButtonText: 'Discard',
            dangerouslyUseHTMLString: true,
        }
    )
        .then(() => {
            loading.value = false
            formDialog.value = false
            ElMessage({
                type: 'primary',
                message: 'draft saved'
            })
        })
        .catch((action) => {
            if (action === 'cancel') {
                let initFormFunction;
                if (currentEditForm.value === 'forum') {
                    initFormFunction = createInitForumForm;
                } else {
                    initFormFunction = createInitActivityForm;
                }
                loading.value = false
                formDialog.value = false
                setTimeout(() => {
                    postForm.value[currentEditForm.value] = initFormFunction()
                    editor.value.commands.setContent(postForm.value[currentEditForm.value].content)
                }, 400)
                ElMessage({
                    type: 'primary',
                    message: 'draft discard'
                })
            } else {
                ElMessage({
                    type: 'info',
                    message: "canceled"
                })
            }
        })
}


const removePreviewImg = (img) => {
    if (img.loading) {
        return
    }
    const index = postForm.value[currentEditForm.value].coverImage.indexOf(img);
    if (index !== -1) {
        postForm.value[currentEditForm.value].coverImage.splice(index, 1);
        ElMessage({
            type: 'success',
            message: 'Removed successfully'
        })
    }
    // postForm.value[currentEditForm.value].coverImage = postForm.value[currentEditForm.value].coverImage.filter(img => { return img !== imgURL })
}

import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Link from '@tiptap/extension-link'
import Superscript from '@tiptap/extension-superscript'
import Subscript from '@tiptap/extension-subscript'
import { Picker } from 'emoji-mart'
import data from '@emoji-mart/data'
import Emoji from '@tiptap/extension-emoji'
import FileHandler from '@tiptap/extension-file-handler'
import Image from '@tiptap/extension-image'
import { uploadImageService } from '@/api/upload'

import DOMPurify from 'dompurify'
import { requestAddPostService } from '@/api/post'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/UIstore'

const editor = useEditor({
    extensions: [
        StarterKit.configure({
            link: false
        }),
        Link.configure({
            openOnClick: false,
            autolink: true,
            linkOnPaste: true,
            defaultProtocol: 'https',
            HTMLAttributes: {
                target: '_blank',
                rel: 'noopener noreferrer nofollow',
            }
        }),
        Superscript,
        Subscript,
        Emoji.configure({
            enableEmoticons: true
        }),
        Image,
        FileHandler.configure({
            allowedMimeTypes: ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'],
            onDrop: async (imgEditor, files, pos) => {
                await handleUpload(imgEditor, files)
            },

            onPaste: async (imgEditor, files) => {
                await handleUpload(imgEditor, files)
            }
        })
    ],
    content: '',
    onUpdate({ editor }) {
        const rawHTML = editor.getHTML();
        postForm.value[currentEditForm.value].content = DOMPurify.sanitize(rawHTML, {
            ALLOWED_TAGS: ['h1', 'h2', 'h3', 'p', 'strong', 'em', 's', 'u', 'span', 'pre', 'code', 'br', 'ol', 'ul', 'li', 'a', 'sub', 'sup'],
            ALLOWED_ATTR: ['href', 'target', 'rel'],
            ALLOW_DATA_ATTR: false,
        })
    },
})

const handleUpload = async (editor, files) => {
    if (files == null) {
        return
    }
    for (const file of files) {
        if (file.size > 5 * 1024 * 1024) {
            ElMessage({
                type: 'error',
                message: 'Maximum 5 MB per image'
            })
            return
        }
        if (postForm.value[currentEditForm.value].coverImage.length >= 9) {
            ElMessage({
                type: 'error',
                message: 'Maximum 9 images',
            })
            return
        }
        const tempImg = { url: '', loading: true }
        postForm.value[currentEditForm.value].coverImage.push(tempImg)
        try {
            const forumData = new FormData()
            forumData.append("file", file)
            const imageURL = await uploadImageService(forumData)
            const index = postForm.value[currentEditForm.value].coverImage.indexOf(tempImg);
            postForm.value[currentEditForm.value].coverImage[index] = {
                url: imageURL,
                loading: false
            }

        } catch (e) {
            ElMessage({
                type: 'error',
                message: e
            })
            const index = postForm.value[currentEditForm.value].coverImage.indexOf(tempImg);
            if (index !== -1) postForm.value[currentEditForm.value].coverImage.splice(index, 1)
        }
    }
}

const linkPopVisible = ref(false)
const url = ref()

const popLinkCon = () => {
    const previousUrl = editor.value.getAttributes('link').href
    url.value = previousUrl
    linkPopVisible.value = true
}

const setLink = () => {
    if (url.value === null) {
        return
    }
    if (url.value.trim() === '') {
        this.editor.chain().focus().extendMarkRange('link').unsetLink().run()
        return
    }
    editor.value.chain().focus().extendMarkRange('link').setLink({ href: url.value }).run()
    linkPopVisible.value = false
}


const showPicker = ref(false)
const pickerContainer = ref(null)
let pickerInstance = null

const togglePicker = async () => {
    showPicker.value = !showPicker.value
    if (showPicker.value) {
        initPicker()
    }
}

const initPicker = () => {
    if (pickerInstance) return
    pickerInstance = new Picker({
        data,
        parent: pickerContainer.value,
        onEmojiSelect: (emoji) => {
            insertEmoji(emoji.native)
            showPicker.value = false
        }
    })
}

const insertEmoji = (nativeEmoji) => {
    if (editor.value) {
        editor.value.chain().focus().insertContent(nativeEmoji).run();
    }
}

const fileInput = ref(null);

const triggerUpload = async () => {
    fileInput.value.click()
}

const onFileChange = async (event) => {
    const files = event.target.files

    await handleUpload(editor.value, files)

    event.target.value = null
}

const UIStore = useUIStore();

const createInitValidForm = () => {
    return {
        login: {
        username: true,
        password: true
        },
        register: {
            username: true,
            email: true,
            password: true,
            rePassword: true,
            otp: true
        },
        accHelp: {
            email: true,
            otp: true,
            password: true,
            rePassword: true
        }
    }
}

const isValid = ref(createInitValidForm());

const currEditOTPForm = ref('register');

const createInitAccForm = () => {
    return {
        register: {
            username: '',
            email: '',
            password: '',
            rePassword: '',
            userAvatar: 'https://img.campusconnect.one/avatars/default.svg',
            otp: ''
        },
        login: {
            username: '',
            password: ''
        },
        accHelp: {
            email: '',
            otp: '',
            password: '',
            rePassword: ''
        }
    }
}

const accountForum = ref(createInitAccForm())

const handleInputCode = (e) => {
    isValid.value[currEditOTPForm.value].otp = true;
    accountForum.value[currEditOTPForm.value].otp = e.target.value.replace(/[^\d]/g, '');
    if (accountForum.value[currEditOTPForm.value].otp.trim().length === 6) {
        currEditOTPForm.value === 'register' ? register() : verifyAccHelpCode();
    }
};

const isAnyLoading = () => {
    return formLoading.value.login || formLoading.value.otp || formLoading.value.register || formLoading.value.accHelpEmail || formLoading.value.accHelp || formLoading.value.resetPwd || formLoading.value.retrieveUsername
}

const handleCloseAccountOps = () => {
    if (isAnyLoading()) {
        return
    }
    ElMessageBox.confirm(
        `Are you sure you want to leave this page?`,
        'Warning',
        {
            confirmButtonText: 'Leave',
            cancelButtonText: 'Cancel',
            type: 'warning',
        }
    )
    .then(() => {
        accountForum.value = createInitAccForm();
        isValid.value = createInitValidForm();
        UIStore.closeAccountOps();
    })
    .catch(() => {
    })
}

const jumpAccountOpsTo = (location) => {
    UIStore.jumpAccountOpsTo(location);
}

const validateRegisterForm = () => {
    if (accountForum.value.register.username.trim() === '') {
        isValid.value.register.username = false;
        throw new Error("Please enter username")
    } else if (accountForum.value.register.email.trim() === '') {
        isValid.value.register.email = false;
        throw new Error("Please enter email address")
    } else if (accountForum.value.register.password.trim() === '') {
        isValid.value.register.password = false;
        throw new Error("Please enter password")
    } else if (accountForum.value.register.rePassword.trim() === '') {
        isValid.value.register.rePassword = false;
        throw new Error("Please confirm your password")
    } else if (accountForum.value.register.username.trim().length < 3 || accountForum.value.register.username.trim().length > 16) {
        isValid.value.register.username = false;
        throw new Error("The length of the username must be between 3 and 16")
    } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(accountForum.value.register.email.trim())) {
        isValid.value.register.email = false;
        throw new Error("Please enter valid email address");
    }
    else if (accountForum.value.register.password.trim().length < 6) {
        isValid.value.register.password = false;
        throw new Error("The lenght of the password must be at least 6")
    } else if (accountForum.value.register.password.trim() !== accountForum.value.register.rePassword.trim()) {
        isValid.value.register.password = false;
        isValid.value.register.rePassword = false;
        throw new Error("Password does not match")
    }
}

const verifyRegisterForm = async () => {
    if (formLoading.value.otp) {
        return
    }
    formLoading.value.otp = true;
    try {
        validateRegisterForm()
        await registerCheckService(accountForum.value.register.username, accountForum.value.register.email)
    } catch (e) {
        if (e.code === 1) {
            if (e.message === 'Username has been occupied') {
                isValid.value.register.username = false;
            } else if (e.message === 'Email has been registered') {
                isValid.value.register.email = false;
            }
            formLoading.value.otp = false
            return
        }
        ElMessage({
            type: 'error',
            message: e.message
        })
        formLoading.value.otp = false
        return
    }
    try {
        await sendOTPService(accountForum.value.register.email);
        currEditOTPForm.value = 'register'
        UIStore.jumpAccountOpsTo('otp');
        startCountdown(60);
    } catch (e) {}
    formLoading.value.otp = false
}

const confirmLogout = () => {
    ElMessageBox.confirm(
        'Are you sure you want to logout?',
        'Warning',
        {
            confirmButtonText: 'Logout',
            confirmButtonType: 'danger',
            cancelButtonText: 'Cancel',
            type: 'warning',
        }
    )
    .then(() => {
        logout()
    })
    .catch(() => {
    })
}

const logout = async () => {
    await logoutService();
    tokenStore.removeToken();
    location.reload()
    ElMessage({
        type: 'success',
        message: 'Logout successful'
    })
}

const validateLoginForm = () => {
    if (accountForum.value.login.username.trim() === '') {
        isValid.value.login.username = false;
        throw new Error("Please enter username")
    } else if (accountForum.value.login.password.trim() === '') {
        isValid.value.login.password = false;
        throw new Error("Please enter password")
    } else if (accountForum.value.login.username.trim().length < 3 || accountForum.value.login.username.trim().length > 16) {
        isValid.value.login.username = false;
        throw new Error("The length of the username must be between 3 and 16")
    } else if (accountForum.value.login.password.trim().length < 6) {
        isValid.value.login.password = false;
        throw new Error("The lenght of the password must be at least 6")
    }
}

const login = async () => {
    if (formLoading.value.login) {
        return
    }
    formLoading.value.login = true
    try {
        validateLoginForm()
    } catch (e) {
        ElMessage({
            type: 'error',
            message: e.message
        })
        formLoading.value.login = false;
        return
    }
    try {
        const token = await loginService(accountForum.value.login);
        tokenStore.setToken(token);
        accountForum.value = createInitAccForm();
        location.reload()
        ElMessage({
            type: 'success',
            message: 'Log in successfully'
        })
    } catch (e) {
        isValid.value.login.username = false;
        isValid.value.login.password = false;
    }
    formLoading.value.login = false
}

const register = async () => {
    if (formLoading.value.register) {
        return
    }
    if (accountForum.value.register.otp.trim().length !== 6) {
        ElMessage({
            type: 'error',
            message: 'Please enter 6 digits verification code'
        })
        isValid.value.register.otp = false;
        return
    }
    formLoading.value.register = true;
    try {
        await registerService(accountForum.value.register);
        ElMessage.success("Register successful")
        accountForum.value = createInitAccForm();
        jumpAccountOpsTo('login');
    } catch (e) {
        isValid.value.register.otp = false;
    }
    formLoading.value.register = false;
}

const formLoading = ref({
    login: false,
    otp: false,
    register: false,
    accHelpEmail: false,
    accHelp: false,
    resetPwd: false,
    retrieveUsername: false
})

const clearErrorHint = (typeOfForm, blank) => {
    isValid.value[typeOfForm][blank] = true;
}

const resendPercentage = ref(0);

const resendColors = [
    { color: '#909399', percentage: 99 },
    { color: '#5cb87a', percentage: 100 },
]

const resendLoading = ref(false)
const countdown = ref();
let timer = null

const startCountdown = (seconds) => {
    resendPercentage.value = 0
    countdown.value = seconds;
    if (timer) {
        clearInterval(timer)
    }
    timer = setInterval(() => {
        countdown.value--
        resendPercentage.value = Math.min(100, Math.ceil(((seconds - countdown.value) / seconds) * 100))
        if (countdown.value <= 0) {
            clearInterval(timer)
            timer = null
        }
    }, 1000)
}

onUnmounted(() => {
    if (timer) clearInterval(timer)
})

const resendCode = async () => {
    if (resendLoading.value) {
        return
    }
    resendLoading.value = true
    try {
        if (currEditOTPForm.value === 'register') {
            await sendOTPService(accountForum.value.register.email)
        } else {
            await sendAccountHelpOTPService(accountForum.value.accHelp.email)
        }
        ElMessage.success("Send successfully")
        startCountdown(60)
    } catch (e) {}
    resendLoading.value = false
}

const validateEmail = () => {
    const email = accountForum.value.accHelp.email.trim()
    if (email === '') {
        isValid.value.accHelp.email = false;
        throw new Error("Please enter email address")
    } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)) {
        isValid.value.accHelp.email = false;
        throw new Error("Please enter valid email address")
    }
}

const verifyAccEmail = async () => {
    if (formLoading.value.accHelpEmail) {
        return
    }
    formLoading.value.accHelpEmail = true
    try {
        validateEmail()
    } catch (e) {
        ElMessage({
            type: 'error',
            message: e.message
        })
        formLoading.value.accHelpEmail = false
        return
    }
    try {
        await sendAccountHelpOTPService(accountForum.value.accHelp.email)
        currEditOTPForm.value = 'accHelp'
        jumpAccountOpsTo('otp');
        startCountdown(60)
    } catch (e) {}
    formLoading.value.accHelpEmail = false
}

const verifyAccHelpCode = async () => {
    if (formLoading.value[currEditOTPForm.value]) {
        return
    }
    if (accountForum.value.accHelp.otp.trim().length !== 6) {
        ElMessage({
            type: 'error',
            message: 'Please enter 6 digits verification code'
        })
        isValid.value.accHelp.otp = false;
        return
    }
    formLoading.value[currEditOTPForm.value] = true;
    try {
        const token = await verifyAccountHelpOTPService(accountForum.value.accHelp.email, accountForum.value.accHelp.otp);
        sessionStorage.setItem("accountToken", token);
        jumpAccountOpsTo('accountOps')
        accountForum.value = createInitAccForm();
    } catch (e) {}
    formLoading.value[currEditOTPForm.value] = false;
    
}

const currAccHelpOps = ref('');

const switchAccHelpOpsTo = (location) => {
    currAccHelpOps.value = location
    accountForum.value = createInitAccForm();
    if (location === 'username') {
        retrieveUsername()
    }
}

const retrievedUsername = ref('');

const retrieveUsername = async () => {
    if (formLoading.value.retrieveUsername) {
        return
    }
    formLoading.value.retrieveUsername = true;
    try {
        const token = sessionStorage.getItem("accountToken");
        if (token) {
            const username = await retrieveUsernameService(token)
            retrievedUsername.value = username
        } else {
            ElMessage.error("Your session has expired. Please try again.")
        }
    } catch (e) {}
    formLoading.value.retrieveUsername = false;
}

const validateResetPassword = () => {
    const password = accountForum.value.accHelp.password.trim()
    const rePassword = accountForum.value.accHelp.rePassword.trim();
    if (password === '') {
        isValid.value.accHelp.password = false;
        throw new Error("Please enter password")
    } else if (rePassword === ''){
        isValid.value.accHelp.rePassword = false
        throw new Error("Please confrim your password")
    } else if (password.length < 6) {
        isValid.value.accHelp.password = false;
        throw new Error("The lenght of the password must be at least 6")
    } else if (password !== rePassword) {
        isValid.value.accHelp.password = false;
        isValid.value.accHelp.rePassword = false;
        throw new Error("Password does not match")
    }
}

const resetPassword = async () => {
    if (formLoading.value.resetPwd) {
        return
    }
    try {
        validateResetPassword()
    } catch (e) {
        ElMessage({
            type: 'error',
            message: e.message
        })
        return
    }
    formLoading.value.resetPwd = true
    try {
        const token = sessionStorage.getItem("accountToken");
        if (token) {
            await updatePasswordService(accountForum.value.accHelp.password, token);
            switchAccHelpOpsTo('')
            accountForum.value = createInitAccForm();
            ElMessage({
                type: 'success',
                message: 'Update successfully'
            })
        } else {
            ElMessage.error("Your session has expired. Please try again.")
        }
    } catch (e) {}
    formLoading.value.resetPwd = false
}

const clearSessionAndRedirect = () => {
    if (formLoading.value.resetPwd || formLoading.value.retrieveUsername) {
        return
    }
    sessionStorage.removeItem("accountToken")
    jumpAccountOpsTo("login")
}

</script>

<template>
    <div v-if="UIStore.accountOpsVisible" class="accountOpsContainer" @click="handleCloseAccountOps">
        <div class="accountOpsBlock" @click.stop>
            <div class="accountBackgroundImgContainer">
                <img src="../assets/registerView.jpg" alt="background image" class="backgroundImg">
                <img src="../assets/logoWithoutText.png" alt="logo" class="logo">
            </div>
            <div class="accountFormContainer">
                <p class="title">{{ UIStore.currentAccountOps === 'register' ? 'Register' : UIStore.currentAccountOps === 'login' ? 'Log in' : UIStore.currentAccountOps === 'otp' ? 'Verification Code' : 'Account Help'}}</p>
                <div v-if="UIStore.currentAccountOps === 'register'" class="accountForumBlockWapper">
                    <div class="accountForumBlock">
                        <div class="accountFormBlank">
                            <p :class="{error: !isValid.register.username}">Username</p>
                            <input type="text" :class="{error: !isValid.register.username}" @input="clearErrorHint('register', 'username')" v-model="accountForum.register.username" class="accountForumInputBox" />
                        </div>
                        <div class="accountFormBlank">
                            <p :class="{error: !isValid.register.email}">Email</p>
                            <input type="text" :class="{error: !isValid.register.email}"  @input="clearErrorHint('register', 'email')" v-model="accountForum.register.email" class="accountForumInputBox" />
                        </div>
                        <div class="accountFormBox">
                            <div class="accountFormBlank">
                                <p :class="{error: !isValid.register.password}">Password</p>
                                <input type="password" :class="{error: !isValid.register.password}" @input="clearErrorHint('register', 'password')" v-model="accountForum.register.password" class="accountForumInputBox" />
                            </div>
                            <div class="accountFormBlank">
                                <p :class="{error: !isValid.register.rePassword}">Confirm Password</p>
                                <input type="password" :class="{error: !isValid.register.rePassword}" @input="clearErrorHint('register', 'rePassword')" v-model="accountForum.register.rePassword" class="accountForumInputBox" />
                            </div>
                        </div>
                        <div class="linkContainerWapper">
                            <div class="linkContainer" @click="jumpAccountOpsTo('login')">
                                <p>Have an account already?</p>
                                <div class="progressBarWapper">
                                    <div class="progressBar"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="accountForumBtnContainer">
                        <button class="mainBtn" :disabled="formLoading.otp" @click="verifyRegisterForm">
                            <span v-if="formLoading.otp">
                                <el-icon :size="18" class="is-loading"><Loading /></el-icon>
                            </span>
                            <span>{{ formLoading.otp ? 'Sending' : 'Send the verification code' }}</span>
                        </button>
                        <button class="altBtn" :disabled="formLoading.otp" @click="handleCloseAccountOps">Cancel</button>
                    </div>
                </div>
                <div v-else-if="UIStore.currentAccountOps === 'otp'" class="accountForumBlockWapper">
                    <div class="accountForumBlock">
                        <p>A verification code has been sent to {{ accountForum[currEditOTPForm].email }}. Please enter it within <strong>5 minutes</strong> before it expires</p>
                        <div class="codeContainerWapper">
                            <div class="codeContainer">
                                <input v-model="accountForum[currEditOTPForm].otp" type="tel" maxlength="6" class="realInput" autofocus @input="handleInputCode" />
                                <div class="codeWapper">
                                    <div v-for="(item, index) in 6" :key="index" :class="['codeSlot', { 'active': accountForum[currEditOTPForm].otp.length === index, 'filled': accountForum[currEditOTPForm].otp.length > index, 'error': !isValid[currEditOTPForm].otp }]">
                                        {{ accountForum[currEditOTPForm].otp[index] }}
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="reSendContainer">
                            <div class="reSendBlock">
                                <el-progress type="dashboard" :percentage="resendPercentage" :color="resendColors" :width="80">
                                    <template #default="{ percentage }">
                                        <div v-if="resendPercentage < 100">
                                            <span class="progressLabel">Resnd in</span>
                                            <span class="countDownValue">{{ countdown }}s</span>
                                        </div>
                                        <div v-else>
                                            <el-icon color="#5cb87a" size="22"><Check /></el-icon>
                                        </div>
                                    </template>
                                </el-progress>
                                <el-button type="primary" plain round :icon="Position" :loading="resendLoading" :disabled="countdown > 0" @click="resendCode">Resend</el-button>
                            </div>
                        </div>
                    </div>
                    <div class="accountForumBtnContainer">
                        <button class="mainBtn" :disabled="formLoading[currEditOTPForm]" @click="register">
                            <span v-if="formLoading[currEditOTPForm]">
                                <el-icon :size="18" class="is-loading"><Loading /></el-icon>
                            </span>
                            <span>{{ formLoading[currEditOTPForm] ? 'Verify' : 'Verifing' }}</span>
                        </button>
                        <button class="altBtn" :disabled="formLoading[currEditOTPForm]" @click="currEditOTPForm === 'register' ? jumpAccountOpsTo('register') : jumpAccountOpsTo('accHelp')">Previous</button>
                    </div>
                </div>
                <div v-else-if="UIStore.currentAccountOps === 'accHelp'" class="accountForumBlockWapper">
                    <div class="accountForumBlock">
                        <p>Please enter your registered email address to retrieve your username and reset your password.</p>
                        <div class="accountFormBlank">
                            <p :class="{error: !isValid.accHelp.email}">Email</p>
                            <input type="text" @input="clearErrorHint('accHelp', 'email')" :class="{error: !isValid.accHelp.email}" v-model="accountForum.accHelp.email" class="accountForumInputBox" />
                        </div>
                    </div>
                    <div class="accountForumBtnContainer">
                        <button class="mainBtn" :disabled="formLoading.accHelpEmail" @click="verifyAccEmail">
                            <span v-if="formLoading.accHelpEmail">
                                <el-icon :size="18" class="is-loading"><Loading /></el-icon>
                            </span>
                            <span>{{ formLoading.accHelpEmail ? 'Sending' : 'Send the verification code' }}</span>
                        </button>
                        <button class="altBtn" :disabled="formLoading.accHelpEmail" @click="jumpAccountOpsTo('login')">Cancel</button>
                    </div>
                </div>
                <div v-else-if="UIStore.currentAccountOps === 'accountOps'" class="accountForumBlockWapper">
                    <div class="accountForumBlock">
                        <div class="linkContainer" @click="switchAccHelpOpsTo('password')">
                            <p>Reset Your Passowrd</p>
                            <div class="progressBarWapper">
                                <div class="progressBar"></div>
                            </div>
                        </div>
                        <div class="linkContainer" @click="switchAccHelpOpsTo('username')">
                            <p>Retrieve Your Username</p>
                            <div class="progressBarWapper">
                                <div class="progressBar"></div>
                            </div>
                        </div>
                        <div class="accountHelpOpsBlockWapper">
                            <div class="accountHelpOpsBlock" v-if="currAccHelpOps==='password'">
                                <div class="accountFormBlank">
                                    <p :class="{error: !isValid.accHelp.password}">Password</p>
                                    <input type="password" :class="{error: !isValid.accHelp.password}" @input="clearErrorHint('accHelp', 'password')" v-model="accountForum.accHelp.password" class="accountForumInputBox" />
                                </div>
                                <div class="accountFormBlank">
                                    <p :class="{error: !isValid.accHelp.rePassword}">Confirm Password</p>
                                    <input type="password" :class="{error: !isValid.accHelp.rePassword}"  @input="clearErrorHint('accHelp', 'rePassword')" v-model="accountForum.accHelp.rePassword" class="accountForumInputBox" />
                                </div>
                                <button class="mainBtn" :disabled="formLoading.resetPwd" @click="resetPassword">
                                    <span v-if="formLoading.resetPwd">
                                        <el-icon :size="18" class="is-loading"><Loading /></el-icon>
                                    </span>
                                    <span>{{ formLoading.resetPwd ? 'Reseting' : 'Reset Password' }}</span>
                                </button>
                            </div>
                            <div class="accountHelpOpsBlock" v-else-if="currAccHelpOps==='username'">
                                <p>Your username is</p>
                                <el-skeleton style="width: 240px" :loading="formLoading.retrieveUsername" animated>
                                    <template #template>
                                        <el-skeleton-item variant="text" />
                                    </template>
                                    <template #default>
                                        <p class="username">{{ retrievedUsername }}</p>
                                    </template>
                                </el-skeleton>
                            </div>
                            <div class="accountHelpOpsBlock" v-else></div>
                        </div>
                    </div>
                    <div class="accountForumBtnContainer">
                        <button class="altBtn" :disabled="formLoading.resetPwd || formLoading.retrieveUsername" @click="clearSessionAndRedirect();">Go To Log in</button>
                    </div>
                </div>
                <div v-else class="accountForumBlockWapper">
                    <div class="accountForumBlock">
                        <div class="accountFormBlank">
                            <p :class="{error: !isValid.login.username}">Username</p>
                            <input type="text" @input="clearErrorHint('login', 'username')" :class="{error: !isValid.login.username}" v-model="accountForum.login.username" class="accountForumInputBox" />
                        </div>
                        <div class="accountFormBlank">
                            <p :class="{error: !isValid.login.password}">Password</p>
                            <input type="password" @input="clearErrorHint('login', 'password')" :class="{error: !isValid.login.password}" v-model="accountForum.login.password" class="accountForumInputBox" />
                        </div>
                        <div class="linkContainerWapper">
                            <div class="linkContainer" @click="jumpAccountOpsTo('register')">
                                <p>Not a member yet?</p>
                                <div class="progressBarWapper">
                                    <div class="progressBar"></div>
                                </div>
                            </div>
                            <div class="linkContainer" @click="jumpAccountOpsTo('accHelp')">
                                <p>Forgot your username or password?</p>
                                <div class="progressBarWapper">
                                    <div class="progressBar"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="accountForumBtnContainer">
                        <button class="mainBtn" :disabled="formLoading.login" @click="login">
                            <span v-if="formLoading.login">
                                <el-icon :size="18" class="is-loading"><Loading /></el-icon>
                            </span>
                            <span>{{ formLoading.login ? 'Logging' : 'Log in' }}</span>
                        </button>
                        <button class="altBtn" @click="handleCloseAccountOps">Cancel</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <el-drawer v-model="formDialog" :title="`Create a New ${currentEditForm === 'forum' ? 'POST' : 'EVENT'}!`"
        size="40%" :before-close="(done) => handleCloseDialog('forum', done)" direction="ltr">
        <div>
            <el-form :model="postForm[currentEditForm]" label-position="top" :rules="postFormRules">
                <el-form-item label="Title" :label-width="formLabelWidth" prop="title">
                    <el-input v-model="postForm[currentEditForm].title" maxlength="80" show-word-limit />
                </el-form-item>
                <el-form-item label="Content" :label-width="formLabelWidth" prop="content">
                    <div class="editorContainer">
                        <div class="editorToolBar">
                            <div class="editorTool" :class="{ 'active': editor.isActive('heading', { level: 1 }) }"
                                @click="editor.chain().focus().toggleHeading({ level: 1 }).run()">
                                <el-tooltip effect="dark" content="Heading 1" placement="top-start">
                                    <span class="material-symbols-outlined">format_h1</span>
                                </el-tooltip>
                            </div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('heading', { level: 2 }) }"
                                @click="editor.chain().focus().toggleHeading({ level: 2 }).run()">
                                <el-tooltip effect="dark" content="Heading 2" placement="top">
                                    <span class="material-symbols-outlined">format_h2</span>
                                </el-tooltip>
                            </div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('heading', { level: 3 }) }"
                                @click="editor.chain().focus().toggleHeading({ level: 3 }).run()">
                                <el-tooltip effect="dark" content="Heading 3" placement="top">
                                    <span class="material-symbols-outlined">format_h3</span>
                                </el-tooltip>
                            </div>
                            <div class="editorToolDivider"></div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('bold') }"
                                @click="editor.chain().focus().toggleBold().run()">
                                <el-tooltip effect="dark" content="Bold" placement="top">
                                    <span class="material-symbols-outlined">format_bold</span>
                                </el-tooltip>
                            </div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('italic') }"
                                @click="editor.chain().focus().toggleItalic().run()">
                                <el-tooltip effect="dark" content="Italic" placement="top">
                                    <span class="material-symbols-outlined">format_italic</span>
                                </el-tooltip>
                            </div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('underline') }"
                                @click="editor.chain().focus().toggleUnderline().run()">
                                <el-tooltip effect="dark" content="Underline" placement="top">
                                    <span class="material-symbols-outlined">format_underlined</span>
                                </el-tooltip>
                            </div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('strike') }"
                                @click="editor.chain().focus().toggleStrike().run()">
                                <el-tooltip effect="dark" content="Strike" placement="top">
                                    <span class="material-symbols-outlined">strikethrough_s</span>
                                </el-tooltip>
                            </div>
                            <el-popover :visible="linkPopVisible" :width="300">
                                <div id="linkPopContainer">
                                    <p>Please enter the link URL</p>
                                    <el-input v-model="url" style="width: 240px" placeholder="URL" />
                                    <div>
                                        <el-button size="small" type="info"
                                            @click="linkPopVisible = false">Cancel</el-button>
                                        <el-button size="small" type="primary" @click="setLink">Apply</el-button>
                                    </div>
                                </div>
                                <template #reference>
                                    <div class="editorTool" :class="{ 'active': editor.isActive('link') }"
                                        @click="popLinkCon">
                                        <el-tooltip effect="dark" content="Link" placement="top">
                                            <span class="material-symbols-outlined">link_2</span>
                                        </el-tooltip>
                                    </div>
                                </template>
                            </el-popover>
                            <div class="editorTool" :class="{ 'active': editor.isActive('codeBlock') }"
                                @click="editor.chain().focus().toggleCodeBlock().run()">
                                <el-tooltip effect="dark" content="Code Block" placement="top">
                                    <span class="material-symbols-outlined">code_xml</span>
                                </el-tooltip>
                            </div>
                            <div class="editorToolDivider"></div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('bulletList') }"
                                @click="editor.chain().focus().toggleBulletList().run()">
                                <el-tooltip effect="dark" content="Bullet List" placement="top">
                                    <span class="material-symbols-outlined">format_list_bulleted</span>
                                </el-tooltip>
                            </div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('orderedList') }"
                                @click="editor.chain().focus().toggleOrderedList().run()">
                                <el-tooltip effect="dark" content="Ordered List" placement="top">
                                    <span class="material-symbols-outlined">format_list_numbered</span>
                                </el-tooltip>
                            </div>
                            <div class="editorToolDivider"></div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('superscript') }"
                                @click="editor.chain().focus().toggleSuperscript().run()">
                                <el-tooltip effect="dark" content="Superscript" placement="top">
                                    <span class="material-symbols-outlined">superscript</span>
                                </el-tooltip>
                            </div>
                            <div class="editorTool" :class="{ 'active': editor.isActive('subscript') }"
                                @click="editor.chain().focus().toggleSubscript().run()">
                                <el-tooltip effect="dark" content="Subscript" placement="top">
                                    <span class="material-symbols-outlined">subscript</span>
                                </el-tooltip>
                            </div>
                            <div class="editorToolDivider"></div>
                            <div class="editorTool" :class="{ 'active': showPicker }" @click="togglePicker">
                                <el-tooltip effect="dark" content="Emoji" placement="top">
                                    <span class="material-symbols-outlined">emoji_language</span>
                                </el-tooltip>
                                <div v-show="showPicker" ref="pickerContainer" class="pickerPop" @click.stop></div>
                            </div>
                            <div class="editorToolDivider"></div>
                            <div class="editorTool" @click="triggerUpload">
                                <el-tooltip effect="dark" content="Add Image" placement="top-end">
                                    <span class="material-symbols-outlined">image_arrow_up</span>
                                </el-tooltip>
                            </div>
                            <input type="file" ref="fileInput" style="display: none" accept="image/*" multiple
                                @change="onFileChange" />
                        </div>
                        <editor-content :editor="editor" class="editor" v-model="postForm[currentEditForm].content" />
                    </div>
                </el-form-item>
                <el-form-item label="Image (Maximum 9 images, each no more than 5MB)" :label-width="formLabelWidth">
                    <div id="forumImgPreviewContainer" v-show="postForm[currentEditForm].coverImage.length !== 0">
                        <div class="forumImgPreviewBlock" v-for="(img, index) in postForm[currentEditForm].coverImage"
                            :key="index">

                            <el-skeleton :loading="img.loading" animated style="width: 100%; height: 100%;">
                                <template #template>
                                    <el-skeleton-item variant="image" style="width: 100%; height: 100%" />
                                </template>
                                <template #default>
                                    <el-image :src="img.url" alt="post image" :zoom-rate="1.2" :max-scale="7"
                                        :min-scale="0.2"
                                        :preview-src-list="postForm[currentEditForm].coverImage.map(i => i.url)"
                                        show-progress :initial-index="index" fit="cover"></el-image>
                                </template>
                            </el-skeleton>
                            <div class="imgMask" v-show="!img.loading"></div>
                            <div class="imgRemoveBox" @click="removePreviewImg(img)" v-show="!img.loading">
                                <span class="material-symbols-outlined">close</span>
                            </div>
                        </div>
                    </div>
                </el-form-item>
            </el-form>
            <div class="demo-drawer__footer">
                <el-button @click="cancelForm('forum')">Cancel</el-button>
                <el-button type="primary" :loading="loading" @click="handleSubmitForum()">
                    {{ loading.forum ? 'Publishing ...' : 'Publish' }}
                </el-button>
            </div>
        </div>
    </el-drawer>
    <el-container class="layoutContainer">
        <el-aside id="layoutMenuContainer">
            <div id="layoutMenuBlock">
                <div id="layoutInfoContainer">
                    <img src="../assets/logoWithoutText.png" alt="logo">
                    <div class="textBox">
                        <p>Campus</p>
                        <p>Connect</p>
                    </div>
                </div>
                <div id="layoutMenuItemContainer">
                    <div class="layoutMenuItemBox" @click="goTo('/forum')" :class="{ active: route.path === '/forum' }">
                        <span class="material-symbols-outlined">assignment</span>
                        <p>Post</p>
                    </div>
                    <div class="layoutMenuItemBox" @click="goTo('/activity')"
                        :class="{ active: route.path === '/activity' }">
                        <span class="material-symbols-outlined">event</span>
                        <p>Event</p>
                    </div>
                    <div class="layoutMenuItemBox" @click="goTo('/user')" :class="{ active: route.path === '/user' }">
                        <span class="material-symbols-outlined">account_circle</span>
                        <p>My Information</p>
                    </div>
                    <div class="layoutMenuItemBox" @click="goTo('/admin')"
                        :class="{ active: route.path.startsWith('/admin') }"
                        v-show="userInfoStore.info.role === 'ADMIN'">
                        <span class="material-symbols-outlined">admin_panel_settings</span>
                        <p>Admin</p>
                    </div>
                    <div id="layoutPostOpsContainer">
                        <div class="layoutMenuItemBox" @click="handleCreatedCommand('forum')">
                            <span class="material-symbols-outlined">assignment_add</span>
                            <p>Create Post</p>
                        </div>
                        <div class="layoutMenuItemBox" @click="handleCreatedCommand('activity')">
                            <span class="material-symbols-outlined">calendar_add_on</span>
                            <p>Create Event</p>
                        </div>
                    </div>
                </div>
            </div>
            <div id="layoutUserOpsContainer">
                <div class="userInfoContainer">
                    <img :src="userInfoStore.info.userAvatar ? userInfoStore.info.userAvatar : 'https://img.campusconnect.one/avatars/default.svg'" alt="user avatar">
                    <div class="userInfoBlock">
                        <p class="username">{{ userInfoStore.info.username ? userInfoStore.info.username : 'username' }}</p>
                        <p class="userRole">{{ userInfoStore.info.role ? userInfoStore.info.role : 'waiting for login' }}</p>
                    </div>
                </div>
                <div class="userOpsBtnContainer" v-if="tokenStore.token !== ''">
                    <el-button type="danger" plain round @click="confirmLogout">Log out</el-button>
                </div>
                <div class="userOpsBtnContainer" v-else>
                    <el-button type="primary" plain round @click="UIStore.openLogin()">Log in</el-button>
                    <el-button type="info" plain round @click="UIStore.openRegister()">Sign up</el-button>
                </div>
            </div>
        </el-aside>

        <el-container>
            <el-main>
                <el-scrollbar>
                    <router-view></router-view>
                </el-scrollbar>
            </el-main>
        </el-container>
    </el-container>
</template>

<style scoped>
.layoutContainer {
    height: 100vh;
}

#layoutMenuContainer {
    width: 300px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 50px 0;
    border-right: 1px solid #DCDFE6;
    color: #303133;
}

#layoutMenuBlock {
    display: flex;
    flex-direction: column;
    gap: 50px;
}

#layoutInfoContainer {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

#layoutInfoContainer img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    object-position: center;
}

#layoutInfoContainer .textBox {
    display: flex;
    flex-direction: column;
    gap: 3px;
    font-size: 23px;
    font-weight: 500;
}

#layoutMenuItemContainer {
    display: flex;
    flex-direction: column;
}

#layoutMenuItemContainer .layoutMenuItemBox {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0 20px;
    padding: 10px;
    cursor: pointer;
    border-radius: 8px;
    color: #606266;
    transition: all 0.3s ease;
}

#layoutPostOpsContainer {
    margin-top: 30px;
}

#layoutMenuItemContainer .layoutMenuItemBox.active {
    background: rgba(0, 0, 0, 0.08);
}

#layoutMenuItemContainer .layoutMenuItemBox.active p,
#layoutMenuItemContainer .layoutMenuItemBox.active .material-symbols-outlined {
    color: #303133;
}

#layoutMenuItemContainer .layoutMenuItemBox:hover {
    background-color: rgba(0, 0, 0, 0.04);
}

#layoutMenuItemContainer .material-symbols-outlined {
    font-variation-settings:
        'FILL' 0,
        'wght' 300,
        'GRAD' 200,
        'opsz' 48;
    font-size: 26px;
    transition: all 0.3s ease;
}

#layoutMenuItemContainer p {
    font-size: 18px;
    font-weight: 350;
}

#layoutMenuContainer,
#layoutMenuContainer .el-scrollbar {
    height: 100%;
}

#layoutMenuContainer .el-scrollbar__view .el-menu-item {
    font-size: 14px;
}

#layoutMenuContainer .el-scrollbar__view .el-icon {
    font-size: 30px;
}

#layoutUserOpsContainer {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
}

#layoutUserOpsContainer .userInfoContainer {
    width: 100%;
    display: flex;
    gap: 10px;
    align-items: center;
    justify-content: center;
    overflow-x: hidden;
}

#layoutUserOpsContainer img {
    width: 50px;
    height: 50px;
    object-fit: cover;
    object-position: center;
    border-radius: 50%;
    border: 1px solid #DCDFE6;
}

#layoutUserOpsContainer .userInfoBlock {
    display: flex;
    flex-direction: column;
    gap: 3px;
}

#layoutUserOpsContainer .username {
    font-size: 22px;
    font-weight: 500;
}

#layoutUserOpsContainer .userRole {
    font-size: 13px;
    font-weight: 300;
    color: #909399;
}

.layoutContainer .el-main {
    padding: 0;
}





.editorContainer {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: fit-content;
    border: 1px solid #D4D7DE;
    border-radius: 8px;
    /* overflow: hidden; */
}

.editorContainer .editorToolBar {
    display: flex;
    /* gap: 10px; */
    justify-content: space-between;
    align-items: center;
    height: fit-content;
    border-bottom: 1px solid #D4D7DE;
    padding: 2px;
    position: relative;
}

.editorTool {
    width: fit-content;
    height: fit-content;
    padding: 3px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    cursor: pointer;
}

.editorTool:hover {
    background-color: #EBEDF0;
}

.editorTool.active {
    background-color: #CDD0D6;
}

.editorToolDivider {
    width: 2px;
    height: 13px;
    background-color: #DCDFE6;
}

.editorTool .material-symbols-outlined {
    font-variation-settings:
        'FILL' 0,
        'wght' 400,
        'GRAD' 200,
        'opsz' 48;
    font-size: 20px;
}

#linkPopContainer {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.pickerPop {
    position: absolute;
    top: 30px;
    right: 0;
    z-index: 100;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.imgOpsContainer {
    width: 100%;
}

.imgEditor {
    width: 100%;
    height: 300px;
    border: 1px solid #D4D7DE;
    border-radius: 8px;
}

#forumImgPreviewContainer {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
    border: 1px solid #D4D7DE;
    padding: 10px;
    border-radius: 8px;
}

#forumImgPreviewContainer>* {
    aspect-ratio: 1 / 1;
    overflow: hidden;
}

#forumImgPreviewContainer .forumImgPreviewBlock {
    border-radius: 4px;
    overflow: hidden;
    position: relative;
    transition: all 0.2s ease;
}

.forumImgPreviewBlock:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}

.forumImgPreviewBlock .el-image {
    width: 100%;
    height: 100%;
    object-position: center;
}

.forumImgPreviewBlock .imgMask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0);
    transition: all 0.2s ease;
    pointer-events: none;
}

.forumImgPreviewBlock:hover .imgMask {
    background: rgba(0, 0, 0, 0.06);
}

.forumImgPreviewBlock .imgRemoveBox {
    position: absolute;
    display: none;
    align-items: center;
    justify-content: center;
    background-color: #fff;
    border: 1px solid #D4D7DE;
    width: fit-content;
    height: fit-content;
    padding: 2px;
    top: 0;
    right: 0;
    border-top-right-radius: 4px;
    box-sizing: border-box;
    cursor: pointer;
}

.forumImgPreviewBlock:hover .imgRemoveBox {
    display: flex;
}

.forumImgPreviewBlock .material-symbols-outlined {
    font-variation-settings:
        'FILL' 1,
        'wght' 400,
        'GRAD' 200,
        'opsz' 48;
    font-size: 16px;
    color: #303133;
}

:deep(.tiptap) {
    padding: 5px;
    min-height: 250px;
    outline: none;

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

.accountOpsContainer {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;

    background: rgba(0, 0, 0, 0.5);

    display: flex;
    align-items: center;
    justify-content: center;

    z-index: 999;

    color: #303133;
}

.accountOpsBlock > div {
    min-width: 0;
    min-height: 0;
}

.accountOpsBlock {
    width: 900px;
    height: 500px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.2);

    display: grid;
    grid-template-columns: 1fr 1fr;
    overflow: hidden;
}

.accountBackgroundImgContainer {
    position: relative;
    width: 100%;
    height: 100%;
}

.accountBackgroundImgContainer .backgroundImg {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: center;
}

.accountBackgroundImgContainer .logo {
    width: 50px;
    height: 50px;
    object-fit: cover;
    object-position: center;
    position: absolute;
    left: 20px;
    top: 10px;
}

.accountFormContainer {
    width: 100%;
    height: 100%;
    padding: 20px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    gap: 30px;
}

.accountFormContainer .title {
    font-size: 25px;
    font-weight: 600;
}

.accountFormContainer .accountForumBlockWapper {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.accountFormContainer .accountForumBlock {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 20px;
    box-sizing: border-box;
}

.accountFormContainer .accountFormBlank {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.accountFormContainer .accountFormBox {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
}

.accountForumBlock input {
    border: none;
    border-bottom: 1px solid #DCDFE6;
    outline: none;
    transition: all 0.5s ease;
    padding: 5px 0;
}

.accountForumBlock input:focus {
    border-bottom: 1px solid #303133;
}

.accountForumBlock p {
    transition: color 0.5s ease;
}

.accountForumBlock p.error {
    color: red;
}

.accountForumBlock input.error {
    border-bottom: 1px solid red;
}

.accountFormContainer .accountForumBtnContainer {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.accountFormContainer .mainBtn {
    width: 100%;
    padding: 10px 0;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 6px;
    background-color: #303133;
    color: white;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.mainBtn .el-icon {
    display: flex;
    align-items: center;
}

.mainBtn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.altBtn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.accountFormContainer .mainBtn:hover {
    background-color: #606266;
}

.accountFormContainer .altBtn {
    width: 100%;
    padding: 10px 0;
    text-align: center;
    border: 1px solid #DCDFE6;
    color: #606266;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.accountFormContainer .altBtn:hover {
    background-color: #F5F7FA;
}

.accountFormContainer .linkContainerWapper {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 5px;
    align-items: flex-end;
}

.accountForumBlock .linkContainer {
    width: fit-content;
    cursor: pointer;
    font-size: 13px;
}

.linkContainer:hover .progressBar {
    width: 100%;
}

.accountForumBlock .progressBarWapper {
    width: 100%;
    height: 1.5px;
    border-radius: 8px;
    overflow: hidden;
    background-color: #DCDFE6;
    position: relative;
}

.accountForumBlock .progressBar {
    position: absolute;
    height: 1.5px;
    width: 0;
    background-color: #303133;
    transition: width 0.4s ease;
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

.reSendContainer {
    display: flex;
    justify-content: flex-end;
}

.reSendBlock {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.reSendBlock.disabled {
    cursor: not-allowed;
    color: #C0C4CC;
}

.reSendBlock .progressLabel {
    display: block;
    font-size: 12px;
    margin-bottom: 5px;
}

.reSendBlock .countDownValue {
    display: block;
    margin-top: 5px;
    font-size: 19px;
    font-weight: 450;
}

.accountHelpOpsBlock {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.accountHelpOpsBlock .username {
    font-size: 25px;
    font-weight: 550;
    text-align: center;
    margin: 10px 0;
}
</style>
