import { defineStore } from "pinia";
import { ref } from "vue";

export const useLoginUserStore = defineStore("loginUser" , ()=>{
    const loginUser = ref<any>({
        userName:"not login"
    });

    async function fetchLoginUser() {
        // 测试用户登录，3 秒后登录
        setTimeout(() => {
            loginUser.value = { userName: '测试用户', id: 1 }
        }, 3000)
    }

    function setLoginStore(newLoginUser:any){
        loginUser.value = newLoginUser;
    }
    return {loginUser,setLoginStore,fetchLoginUser}
})