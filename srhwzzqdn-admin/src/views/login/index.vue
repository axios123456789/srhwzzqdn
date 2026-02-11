<template>
  <div class="login-container">
    <!-- 动态背景图容器 -->
    <div class="bg-container">
      <img
        class="bg-gif"
        src="@/assets/system/brainBjtLogin.gif"
        alt="background"
      />
    </div>

    <!-- 登录表单 -->
    <div class="login" v-if="isLoginVisible">
      <el-form class="form" :model="model" :rules="rules" ref="loginForm">
        <h1 class="title">私人化外置增强大脑</h1>
        <el-form-item prop="userName">
          <el-input
            class="text"
            v-model="model.userName"
            prefix-icon="User"
            clearable
            :placeholder="$t('login.username')"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            class="text"
            v-model="model.password"
            prefix-icon="Lock"
            show-password
            clearable
            :placeholder="$t('login.password')"
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha">
            <el-input
              class="text"
              v-model="model.captcha"
              prefix-icon="Picture"
              placeholder="请输入大脑连接验证码"
            ></el-input>
            <img :src="captchaSrc" @click="refreshCaptcha" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            class="btn"
            size="large"
            @click="submit"
          >
            {{ btnText }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <div style="width: 100%">
            <a style="float: right" @click="registerBrainAccount">
              还未创建属于自己的外置大脑账号，注册一个？
            </a>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 注册表单 -->
    <div class="register" v-if="isRegisterVisible">
      <el-form class="rform" label-width="110px">
        <h1 class="title">私人化外置增强大脑账号注册</h1>
        <el-form-item label="外置大脑账号">
          <el-input
            v-model="form.account"
            placeholder="输入账号"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="连接密码">
          <el-input
            placeholder="请输入密码"
            v-model="form.password"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            placeholder="请确认连接密码"
            v-model="form.password2"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="大脑主人名称">
          <el-input
            v-model="form.userName"
            placeholder="输入名字"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8400/superBrain/system/fileUpload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="headers"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            type="textarea"
            :rows="3"
            placeholder="请输入内容"
            v-model="form.description"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <div class="form-buttons">
            <el-button type="primary" @click="brainRegister">
              大脑注册
            </el-button>
            <el-button class="cancel-btn" @click="cancel">取消</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 主脑账号注册认证模态窗口 -->
    <el-dialog v-model="rzdialogVisible" title="主脑账号注册认证" width="30%">
      <el-form label-width="120px">
        <el-form-item label="认证密码">
          <el-input v-model="form.verificationPassword" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="verification" style="float:left;">
            认证
          </el-button>
          <el-button @click="brainRegister2" style="float: right">
            直接注册
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <div class="change-lang">
      <change-lang />
    </div>
  </div>
</template>

<script>
import {
  defineComponent,
  getCurrentInstance,
  reactive,
  toRefs,
  ref,
  computed,
  onMounted,
  watch,
  onUnmounted,
} from 'vue'
import {
  Login,
  GetValidateCode,
  IsRegisterMainBrain,
  RegisterBrainAccount,
} from '@/api/login'
import { useRouter, useRoute } from 'vue-router'
import ChangeLang from '@/layout/components/Topbar/ChangeLang.vue'
import useLang from '@/i18n/useLang'
import { useApp } from '@/pinia/modules/app'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DeleteUserById } from '@/api/sysUser'

