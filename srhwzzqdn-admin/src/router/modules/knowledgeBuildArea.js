// 导入组件
const Layout = () => import('@/layout/index.vue')
const commonKnowledgeBuild = () =>
  import('@/views/knowledgeBuildArea/commonKnowledgeBuild.vue')
const workKnowledgeBuild = () =>
  import('@/views/knowledgeBuildArea/workKnowledgeBuild.vue')
const communicateKnowledgeBuild = () =>
  import('@/views/knowledgeBuildArea/communicateKnowledgeBuild.vue')
const cultureKnowledgeBuild = () =>
  import('@/views/knowledgeBuildArea/cultureKnowledgeBuild.vue')
const lateralKnowledgeBuild = () =>
  import('@/views/knowledgeBuildArea/lateralKnowledgeBuild.vue')

// 导出该组件
export default [
  {
    path: '/knowledgeBuildArea',
    component: Layout,
    name: 'knowledgeBuildArea',
    meta: {
      title: '知识构建区',
    },
    icon: 'reading',
    children: [
      {
        path: '/commonKnowledgeBuild',
        name: 'commonKnowledgeBuild',
        component: commonKnowledgeBuild,
        meta: {
          title: '常识知识构建',
        },
        icon: 'box', // 表示原始/未加工状态
        hidden: false,
      },
      {
        path: '/workKnowledgeBuild',
        name: 'workKnowledgeBuild',
        component: workKnowledgeBuild,
        meta: {
          title: '工作知识构建',
        },
        icon: 'briefcase', // 工作相关图标
        hidden: false,
      },
      {
        path: '/communicateKnowledgeBuild',
        name: 'communicateKnowledgeBuild',
        component: communicateKnowledgeBuild,
        meta: {
          title: '交际知识构建',
        },
        icon: 'chat-line-round', // 家居生活图标
        hidden: false,
      },
      {
        path: '/cultureKnowledgeBuild',
        name: 'cultureKnowledgeBuild',
        component: cultureKnowledgeBuild,
        meta: {
          title: '文化知识构建',
        },
        icon: 'School', // 娱乐相关图标
        hidden: false,
      },
      {
        path: '/lateralKnowledgeBuild',
        name: 'lateralKnowledgeBuild',
        component: lateralKnowledgeBuild,
        meta: {
          title: '旁通知识构建',
        },
        icon: 'more', // 交流对话图标
        hidden: false,
      },
    ],
  },
]
