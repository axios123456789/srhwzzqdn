// 导入组件
const Layout = () => import('@/layout/index.vue')
const careerDevelopmentPlan = () =>
  import('@/views/futurePlanArea/careerDevelopmentPlan.vue')
const learnGroupPlan = () => import('@/views/futurePlanArea/learnGroupPlan.vue')
const lifeManagePlan = () => import('@/views/futurePlanArea/lifeManagePlan.vue')
const financePlan = () => import('@/views/futurePlanArea/financePlan.vue')
const interpersonalRelationshipsPlan = () =>
  import('@/views/futurePlanArea/interpersonalRelationshipsPlan.vue')

// 导出该组件
export default [
  {
    path: '/futurePlanArea',
    component: Layout,
    name: 'futurePlanArea',
    meta: {
      title: '未来规划区',
    },
    icon: 'Opportunity',
    children: [
      {
        path: '/careerDevelopmentPlan',
        name: 'careerDevelopmentPlan',
        component: careerDevelopmentPlan,
        meta: {
          title: '职业发展规划',
        },
        icon: 'Promotion', // 表示原始/未加工状态
        hidden: false,
      },
      {
        path: '/learnGroupPlan',
        name: 'learnGroupPlan',
        component: learnGroupPlan,
        meta: {
          title: '学习成长规划',
        },
        icon: 'Reading', // 工作相关图标
        hidden: false,
      },
      {
        path: '/lifeManagePlan',
        name: 'lifeManagePlan',
        component: lifeManagePlan,
        meta: {
          title: '生活管理规划',
        },
        icon: 'House', // 家居生活图标
        hidden: false,
      },
      {
        path: '/financePlan',
        name: 'financePlan',
        component: financePlan,
        meta: {
          title: '财务金融规划',
        },
        icon: 'Money', // 娱乐相关图标
        hidden: false,
      },
      {
        path: '/interpersonalRelationshipsPlan',
        name: 'interpersonalRelationshipsPlan',
        component: interpersonalRelationshipsPlan,
        meta: {
          title: '人际关系规划',
        },
        icon: 'UserFilled', // 交流对话图标
        hidden: false,
      },
    ],
  },
]
