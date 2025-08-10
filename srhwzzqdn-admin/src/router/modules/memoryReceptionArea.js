// 导入组件
const Layout = () => import('@/layout/index.vue')
const rowMemoryAccess = () => import('@/views/memoryReceptionArea/rowMemoryAccess.vue')
const workMemoryAccess = () => import('@/views/memoryReceptionArea/workMemoryAccess.vue')
const lifeMemoryAccess = () => import('@/views/memoryReceptionArea/lifeMemoryAccess.vue')
const funMemoryAccess = () => import('@/views/memoryReceptionArea/funMemoryAccess.vue')
const communicateMemoryAccess = () => import('@/views/memoryReceptionArea/communicateMemoryAccess.vue')
const learnMemoryAccess = () => import('@/views/memoryReceptionArea/learnMemoryAccess.vue')

// 导出该组件
export default [
    {
        path: '/memoryReceptionArea',
        component: Layout,
        name: 'memoryReceptionArea',
        meta: {
            title: '记忆接收区',
        },
        icon: 'cpu', // 改为表示"记忆处理中心"的图标
        children: [
            {
                path: '/rowMemoryAccess',
                name: 'rowMemoryAccess',
                component: rowMemoryAccess,
                meta: {
                    title: '原始记忆接入',
                },
                icon: 'magic-stick', // 表示原始/未加工状态
                hidden: false,
            },
            {
                path: '/workMemoryAccess',
                name: 'workMemoryAccess',
                component: workMemoryAccess,
                meta: {
                    title: '工作记忆接入',
                },
                icon: 'briefcase', // 工作相关图标
                hidden: false,
            },
            {
                path: '/lifeMemoryAccess',
                name: 'lifeMemoryAccess',
                component: lifeMemoryAccess,
                meta: {
                    title: '生活记忆接入',
                },
                icon: 'house', // 家居生活图标
                hidden: false,
            },
            {
                path: '/funMemoryAccess',
                name: 'funMemoryAccess',
                component: funMemoryAccess,
                meta: {
                    title: '娱乐记忆接入',
                },
                icon: 'video-play', // 娱乐相关图标
                hidden: false,
            },
            {
                path: '/communicateMemoryAccess',
                name: 'communicateMemoryAccess',
                component: communicateMemoryAccess,
                meta: {
                    title: '交际记忆接入',
                },
                icon: 'chat-line-round', // 交流对话图标
                hidden: false,
            },
            {
                path: '/learnMemoryAccess',
                name: 'learnMemoryAccess',
                component: learnMemoryAccess,
                meta: {
                    title: '学习记忆接入',
                },
                icon: 'notebook', // 学习笔记图标
                hidden: false,
            },
        ],
    },
]
