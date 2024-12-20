<template>
        <a-row :wrap="false" style="height: 30px;">
            <a-col flex="200px">
                <RouterLink to="/">
                    <div class="container">
                        <a-avatar :size="{ xs: 20, sm: 20, md: 30, lg: 40, xl: 50, xxl: 70 }">
                            <template #icon>
                                <img src="../assets/logo.png"/>
                            </template>
                        </a-avatar>
                        <div class="title">by ziio</div>
                    </div>
                </RouterLink>
            </a-col>
            <a-col flex="auto">
                    <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click = "doMenuClick" />
            </a-col>
            <a-col flex="120px">
                <div class="user-login-status">
                    <div v-if="loginUserStore.loginUser.id">
                        {{ loginUserStore.loginUser.userName ?? '无名' }}
                    </div>
                    <div v-else>
                        <a-button type="primary" href="/user/login">登录</a-button>
                    </div>
                </div>
            </a-col>
        </a-row>

</template>

<script lang="ts" setup>
import { h, ref } from 'vue';
import { MailOutlined, AppstoreOutlined, SettingOutlined } from '@ant-design/icons-vue';
import {useLoginUserStore} from '@/store/user.ts'
import { MenuProps } from 'ant-design-vue';
import { useRouter } from "vue-router";
const current = ref<string[]>(['/']);
const loginUserStore = useLoginUserStore();
loginUserStore.fetchLoginUser()
const items = ref<MenuProps['items']>([
{
    key: '/',
    icon: () => h(MailOutlined),
    label: 'Index',
    title: 'Index',
},
{
    key: '/about',
    icon: () => h(AppstoreOutlined),
    label: 'about',
    title: 'about',
},
{
    key: 'user',
    icon: () => h(SettingOutlined),
    label: 'User',
    title: 'User',
    children: [
    {
        type: 'group',
        label: 'Item 1',
        children: [
        {
            label: 'Option 1',
            key: 'setting:1',
        },
        {
            label: 'Option 2',
            key: 'setting:2',
        },
        ],
    },
    {
        type: 'group',
        label: 'Item 2',
        children: [
        {
            label: 'Option 3',
            key: 'setting:3',
        },
        {
            label: 'Option 4',
            key: 'setting:4',
        },
        ],
    },
    ],
},
{
    key: 'alipay',
    label: h('a', { href: 'https://jackzion.github.io/', target: '_blank' }, 'Navigation Four - Link'),
    title: 'Navigation Four - Link',
},
]);
const router = useRouter()
// 路由跳转事件
const doMenuClick = ({ key } ) => {
  router.push({
    path: key,
  });
};

router.afterEach((to) =>{
    current.value = [to.path]

})


</script>

<style scoped>
    .logo {
        height: 40px;
    }
    .container{
        display: flex; /* 使用Flexbox布局 */
        flex-direction: column; /* 设置主轴为垂直方向 */
        align-items: center; /* 在交叉轴上居中对齐 */
        height: 100vh; /* 设置容器高度为视口高度 */
    }
    .title {
        color: black;
        font-size: 10px;
        text-align: center;
    }
</style>
  
  