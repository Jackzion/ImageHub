<template>

  <div id = "addPicturePage">
    <h2 style="margin-bottom: 16px;">create Picture</h2>
    <PictureUpload style="margin: 30px" :picture="picture" :onSuccess="onSuccess"/>
    <h2 style="margin-bottom: 16px;">please input Picture Info</h2>
    
    <a-form
      v-if="picture"
      name="pictureForm"
      layout="vertical"
      :model="pictureForm"
      @finish="onFinish"
  >
    <a-form-item label="名称" name="name">  
      <a-input v-model:value="pictureForm.name" placeholder="请输入名称" />  
    </a-form-item>  

    <a-form-item label="简介" name="introduction">  
      <a-textarea  
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          allow-clear  
      />      
    </a-form-item>  
    
    <a-form-item name="category" label="分类">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="tags" label="标签">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
  </a-form>
  </div>

</template>

<script setup lang="ts">
import { editPictureUsingPost, listPictureTagCategoryUsingGet } from '@/api/pictureController';
import PictureUpload from '@/components/picture/PictureUpload.vue';
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter()
const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const onFinish = async (values:any) => {
  const pictureId = picture.value?.id
  if(!pictureId){
    return
  }
  const res = await editPictureUsingPost({
    id:pictureId,
    ...values
  })
  if(res.data.code === 0 && res.data.data){
    message.success('success')
    router.push({
      path:`picture/${pictureId}`,
    })
  }else{
    message.error('failed' + res.data.message)
  }
}

interface Option {
  value:string , 
  label:string
}

const categoryOptions = ref<Option[]>()
const tagOptions = ref<Option[]>()
const getTagsAndCategorys = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if(res.data.code === 0 && res.data.data){
    message.success('success')
    tagOptions.value = (res.data.data.tagList ?? []).map((data:string) =>{
      return {
        value:data,
        label:data
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data:string) =>{
      return {
        value:data,
        label:data
      }
    })
  }else{
    message.error('failed' + res.data.message)
  }
}

onMounted(() => {
  getTagsAndCategorys()
})


</script>

<style scoped>
  #addPicturePage {
    max-width: 720px;
    margin: 0 auto;
  }

  #addPicturePage .edit-bar {
    text-align: center;
    margin: 16px 0;
  }
</style>