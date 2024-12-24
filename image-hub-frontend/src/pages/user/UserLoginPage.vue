<template>
    <a-form
      :model="formState"
      name="normal_login"
      class="login-form"
      @finish="onFinish"
      @finishFailed="onFinishFailed"
    >
      <a-form-item
        label="userAccount"
        name="userAccount"
        :rules="[{ required: true, message: 'Please input your userAccount!' }]"
      >
        <a-input v-model:value="formState.userAccount">
          <template #prefix>
            <UserOutlined class="site-form-item-icon" />
          </template>
        </a-input>
      </a-form-item>
  
      <a-form-item
        label="userPassword"
        name="userPassword"
        :rules="[{ required: true, message: 'Please input your userPassword!' }]"
      >
        <a-input-password v-model:value="formState.userPassword">
          <template #prefix>
            <LockOutlined class="site-form-item-icon" />
          </template>
        </a-input-password>
      </a-form-item>
  
      <a-form-item>
        <a-form-item name="remember" no-style>
          <a-checkbox v-model:checked="remember">Remember me</a-checkbox>
        </a-form-item>
        <a class="login-form-forgot" href="">Forgot userPassword</a>
      </a-form-item>
  
      <a-form-item>
        <a-button :disabled="disabled" type="primary" html-type="submit" class="login-form-button" onclick="">
          Log in
        </a-button>
        Or
        <a href="/user/register">register now!</a>
      </a-form-item>
    </a-form>
  </template>
  <script lang="ts" setup>
    import { reactive, computed, ref } from 'vue';
    import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
    import { useRouter } from 'vue-router';
    import { useLoginUserStore } from '@/store/user';
    import { userLoginUsingPost } from '@/api/userController';
    import { message } from 'ant-design-vue';
    const router = useRouter()
    const loginUserStore = useLoginUserStore()
    const remember = ref<boolean>(false);
    interface FormState {
        userAccount: string;
        userPassword: string;
    }
    const formState = reactive<FormState>({
        userAccount: '',
        userPassword: '',
    });
    const onFinish = async (values: any) => {
        // handleLoginForm
        const res = await userLoginUsingPost(values)
        // save login state in store
        if(res.data.code === 0 && res.data.data){
            await loginUserStore.fetchLoginUser()
            message.success('login successfully')
            router.push({
                path:'/',
                replace:true,
            })
        }else{
            message.error('failed to login :' + res.data.message)
        }
    };
    
    const onFinishFailed = (errorInfo: any) => {
        console.log('Failed:', errorInfo);
    };
    const disabled = computed(() => {
        return !(formState.userAccount && formState.userPassword);
    });
  

  </script>
  <style scoped>
  #components-form-demo-normal-login .login-form {
    max-width: 300px;
  }
  #components-form-demo-normal-login .login-form-forgot {
    float: right;
  }
  #components-form-demo-normal-login .login-form-button {
    width: 100%;
  }
  </style>
  