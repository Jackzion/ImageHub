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
            <!-- <a-col flex="200px">
                <a-menu-item>
                    <router-link to="/my_space">
                        <UserOutlined />
                        我的空间
                    </router-link>
                </a-menu-item>
            </a-col> -->
            <a-col flex="auto">
                    <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click = "doMenuClick" />
            </a-col>
            <a-col flex="120px">
                <div class="user-login-status">
                    <div v-if="loginUserStore.loginUser.id">
                        <a-popover>
                            <template #content>
                                <LogoutOutlined/>
                                <a-button type="warn" href="/user/login" @click="doLogout">注销</a-button>
                            </template>
                            <a-button type="primary">{{loginUserStore.loginUser.userAccount}}</a-button>
                        </a-popover>
                    </div>
                    <div v-else>
                        <a-button type="primary" href="/user/login">登录</a-button>
                    </div>
                </div>
            </a-col>
        </a-row>

</template>

<script lang="ts" setup>
import { userLogoutUsingPost } from '@/api/userController';
import { useLoginUserStore } from '@/store/user.ts';
import { AppstoreOutlined, LogoutOutlined, MailOutlined, SettingOutlined } from '@ant-design/icons-vue';
import { type MenuProps, message } from 'ant-design-vue';
import { h, ref } from 'vue';
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
  key: '/admin/spaceManage',
  label: '空间管理',
  title: '空间管理',
},
{
    key: '/about',
    icon: () => h(AppstoreOutlined),
    label: 'about',
    title: 'about',
},
{  
  key: '/admin/pictureManage',  
  label: '图片管理',  
  title: '图片管理',  
},
{
    key: '/add_picture',
    icon: () => h(AppstoreOutlined),
    label: 'Add Picture',
    title: 'Add Picture',
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

const doLogout = async () => {
    const res = await userLogoutUsingPost()
    console.log(res)
    if(res.data.code == 0 ){
        loginUserStore.setLoginStore({
            userName:'not login'
        })
        await router.push('user/login')
    }else{
        message.error("failed to logout" + res.data.message)
    }
}


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
  
  