import ActivityView from '@/views/ActivityView.vue'
import AdminCommentView from '@/views/AdminCommentView.vue'
import AdminForumAuditView from '@/views/AdminForumAuditView.vue'
import AdminForumExamineView from '@/views/AdminForumExamineView.vue'
import AdminUserAuditView from '@/views/AdminUserAuditView.vue'
import AdminUserInfoView from '@/views/AdminUserInfoView.vue'
import AdminView from '@/views/AdminView.vue'
import ForumView from '@/views/ForumView.vue'
import HomeView from '@/views/HomeView.vue'
import UserInfoView from '@/views/UserInfoView.vue'
import { redirect } from 'react-router-dom'
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {path: '/', component: HomeView, redirect: '/forum', children: [
        {path: '/forum', component: ForumView},
        {path: '/activity', component: ActivityView},
        {path: '/user', component: UserInfoView},
        {path: '/admin', component: AdminView, redirect: '/admin/user/audit', children: [
            {path: 'user/audit', component: AdminUserAuditView},
            {path: 'user/info', component: AdminUserInfoView},
            {path: 'forum/audit', component: AdminForumAuditView},
            {path: 'forum/examine', component: AdminForumExamineView},
            {path: 'comment', component: AdminCommentView}
        ]},
    ]}
]

const router = createRouter({
    routes: routes,
    history: createWebHistory()
})

export default router
