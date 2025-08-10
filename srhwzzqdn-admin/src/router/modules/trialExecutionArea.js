// 导入组件
const Layout = () => import('@/layout/index.vue')
const dailyTrialRun = () => import('@/views/trialExecutionArea/dailyTrialRun.vue')
const businessExperimentRun = () => import('@/views/trialExecutionArea/businessExperimentRun.vue')
const humanNatureExperimentRun = () => import('@/views/trialExecutionArea/humanNatureExperimentRun.vue')
const bypassExperimentRun = () => import('@/views/trialExecutionArea/bypassExperimentRun.vue')

// 导出该组件
export default [
    {
        path: '/trialExecutionArea',
        component: Layout,
        name: 'trialExecutionArea',
        meta: {
            title: '试验开展区',
        },
        icon: 'MagicStick', // 机会图标象征实验探索
        children: [
            {
                path: '/dailyTrialRun',
                name: 'dailyTrialRun',
                component: dailyTrialRun,
                meta: {
                    title: '生活试验开展',
                },
                icon: 'Sunny', // 太阳图标代表日常生活
                hidden: false,
            },
            {
                path: '/businessExperimentRun',
                name: 'businessExperimentRun',
                component: businessExperimentRun,
                meta: {
                    title: '商业试验开展',
                },
                icon: 'TrendCharts', // 趋势图图标象征商业分析
                hidden: false,
            },
            {
                path: '/humanNatureExperimentRun',
                name: 'humanNatureExperimentRun',
                component: humanNatureExperimentRun,
                meta: {
                    title: '人性试验开展',
                },
                icon: 'User', // 用户图标象征人性研究
                hidden: false,
            },
            {
                path: '/bypassExperimentRun',
                name: 'bypassExperimentRun',
                component: bypassExperimentRun,
                meta: {
                    title: '旁通试验开展',
                },
                icon: 'Connection', // 连接图标象征旁通路径
                hidden: false,
            }
        ]
    }
]