// 导入组件
const Layout = () => import('@/layout/index.vue')
const relationshipInfo = () => import('@/views/socialConnectionsArea/relationshipInfo.vue')
const interpersonalCommunicationEvent = () => import('@/views/socialConnectionsArea/interpersonalCommunicationEvent.vue')
const psychometricTopologyAnalysis = () => import('@/views/socialConnectionsArea/psychometricTopologyAnalysis.vue')

// 导出该组件
export default [
    {
        path: '/socialConnectionsArea',
        component: Layout,
        name: 'socialConnectionsArea',
        meta: {
            title: '人脉交际区',
        },
        icon: 'Connection', // 改为表示人际连接的图标
        children: [
            {
                path: '/relationshipInfo',
                name: 'relationshipInfo',
                component: relationshipInfo,
                meta: {
                    title: '缘系人信息',
                },
                icon: 'User', // 用户图标象征个人信息
                hidden: false,
            },
            {
                path: '/interpersonalCommunicationEvent',
                name: 'interpersonalCommunicationEvent',
                component: interpersonalCommunicationEvent,
                meta: {
                    title: '人际交流事件',
                },
                icon: 'ChatRound', // 对话图标象征交流
                hidden: false,
            },
            {
                path: '/psychometricTopologyAnalysis',
                name: 'psychometricTopologyAnalysis',
                component: psychometricTopologyAnalysis,
                meta: {
                    title: '人格拓扑测绘',
                },
                icon: 'DataAnalysis', // 数据分析图标象征测绘分析
                hidden: false,
            }
        ]
    }
]
