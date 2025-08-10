// 导入组件
const Layout = () => import('@/layout/index.vue')
const assetAccounting = () => import('@/views/assetControlArea/assetAccounting.vue')
const assetOverview = () => import('@/views/assetControlArea/assetOverview.vue')
const assetClusteringAnalysis = () => import('@/views/assetControlArea/assetClusteringAnalysis.vue')

// 导出该组件
export default [
    {
        path: '/assetControlArea',
        component: Layout,
        name: 'assetControlArea',
        meta: {
            title: '资产管制区',
        },
        icon: 'money', // 主菜单使用"锁"图标象征管控
        children: [
            {
                path: '/assetAccounting',
                name: 'assetAccounting',
                component: assetAccounting,
                meta: {
                    title: '资产记账',
                },
                icon: 'DocumentCopy', // 文档复制图标象征记账
                hidden: false,
            },
            {
                path: '/assetOverview',
                name: 'assetOverview',
                component: assetOverview,
                meta: {
                    title: '资产总览',
                },
                icon: 'DataLine', // 数据曲线图标象征总览
                hidden: false,
            },
            {
                path: '/assetClusteringAnalysis',
                name: 'assetClusteringAnalysis',
                component: assetClusteringAnalysis,
                meta: {
                    title: '资产聚类分析',
                },
                icon: 'PieChart', // 饼图图标象征聚类分析
                hidden: false,
            }
        ]
    }
]
