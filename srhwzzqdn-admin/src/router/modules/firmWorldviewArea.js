// 导入组件
const Layout = () => import('@/layout/index.vue')
const mentalTopographyBuild = () => import('@/views/firmWorldviewArea/mentalTopographyBuild.vue')
const valuesClusteringAnalysis = () => import('@/views/firmWorldviewArea/valuesClusteringAnalysis.vue')

// 导出该组件
export default [
    {
        path: '/firmWorldviewArea',
        component: Layout,
        name: 'firmWorldviewArea',
        meta: {
            title: '三观稳固区',
        },
        icon: 'Lock',
        children: [
            {
                path: '/mentalTopographyBuild',
                name: 'mentalTopographyBuild',
                component: mentalTopographyBuild,
                meta: {
                    title: '精神拓扑构建',
                },
                icon: 'circle-plus', // 表示原始/未加工状态
                hidden: false,
            },
            {
                path: '/valuesClusteringAnalysis',
                name: 'valuesClusteringAnalysis',
                component: valuesClusteringAnalysis,
                meta: {
                    title: '三观聚类分析',
                },
                icon: 'TrendCharts', // 工作相关图标
                hidden: false,
            },
        ],
    },
]
