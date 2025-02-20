<template>
    <div class="picture-upload">
      <a-upload
        :show-upload-list="false"  
        :custom-request="handleUpload"
        :bofore-upload="beforeUpload"
        list-type="picture-card"
      >
        <img v-if="picture?.url" :src="picture?.url" alt="avatar"/>
        <div v-else>  
            <loading-outlined v-if="loading"></loading-outlined>  
            <plus-outlined v-else></plus-outlined>  
            <div class="ant-upload-text">点击或拖拽上传图片</div>  
        </div>  
      </a-upload>
      <div v-if="spaceId">將保存到 {{ spaceId }} 號私人空間</div>
    </div>
  </template>
<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { message, type UploadProps } from 'ant-design-vue';
  import { uploadPictureUsingPost } from '@/api/pictureController';
  import { useRoute } from 'vue-router';
  interface Props{
    picture?:API.PictureVO,
    spaceId?: number,
    onSuccess?:(newPicture:API.PictureVO)=> void
  }

  const loading = ref<boolean>(false)
  const props = defineProps<Props>()

  /**
   * 校验
   * @param file 
   */
  const beforeUpload = (file:UploadProps['fileList'][number]) => {
        const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
        if(!isJpgOrPng){
            message.error('不支持非 jpg , png 类型')
        }
        const isLt2M = file.size / 1024 / 1024 > 2
        if(isLt2M){
            message.error('不超过 2 M ')
        }
        return isJpgOrPng && isLt2M
  }

  /*
   * 上传
   * @param param0 
   */
  const handleUpload = async ({file} : any) => {
    loading.value = true
    try{
        const param:API.uploadPictureUsingPOSTParams = props.picture ? {id:props.picture.id}:{}
        param.spaceId=props.spaceId
        const res = await uploadPictureUsingPost(param,{},file)
        if(res.data.code === 0 && res.data.data){
            message.success('success')
            // 传递给父组件
            props.onSuccess?.(res.data.data)
        }else{
            message.error('failed')
        }
    }catch (error){
        message.error('failed')
    }finally{
        loading.value = false
    }
  }
  
  </script>
  <style scoped>
    .picture-upload :deep(.ant-upload) {  
    width: 100% !important;  
    height: 100% !important;  
    min-height: 152px;  
    min-width: 152px;  
    }  
    
    .picture-upload img {  
    max-width: 100%;  
    max-height: 480px;  
    }  
    
    .ant-upload-select-picture-card i {  
    font-size: 32px;  
    color: #999;  
    }  
    
    .ant-upload-select-picture-card .ant-upload-text {  
    margin-top: 8px;  
    color: #666;  
    }
  </style>

  