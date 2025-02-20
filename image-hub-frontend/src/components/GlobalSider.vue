<template>>
    <div id="globalSider">
        <a-layout-sider v-if="loginUserStore.loginUser.id" class="sider" width="200"
            breakpoint="lg"
            collapsed-width="0"
        >
        <a-menu
                mode="inline"
                v-model:selectedKeys="current"
                :items="menuItems"
                @click="doMenuClick"
            />
        </a-layout-sider>
    </div>

</template>

<script lang="ts" setup>
import { useLoginUserStore } from '@/store/user'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const loginUserStore = useLoginUserStore()
// 菜单列表
const menuItems = [
  {
    key: '/',
    label: '公共图库',
  },
  {
    key: '/my_space',
    label: '我的空间',
  },
]

const router = useRouter()

// 菜单事件
const current = ref<string[]>([])
router.afterEach((to, from, failure) => {
  current.value = [to.path]
})

// 路由事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}


</script>