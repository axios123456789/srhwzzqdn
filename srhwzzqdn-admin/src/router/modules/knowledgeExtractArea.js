// 导入组件
const Layout = () => import('@/layout/index.vue')
const commonKnowledgeExtract = () =>
  import('@/views/knowledgeExtractArea/commonKnowledgeExtract.vue')
const workKnowledgeExtract = () =>
  import('@/views/knowledgeExtractArea/workKnowledgeExtract.vue')
const communicateKnowledgeExtract = () =>
  import('@/views/knowledgeExtractArea/communicateKnowledgeExtract.vue')
const cultureKnowledgeExtract = () =>
  import('@/views/knowledgeExtractArea/cultureKnowledgeExtract.vue')
const lateralKnowledgeExtract = () =>
  import('@/views/knowledgeExtractArea/lateralKnowledgeExtract.vue')

// 导出该组件
export default [
  {
    path: '/knowledgeExtractArea',
    component: Layout,
    name: 'knowledgeExtractArea',
    meta: {
      title: '知识提取区',
    },
    icon: 'copy-document',
    children: [
      {
        path: '/commonKnowledgeExtract',
        name: 'commonKnowledgeExtract',
        component: commonKnowledgeExtract,
        meta: {
          title: '常识知识提取',
        },
        icon: 'box', // 表示原始/未加工状态
        hidden: false,
      },
      {
        path: '/workKnowledgeExtract',
        name: 'workKnowledgeExtract',
        component: workKnowledgeExtract,
        meta: {
          title: '工作知识提取',
        },
        icon: 'briefcase', // 工作相关图标
        hidden: false,
      },
      {
        path: '/communicateKnowledgeExtract',
        name: 'communicateKnowledgeExtract',
        component: communicateKnowledgeExtract,
        meta: {
          title: '交际知识提取',
        },
        icon: 'chat-line-round', // 家居生活图标
        hidden: false,
      },
      {
        path: '/cultureKnowledgeExtract',
        name: 'cultureKnowledgeExtract',
        component: cultureKnowledgeExtract,
        meta: {
          title: '文化知识提取',
        },
        icon: 'School', // 娱乐相关图标
        hidden: false,
      },
      {
        path: '/lateralKnowledgeExtract',
        name: 'lateralKnowledgeExtract',
        component: lateralKnowledgeExtract,
        meta: {
          title: '旁通知识提取',
        },
        icon: 'more', // 交流对话图标
        hidden: false,
      },
    ],
  },
]
