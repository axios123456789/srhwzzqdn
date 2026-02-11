<!--
 *                        _oo0oo_
 *                       o8888888o
 *                       88" . "88
 *                       (| -_- |)
 *                       0\  =  /0
 *                     ___/`---'\___
 *                   .' \\|     |// '.
 *                  / \\|||  :  |||// \
 *                 / _||||| -:- |||||- \
 *                |   | \\\  - /// |   |
 *                | \_|  ''\---/''  |_/ |
 *                \  .-\__  '-'  ___/-. /
 *              ___'. .'  /--.--\  `. .'___
 *           ."" '<  `.___\_<|>_/___.' >' "".
 *          | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *          \  \ `_.   \_ __\ /__ _/   .-` /  /
 *      =====`-.____`.___ \_____/___.-`___.-'=====
 *                        `=---='
 * 
 * 
 *      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 
 *            佛祖保佑       永不宕机     永无BUG
 * 
 *        佛曰:  
 *                写字楼里写字间，写字间里程序员；  
 *                程序人员写程序，又拿程序换酒钱。  
 *                酒醒只在网上坐，酒醉还来网下眠；  
 *                酒醉酒醒日复日，网上网下年复年。  
 *                但愿老死电脑间，不愿鞠躬老板前；  
 *                奔驰宝马贵者趣，公交自行程序员。  
 *                别人笑我忒疯癫，我笑自己命太贱；  
 *                不见满街漂亮妹，哪个归得程序员？
 * 
 * @Descripttion: 
 * @version: 
 * @Date: 2021-04-20 11:06:21
 * @LastEditors: huzhushan@126.com
 * @LastEditTime: 2022-09-27 17:56:21
 * @Author: huzhushan@126.com
 * @HomePage: https://huzhushan.gitee.io/vue3-element-admin
 * @Github: https://github.com/huzhushan/vue3-element-admin
 * @Donate: https://huzhushan.gitee.io/vue3-element-admin/donate/
 -->

<template>
  <el-dropdown trigger="hover">
    <div class="userinfo">
      <template v-if="!userinfo">
        <i class="el-icon-user" />
        admin
      </template>
      <template v-else>
        <img class="avatar" :src="userinfo.avatar" />
        {{ userinfo.userName }}
      </template>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item @click="center">
          {{ $t('topbar.center') }}
        </el-dropdown-item>
        <!--        <el-dropdown-item>{{ $t('topbar.center') }}</el-dropdown-item>-->
        <el-dropdown-item @click="updatePassword">
          {{ $t('topbar.password') }}
        </el-dropdown-item>
        <lock-modal />
        <el-dropdown-item @click="logout">
          {{ $t('topbar.logout') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>

  <!-- 点击修改密码打开该模态窗口 -->
  <el-dialog v-model="dialogVisible" title="修改大脑连接密码" width="30%">
    <el-form label-width="100px">
      <el-form-item label="原密码">
        <el-input
          v-model="editPassword.oldPassword"
          placeholder="请输入原密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input
          v-model="editPassword.newPassword"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input
          v-model="editPassword.newPassword2"
          placeholder="请再次输入确认新密码"
          show-password
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitUpdate">提交</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>
<script>
import { useRouter } from 'vue-router'
import { useUserinfo } from '@/components/Avatar/hooks/useUserinfo'
import LockModal from './LockModal.vue'
import { useApp } from '@/pinia/modules/app'

import { defineComponent, getCurrentInstance, ref } from 'vue'
import { EditPassword, Logout } from '@/api/login'
import { ElMessage } from 'element-plus'

export default defineComponent({
  components: {
    LockModal,
  },
  setup() {
    const router = useRouter()

    const { userinfo } = useUserinfo()

    const { proxy: ctx } = getCurrentInstance() // 可以把ctx当成vue2中的this

    const editPassword = ref({}) //存放修改密码参数
    const dialogVisible = ref(false) //控制修改密码模态窗口开闭

    //修改密码点击事件
    const updatePassword = () => {
      //清空密码框
      editPassword.value = {}
      //打开模态窗口
      dialogVisible.value = true
    }

    //点击修改密码提交按钮后触发
    const submitUpdate = async () => {
      if (
        editPassword.value.oldPassword == undefined ||
        editPassword.value.oldPassword == ''
      ) {
        ElMessage.warning('【旧密码】不能为空')
        return
      }
      if (
        editPassword.value.newPassword == undefined ||
        editPassword.value.newPassword == ''
      ) {
        ElMessage.warning('【新密码】不能为空')
        return
      }
      if (editPassword.value.newPassword.length < 6) {
        ElMessage.warning('密码长度不能小于6个字符串')
        return
      }
      if (editPassword.value.newPassword.length > 20) {
        ElMessage.warning('密码长度不能大于20个字符串 ')
        return
      }
      if (
        editPassword.value.newPassword2 == undefined ||
        editPassword.value.newPassword2 == ''
      ) {
        ElMessage.warning('【确认新密码框】不能为空')
        return
      }

      const { code, message } = await EditPassword(editPassword.value)
      if (code === 200) {
        dialogVisible.value = false
        ElMessage.success(message + '，密码已被修改！')
        router.push('/login')
      } else {
        ElMessage.error(message)
      }
    }

    // 退出
    const logout = async () => {
      const { code, data, message } = await Logout()
      if (code == 200) {
        // 清除token
        useApp().clearToken()
        router.push('/login')
      } else {
        ctx.$message.error(message)
      }
    }

    //个人中心
    const center = () => {
      // 跳转到锁屏页面
      router.push(
        '/personCenter?redirect=' + router.currentRoute.value.fullPath
      )
    }

    return {
      userinfo,
      logout,
      center,
      updatePassword,
      editPassword,
      dialogVisible,
      submitUpdate,
    }
  },
})
</script>

<style lang="scss" scoped>
.userinfo {
  padding: 0 16px;
  line-height: 48px;
  cursor: pointer;
  display: flex;
  align-items: center;
  &:hover {
    background: #f5f5f5;
  }
  .el-icon-user {
    font-size: 20px;
    margin-right: 8px;
  }
  .avatar {
    margin-right: 8px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
  }
}
</style>
