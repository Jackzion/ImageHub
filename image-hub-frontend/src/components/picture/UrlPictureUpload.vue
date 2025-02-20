<template>
  <div class="url-picture-upload">
    <a-input-group>
      <a-input v-model:value="fileUrl" style="width: calc(100% - 120px);" placeholder="input url"/>
      <a-button type="primary" :loading="loading" @click="handleUpload" style="width: 120px;">Submit</a-button>
    </a-input-group>
  </div>

</template>

<script setup lang="ts">
  import { uploadPictureByUrlUsingPost } from '@/api/pictureController';
  import { message, Space } from 'ant-design-vue';
  import { ref } from 'vue';
  const loading = ref<boolean>(false)
  const fileUrl = ref<string>('')
  interface Props{
    picture?:API.PictureVO,
    onSuccess?:(newPicture:API.PictureVO)=> void,
    spaceId?:number
  }
  const props = defineProps<Props>()

  const handleUpload = async () => {
    loading.value = true
    try{
        const params:API.uploadPictureByUrlUsingPOSTParams = {fileUrl:fileUrl.value}
        if(props.picture){
          params.id = props.picture.id,
          params.spaceId = props.spaceId
        }
        const res = await uploadPictureByUrlUsingPost(params)
        if(res.data.code === 0 && res.data.data){
          message.success('上传成功')
        }else{
          message.error('上传失败' + res.data.message)
        }
      }
    catch(error){
      message.error('上传失败' + error)
    }finally{
      loading.value =false
    }
  }

</script>