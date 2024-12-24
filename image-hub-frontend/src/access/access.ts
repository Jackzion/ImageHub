import { useLoginUserStore } from '@/store/user'
import router from '@/router'
import ACCESS_ENUM from './accessEnum'

const firstFetchLoginUser = true

router.beforeEach(async (to , from , next) => {
    const loginUserStore = useLoginUserStore()
    if(firstFetchLoginUser){
        await loginUserStore.fetchLoginUser()
    }

    const loginuser = loginUserStore.loginUser
    console.log('userInfo : ' + loginuser)
    const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN
    // check access and userRole
    if(needAccess !== ACCESS_ENUM.NOT_LOGIN){
        if(!loginuser||!loginuser.userRole||loginuser.userRole === ACCESS_ENUM.NOT_LOGIN){
            next(`/user/login?redirect=${to.fullPath}`)
            return
        }
    }
    if(needAccess === ACCESS_ENUM.ADMIN){
        if(loginuser.userRole !== ACCESS_ENUM.ADMIN){
            next('/noAuth')
            return
        }
    }
    next()
})