import { createRouter, createWebHistory } from 'vue-router'
import UserLoginPage from '../pages/user/UserLoginPage.vue'
import UserRegisterPage from '../pages/user/UserRegisterPage.vue'
import UserManagePage from '../pages/admin/UserManagePage.vue'
import ACCESS_ENUM from '@/access/accessEnum'
import AddPicturePage from '@/pages/picture/AddPicturePage.vue'
import EditPicturePage from '@/pages/picture/EditPicturePage.vue'
import PictureManagePage from '@/pages/admin/PictureManagePage.vue'
import PictureDetailPage from '@/pages/picture/PictureDetailPage.vue'
import HomePage from '@/pages/HomePage.vue'
import AddPictureBatchPage from '@/pages/picture/AddPictureBatchPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/user/login',
      name: 'login',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: UserLoginPage,
    },    
    {
      path: '/user/register',
      name: 'register',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: UserRegisterPage,
    },    
    {
      path: '/admin/userManage',
      name: 'userManage',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: UserManagePage,
      meta:{
        access : ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/add_picture',
      name: 'add_picture',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: AddPicturePage,
    }, 
    {
      path: '/edit_picture',
      name: 'edit_picture',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: EditPicturePage,
    }, 
    {
      path: '/admin/pictureManage',  
      name: 'pictureManage',  
      component: PictureManagePage,  
    },
    {  
      path: '/picture/:id',  
      name: 'pictureDetail',  
      component: PictureDetailPage,  
      props: true,  
    },
    {  
      path: '/add_picture/batch',  
      name: '批量创建图片',  
      component: AddPictureBatchPage,  
    }
  ],
})

export default router
