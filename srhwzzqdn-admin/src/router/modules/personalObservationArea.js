// 导入组件
const Layout = () => import('@/layout/index.vue')
const basicInformationObservation = () =>
  import('@/views/personalObservationArea/basicInformationObservation.vue')
const spiritualDimensionObservation = () =>
  import('@/views/personalObservationArea/spiritualDimensionObservation.vue')
const knowledgeLevelObservation = () =>
  import('@/views/personalObservationArea/knowledgeLevelObservation.vue')
const assetDynamicsObservation = () =>
  import('@/views/personalObservationArea/assetDynamicsObservation.vue')
const interpersonalRelationshipsObservation = () =>
  import(
    '@/views/personalObservationArea/interpersonalRelationshipsObservation.vue'
  )

// 导出该组件
export default [
  {
    path: '/personalObservationArea',
    component: Layout,
    name: 'personalObservationArea',
    meta: {
      title: '个人观测区',
    },
    icon: 'Monitor', // 监测仪表盘图标，象征系统性观测
    children: [
      {
        path: '/basicInformationObservation',
        name: 'basicInformationObservation',
        component: basicInformationObservation,
        meta: {
          title: '基本情况观测',
        },
        icon: 'Document', // 文档图标代表基础数据记录
        hidden: false,
      },
      {
        path: '/spiritualDimensionObservation',
        name: 'spiritualDimensionObservation',
        component: spiritualDimensionObservation,
        meta: {
          title: '精神层面观测',
        },
        icon: 'Moon', // 月亮图标象征内在精神世界
        hidden: false,
      },
      {
        path: '/knowledgeLevelObservation',
        name: 'knowledgeLevelObservation',
        component: knowledgeLevelObservation,
        meta: {
          title: '知识层面观测',
        },
        icon: 'Notebook', // 笔记本图标代表知识积累
        hidden: false,
      },
      {
        path: '/assetDynamicsObservation',
        name: 'assetDynamicsObservation',
        component: assetDynamicsObservation,
        meta: {
          title: '资产动态观测',
        },
        icon: 'Coin', // 硬币图标直观表示资产
        hidden: false,
      },
      {
        path: '/interpersonalRelationshipsObservation',
        name: 'interpersonalRelationshipsObservation',
        component: interpersonalRelationshipsObservation,
        meta: {
          title: '人际关系观测',
        },
        icon: 'UserFilled', // 双人图标变体表示人际关系
        hidden: false,
      },
    ],
  },
]
