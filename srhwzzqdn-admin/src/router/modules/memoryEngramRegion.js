// 导入组件
const Layout = () => import('@/layout/index.vue')
const rowMemoryEngram = () =>
  import('@/views/memoryEngramRegion/rowMemoryEngram.vue')
const workMemoryEngram = () =>
  import('@/views/memoryEngramRegion/workMemoryEngram.vue')
const lifeMemoryEngram = () =>
  import('@/views/memoryEngramRegion/lifeMemoryEngram.vue')
const funMemoryEngram = () =>
  import('@/views/memoryEngramRegion/funMemoryEngram.vue')
const communicateMemoryEngram = () =>
  import('@/views/memoryEngramRegion/communicateMemoryEngram.vue')
const learnMemoryEngram = () =>
  import('@/views/memoryEngramRegion/learnMemoryEngram.vue')

// 导出该组件
export default [
  {
    path: '/memoryEngramRegion',
    component: Layout,
    name: 'memoryEngramRegion',
    meta: {
      title: '记忆铭纂区',
    },
    icon: 'grid',
    children: [
      {
        path: '/rowMemoryEngram',
        name: 'rowMemoryEngram',
        component: rowMemoryEngram,
        meta: {
          title: '原始记忆铭纂',
        },
        icon: 'magic-stick', // 表示原始/未加工状态
        hidden: false,
      },
      {
        path: '/workMemoryEngram',
        name: 'workMemoryEngram',
        component: workMemoryEngram,
        meta: {
          title: '工作记忆铭纂',
        },
        icon: 'briefcase', // 工作相关图标
        hidden: false,
      },
      {
        path: '/lifeMemoryEngram',
        name: 'lifeMemoryEngram',
        component: lifeMemoryEngram,
        meta: {
          title: '生活记忆铭纂',
        },
        icon: 'house', // 家居生活图标
        hidden: false,
      },
      {
        path: '/funMemoryEngram',
        name: 'funMemoryEngram',
        component: funMemoryEngram,
        meta: {
          title: '娱乐记忆铭纂',
        },
        icon: 'video-play', // 娱乐相关图标
        hidden: false,
      },
      {
        path: '/communicateMemoryEngram',
        name: 'communicateMemoryEngram',
        component: communicateMemoryEngram,
        meta: {
          title: '交际记忆铭纂',
        },
        icon: 'chat-line-round', // 交流对话图标
        hidden: false,
      },
      {
        path: '/learnMemoryEngram',
        name: 'learnMemoryEngram',
        component: learnMemoryEngram,
        meta: {
          title: '学习记忆铭纂',
        },
        icon: 'notebook', // 学习笔记图标
        hidden: false,
      },
    ],
  },
]