export default defineComponent({
  components: { ChangeLang },
  name: 'login',
  setup() {
    const { proxy: ctx } = getCurrentInstance() // 可以把ctx当成vue2中的this
    const router = useRouter()
    const route = useRoute()
    const { lang } = useLang()
    watch(lang, () => {
      state.rules = getRules()
    })
    const getRules = () => ({
      userName: [
        {
          required: true,
          message: ctx.$t('login.rules-username'),
          trigger: 'blur',
        },
      ],
      password: [
        {
          required: true,
          message: ctx.$t('login.rules-password'),
          trigger: 'blur',
        },
        {
          min: 6,
          max: 20,
          message: ctx.$t('login.rules-regpassword'),
          trigger: 'blur',
        },
      ],
      captcha: [
        {
          required: true,
          message: ctx.$t('login.rules-validate-code'),
          trigger: 'blur',
        },
      ],
    })

    // 定义登录和注册模块的显示
    const isLoginVisible = ref(true)
    const isRegisterVisible = ref(false)
    const form = ref({}) //注册表单
    const rzdialogVisible = ref(false) //控制认证模态窗口的开闭

    //onMounted钩子函数
    onMounted(() => {
      isLoginVisible.value = true
      isRegisterVisible.value = false
      state.refreshCaptcha()

      // 添加键盘事件监听
      window.addEventListener('keyup', handleKeyUp)
    })

    //组件被卸载时执行
    onUnmounted(() => {
      // 组件卸载时移除事件监听，防止资源堆积
      window.removeEventListener('keyup', handleKeyUp)
    })

    //当敲击Enter时直接登录
    const handleKeyUp = e => {
      if (e.key === 'Enter' && isLoginVisible.value) {
        state.submit()
      }
    }

    //注册连接点击事件
    const registerBrainAccount = () => {
      isLoginVisible.value = false
      isRegisterVisible.value = true
    }

    //-------------------------------------------------文件上传--------------------------------------------
    // 图像上传成功以后的事件处理函数
    const handleAvatarSuccess = (response, uploadFile) => {
      form.value.avatar = response.data
    }

    //点击注册页面的取消按钮触发
    const cancel = () => {
      form.value = {}
      isRegisterVisible.value = false
      isLoginVisible.value = true
    }

    //点击注册页面的大脑连接按钮触发
    const brainRegister = async () => {
      //必填字段校验
      if (form.value.account == undefined || form.value.account == '') {
        ElMessage.warning('【大脑账号】不能为空')
        return
      }
      if (form.value.password == undefined || form.value.password == '') {
        ElMessage.warning('【连接密码】不能为空')
        return
      }
      if (form.value.password.length < 6) {
        ElMessage.warning('大脑连接密码不能小于6个字符串')
        return
      }
      if (form.value.password.length > 20) {
        ElMessage.warning('大脑连接密码不能大于20个字符串')
        return
      }
      if (form.value.userName == undefined || form.value.userName == '') {
        ElMessage.warning('【大脑主人名称】不能为空')
        return
      }

      //判断是否注册了主脑
      const { data } = await IsRegisterMainBrain()
      if (data) {
        //注册了主脑
        //设置账号等级
        form.value.level = 3
        //直接调用注册接口完成注册
        const { code, message } = await RegisterBrainAccount(form.value)
        if (code === 200) {
          ElMessage.success(message)
          //回到登录界面
          isLoginVisible.value = true
          isRegisterVisible.value = false
        } else {
          ElMessage.error(message)
        }
      } else {
        //没有注册主脑
        //先认证，再注册主脑
        rzdialogVisible.value = true
      }
    }

    //主脑账号注册认证模态窗口直接注册点击事件
    const brainRegister2 = () => {
      ElMessageBox.confirm(
        '不认证的情况下将注册普通大脑, 是否继续?',
        'Warning',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        form.value.level = 3
        //调用注册接口
        const { code, message } = await RegisterBrainAccount(form.value)
        if (code === 200) {
          rzdialogVisible.value = false
          ElMessage.success(message)
          //回到登录界面
          isLoginVisible.value = true
          isRegisterVisible.value = false
        } else {
          ElMessage.error(message)
        }
      })
    }
    //主脑账号注册认证模态窗口认证点击事件
    const verification = async () => {
      form.value.level = 1

      //调用注册接口
      const { code, message } = await RegisterBrainAccount(form.value)
      if (code === 200) {
        rzdialogVisible.value = false
        ElMessage.success(message)
        //回到登录界面
        isLoginVisible.value = true
        isRegisterVisible.value = false
      } else {
        ElMessage.error(message)
      }
    }

    const state = reactive({
      model: {
        userName: 'xk123456789',
        password: '123456',
        captcha: '', // 用户输入的验证码
        codeKey: '', // 后端返回的验证码key
      },
      rules: getRules(),
      loading: false,
      captchaSrc: '',
      refreshCaptcha: async () => {
        const { data } = await GetValidateCode()
        state.model.codeKey = data.codeKey
        state.captchaSrc = data.codeValue
      },
      btnText: computed(() =>
        state.loading ? ctx.$t('login.logging') : ctx.$t('login.login')
      ),
      loginForm: ref(null),
      submit: () => {
        if (state.loading) {
          return
        }
        state.loginForm.validate(async valid => {
          if (valid) {
            state.loading = true
            const { code, data, message } = await Login(state.model)
            if (+code === 200) {
              ctx.$message.success({
                message: ctx.$t('login.loginsuccess'),
                duration: 1000,
              })

              const targetPath = decodeURIComponent(route.query.redirect)
              if (targetPath.startsWith('http')) {
                // 如果是一个url地址
                window.location.href = targetPath
              } else if (targetPath.startsWith('/')) {
                // 如果是内部路由地址
                router.push(targetPath)
              } else {
                router.push('/home') // 请求成功后，进入到首页
              }
              useApp().initToken(data)
            } else {
              ctx.$message.error(message)
            }
            state.loading = false
          }
        })
      },
    })

    return {
      ...toRefs(state),
      isLoginVisible,
      isRegisterVisible,
      form,
      registerBrainAccount,
      handleAvatarSuccess,
      cancel,
      brainRegister,
      rzdialogVisible,
      brainRegister2,
      verification,
    }
  },
})
</script>

