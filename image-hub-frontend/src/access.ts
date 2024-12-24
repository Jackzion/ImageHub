import { useLoginUserStore } from '@/store/user'
import { message } from 'ant-design-vue'
import router from '@/router'

let firstFetchLoginUser = true

router.beforeEach(async (to , from , next) => {
    const loginuserStore = useLoginUserStore()
    let loginUser = loginuserStore.loginUser
    if(firstFetchLoginUser){
        await loginuserStore.fetchLoginUser()
        loginUser = loginuserStore.loginUser
        firstFetchLoginUser = false
    }
    const toUrl = to.fullPath
    if(toUrl.startsWith('/admin')){
        if(!loginUser || loginUser.userRole !== 'admin'){
            message.error('no auth')
            next(`/user/login?redirect=${to.fullPath}`)
            return 
        }
    }
    next()
})