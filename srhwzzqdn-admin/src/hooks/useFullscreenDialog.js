import { ref, nextTick, onMounted, onUnmounted } from 'vue'

export function useFullscreenDialog(dialogRef = null) {
    const isFullscreen = ref(false)
    const fullscreenBtnId = ref(`fullscreen-btn-${Math.random().toString(36).substr(2, 9)}`)

    // 切换全屏
    const toggleFullscreen = () => {
        isFullscreen.value = !isFullscreen.value
    }

    // 重置全屏状态
    const resetFullscreen = () => {
        isFullscreen.value = false
    }

    // 添加全屏按钮到标题栏
    const addFullscreenButton = (dialogElement = null) => {
        const dialog = dialogElement || dialogRef?.value?.$el || document.querySelector('.el-dialog')
        if (!dialog) return

        const dialogHeader = dialog.querySelector('.el-dialog__header')
        if (!dialogHeader) return

        // 检查是否已存在全屏按钮
        if (dialogHeader.querySelector(`#${fullscreenBtnId.value}`)) return

        // 查找原有的关闭按钮
        const closeBtn = dialogHeader.querySelector('.el-dialog__headerbtn')

        const fullscreenBtn = document.createElement('button')
        fullscreenBtn.id = fullscreenBtnId.value
        fullscreenBtn.className = 'el-dialog__headerbtn fullscreen-btn'
        fullscreenBtn.setAttribute('aria-label', 'fullscreen')
        fullscreenBtn.style.right = closeBtn ? '50px' : '20px' // 如果有关闭按钮，放在它左边
        fullscreenBtn.innerHTML = `
      <i class="el-dialog__close">
        <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
          <path fill="currentColor" d="M160 96.064 352 288V160h64v192H160v-64h128L160 96.064zm0 831.872 192-192v128h64V672H160v64h128L160 927.936zm704-831.872-192 192V160h-64v192h256v-64H704L864 96.064zm0 831.872-192-192v128h-64V672h256v64H704L864 927.936z"/>
        </svg>
      </i>
    `
        fullscreenBtn.onclick = toggleFullscreen

        // 插入到关闭按钮之前
        if (closeBtn) {
            dialogHeader.insertBefore(fullscreenBtn, closeBtn)
        } else {
            dialogHeader.appendChild(fullscreenBtn)
        }
    }

    // 移除全屏按钮
    const removeFullscreenButton = () => {
        const btn = document.getElementById(fullscreenBtnId.value)
        if (btn) btn.remove()
    }

    // 在组件挂载后自动添加按钮，并重置全屏状态
    const initFullscreen = (dialogElement = null) => {
        // 每次打开时重置全屏状态
        resetFullscreen()

        nextTick(() => {
            addFullscreenButton(dialogElement)
        })
    }

    // 清理
    onUnmounted(() => {
        removeFullscreenButton()
    })

    return {
        isFullscreen,
        toggleFullscreen,
        addFullscreenButton,
        removeFullscreenButton,
        initFullscreen,
        resetFullscreen,
        fullscreenBtnId
    }
}