<style lang="scss" scoped>
/* 新增外层容器 */
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 动态背景图容器 */
.bg-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;

  .bg-gif {
    width: 100%;
    height: 100%;
    object-fit: cover;
    /* 优化GIF渲染 */
    image-rendering: -moz-crisp-edges;
    image-rendering: -o-crisp-edges;
    image-rendering: -webkit-optimize-contrast;
    image-rendering: crisp-edges;
    -ms-interpolation-mode: nearest-neighbor;
  }
}

/* 登录表单样式 - 修复垂直居中问题 */
.login {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  max-width: 520px;
  padding: 0 24px;
  box-sizing: border-box;
  z-index: 1;

  /* 半透明遮罩增强可读性 */
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(45, 58, 75, 0.7);
    z-index: -1;
  }

  .form {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(8px);
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    padding: 40px;

    :deep {
      .el-input__wrapper {
        box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
        background: rgba(0, 0, 0, 0.1);
      }
      .el-input-group--append > .el-input__wrapper {
        border-top-right-radius: 0;
        border-bottom-right-radius: 0;
      }
      .el-input-group--prepend > .el-input__wrapper {
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
      }
    }
    .title {
      color: #fff;
      text-align: center;
      font-size: 24px;
      margin: 0 0 24px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
    }
    .text {
      font-size: 16px;
      :deep(.el-input__inner) {
        color: #fff;
        height: 48px;
        line-height: 48px;
        &::placeholder {
          color: rgba(255, 255, 255, 0.2);
        }
      }
    }
    .btn {
      width: 100%;
      margin-top: 10px;
      font-weight: bold;
      letter-spacing: 1px;
    }
  }
}

/* 注册表单样式 - 修改为与登录表单一致的透明风格 */
.register {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  max-width: 700px;
  padding: 0 20px;
  box-sizing: border-box;
  z-index: 1;

  /* 半透明遮罩增强可读性 */
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(45, 58, 75, 0.7);
    z-index: -1;
  }

  .rform {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(8px);
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    padding: 40px;

    .title {
      color: #fff;
      text-align: center;
      font-size: 24px;
      margin: 0 0 24px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
    }

    :deep {
      .el-form-item__label {
        color: #fff;
        font-weight: normal;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
      }

      .el-input__wrapper {
        box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
        background: rgba(0, 0, 0, 0.1);
        &:hover {
          box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset;
        }
      }

      .el-input__inner {
        color: #fff;
        &::placeholder {
          color: rgba(255, 255, 255, 0.3);
        }
      }

      .el-textarea__inner {
        background: rgba(0, 0, 0, 0.1);
        color: #fff;
        border: 1px solid rgba(255, 255, 255, 0.1);
        &::placeholder {
          color: rgba(255, 255, 255, 0.3);
        }
      }
    }

    .form-buttons {
      display: flex;
      justify-content: space-between;
      width: 100%;
      margin-top: 20px;

      .el-button {
        flex: 1;
        max-width: 48%;
        font-weight: bold;
      }

      .cancel-btn {
        background-color: transparent;
        border-color: rgba(255, 255, 255, 0.3);
        color: #fff;

        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
          border-color: rgba(255, 255, 255, 0.4);
        }

        &:active {
          background-color: rgba(255, 255, 255, 0.2);
        }
      }
    }
  }
}

.captcha {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.captcha img {
  cursor: pointer;
  margin-left: 20px;
}

.change-lang {
  position: fixed;
  right: 20px;
  top: 20px;
  z-index: 100;

  :deep {
    .change-lang {
      height: 24px;
      &:hover {
        background: none;
      }
      .icon {
        color: #fff;
      }
    }
  }
}

.avatar-uploader {
  border: 2px dashed rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.2);
  width: 150px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
  margin: 0 auto;

  &:hover {
    border-color: rgba(103, 194, 58, 0.8);
    background-color: rgba(0, 0, 0, 0.3);
    box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);

    .avatar-uploader-icon {
      color: rgba(103, 194, 58, 0.8);
    }
  }

  .avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 6px;
  }

  .avatar-uploader-icon {
    font-size: 50px;
    color: rgba(255, 255, 255, 0.5);
    transition: all 0.3s ease;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .login .form,
  .register .rform {
    width: 90%;
    padding: 30px 20px;
  }

  .avatar-uploader {
    width: 120px;
    height: 120px;
  }
}
</style>
