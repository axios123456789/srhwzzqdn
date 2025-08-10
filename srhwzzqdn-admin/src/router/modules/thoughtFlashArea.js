// 导入组件
const Layout = () => import('@/layout/index.vue')
const glitchLightDomain = () => import('@/views/thoughtFlashArea/glitchLightDomain.vue')
const flashHaltDarkroom = () => import('@/views/thoughtFlashArea/flashHaltDarkroom.vue')
const flashHaltMindCloudMap = () => import('@/views/thoughtFlashArea/flashHaltMindCloudMap.vue')

// 导出该组件
export default [
    {
        path: '/thoughtFlashArea',
        component: Layout,
        name: 'thoughtFlashArea',
        meta: {
            title: '想法闪停区',
        },
        icon: 'Lightning', // 闪电图标象征思维闪光
        children: [
            {
                path: '/glitchLightDomain',
                name: 'glitchLightDomain',
                component: glitchLightDomain,
                meta: {
                    title: '闪停光域',
                },
                icon: 'sunny', // 闪光图标契合光域概念
                hidden: false,
            },
            {
                path: '/flashHaltDarkroom',
                name: 'flashHaltDarkroom',
                component: flashHaltDarkroom,
                meta: {
                    title: '闪停暗房',
                },
                icon: 'MoonNight', // 月亮图标代表暗环境
                hidden: false,
            },
            {
                path: '/flashHaltMindCloudMap',
                name: 'flashHaltMindCloudMap',
                component: flashHaltMindCloudMap,
                meta: {
                    title: '闪停思维云图',
                },
                icon: 'Cpu', // CPU图标象征思维计算
                hidden: false,
            }
        ]
    }
]