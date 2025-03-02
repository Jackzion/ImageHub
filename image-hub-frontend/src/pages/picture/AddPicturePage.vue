<template>

  <div id = "addPicturePage">
    <h2 style="margin-bottom: 16px;">create Picture</h2>
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传"></a-tab-pane>
        <PictureUpload v-if="uploadType === 'file'" :picture="picture" :onSuccess="onSuccess" :spaceId="spaceId"/>
      <a-tab-pane key="url" tab="url 上传">
        <UrlPictureUpload :picture="picture" :onSuccess="onSuccess" :spaceId="spaceId"/>
      </a-tab-pane>
    </a-tabs>

    <div v-if="picture" class="edit-bar">
      <a-button :icon="h(EditOutlined)" @click="doEditPicture">编辑图片</a-button>
      <a-button type="primary" ghost :icon="h(FullscreenExitOutlined)" @click="doImagePainting">
        AI 扩图
      </a-button>
      <ImageOutPaintingModal
        ref="imageOutPaintingRef"
        :picture="picture"
        :spaceId="spaceId"
        :onSuccess="onImageOutPaintingSuccess"
      />
      <ImageCropper
        ref="imageCropperRef"
        :imageUrl="picture?.url"
        :picture="picture"
        :spaceId="spaceId"
        :onSuccess="onCropSuccess"
      />
    </div>

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
      <div>{{ spaceId }}</div>
  </a-form>
  </div>

</template>

<script setup lang="ts">
import { editPictureUsingPost, listPictureTagCategoryUsingGet } from '@/api/pictureController';
import PictureUpload from '@/components/picture/PictureUpload.vue';
import UrlPictureUpload from '@/components/picture/UrlPictureUpload.vue';
import { message } from 'ant-design-vue';
import {
  EditOutlined,
  FullscreenExitOutlined
} from '@ant-design/icons-vue';
import { h, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ImageCropper from '@/components/picture/ImageCropper.vue';
import ImageOutPaintingModal from '@/components/picture/ImageOutPaintingModal.vue';

const router = useRouter()
const route = useRoute()
const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}
const spaceId = route.query?.spaceId

const onFinish = async (values:any) => {
  const pictureId = picture.value?.id
  if(!pictureId){
    return
  }
  const res = await editPictureUsingPost({
    id:pictureId,
    spaceId:spaceId,
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

const uploadType = ref<'file'|'url'>('file')

onMounted(() => {
  getTagsAndCategorys()
})

  // 图片编辑弹窗引用
  const imageCropperRef = ref()

  // 编辑图片
  const doEditPicture = () => {
    if (imageCropperRef.value) {
      imageCropperRef.value.openModal()
    }
  }

  // 编辑成功事件
  const onCropSuccess = (newPicture: API.PictureVO) => {
    picture.value = newPicture
  }

  // AI 扩图弹窗引用
  const imageOutPaintingRef = ref()

  // AI 扩图
const doImagePainting = () => {
  if (imageOutPaintingRef.value) {
    imageOutPaintingRef.value.openModal()
  }
}

// 编辑成功事件
const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

</script>

<style scoped>
  #addPicturePage {
    text-align: center;
    margin: 16px 0;
  }

  #addPicturePage .edit-bar {
    text-align: center;
    margin: 16px 0;
  }
</style>