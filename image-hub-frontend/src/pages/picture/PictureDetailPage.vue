<template>
  <a-row :gutter="[16,16]">
      <!-- 图片展示区 -->  
    <a-col :sm="24" :md="16" :xl="18">  
        <a-card title="图片预览">  
          <a-image  
            style="max-height: 600px; object-fit: contain"  
            :src="picture.url"  
          />  
        </a-card>  
    </a-col> 
    <a-col :sm="24" :md="8" :xl="6">
      <a-card title="picture info">
        <a-descriptions :column="1">
          <a-descriptions-item label="作者">  
            <a-space>  
              <a-avatar :size="24" :src="picture.user?.userAvatar" />  
              <div>{{ picture.user?.userName }}</div>  
            </a-space>  
          </a-descriptions-item> 
          <a-descriptions-item label="名称">  
            {{ picture.name ?? '未命名' }}  
          </a-descriptions-item> 
          <a-descriptions-item label="简介">  
            {{ picture.introduction ?? '-' }}  
          </a-descriptions-item>  
          <a-descriptions-item label="标签">  
            <a-tag v-for="tag in picture.tags" :key="tag">  
              {{ tag }}  
            </a-tag>  
          </a-descriptions-item>  
          <a-descriptions-item label="格式">  
            {{ picture.picFormat ?? '-' }}  
          </a-descriptions-item>  
          <a-descriptions-item label="宽度">  
            {{ picture.picWidth ?? '-' }}  
          </a-descriptions-item>  
          <a-descriptions-item label="高度">  
            {{ picture.picHeight ?? '-' }}  
          </a-descriptions-item>  
          <a-descriptions-item label="宽高比">  
            {{ picture.picScale ?? '-' }}  
          </a-descriptions-item>  
          <a-descriptions-item label="大小">  
            {{ formatSize(picture.picSize) }}  
          </a-descriptions-item>  
          <a-descriptions-item label="主色调">
            <a-space>
              {{ picture.picColor ?? '-' }}
              <div
                v-if="picture.picColor"
                :style="{
                  backgroundColor: toHexColor(picture.picColor),
                  width: '16px',
                  height: '16px',
                }"
              />
            </a-space>
          </a-descriptions-item>
        </a-descriptions>

        <a-space wrap>
          <a-button v-if="canEdit" type="default" @click="doEdit">
            Edit
          </a-button>
          <a-button v-if="canEdit" danger type="default" @click="doDelete">
            Delete
          </a-button>
          <a-button type="primary" @click="doDownload">
            Download
          </a-button>
          <a-button type="primary" ghost @click="doShare">
            分享
            <template #icon>
              <share-alt-outlined />
            </template>
          </a-button>
        </a-space>
      </a-card>
    </a-col>
  </a-row>
  <ShareModal ref="shareModalRef" :link="shareLink" />
</template>

<script setup lang="ts">
import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController';
import router from '@/router';
import { useLoginUserStore } from '@/store/user';
import { downloadImage } from '@/util';
import { message } from 'ant-design-vue';
import { computed, onMounted, ref } from 'vue';

  interface Props {
    id: string|number
  }

  const props = defineProps<Props>()

  const picture = ref<API.PictureVO>({})

  const fetchData = async() =>{
    try{
      console.log(props.id)
      const res = await getPictureVoByIdUsingGet({id:props.id})
      if(res.data.code === 0 && res.data.data){
        picture.value = res.data.data
      }else{
        message.error("failed to get pictureVO")
      }
    }catch(e:any){
      message.error("failed to get pictureVO" + e.message)
    }
  }

  const formatSize = (size:number) => {
    if(!size) return 'unkonwn'
    if(size<1024) return size + ' B'
    if(size<1024*1024) return size + ' kB'
    return (size / (1024*1024)).toFixed(2) + ' MB'
  }

  const loginUserStore = useLoginUserStore()

  const canEdit = computed(() =>{
    const loginUser = loginUserStore.loginUser
    if(!loginUser.id){
      return false
    }
    const user = picture.value?.user || {}
    return loginUser.id === user.id || loginUser.userRole === 'admin'
  })

  const doEdit = () =>{
    router.push('/edit_picture?id=' + picture.value?.id)
  }

  const doDelete = async() =>{
    const id = picture.value?.id
    const res = await deletePictureUsingPost({id:id})
    if (res.data.code === 0) {  
      message.success('删除成功')  
    } else {  
      message.error('删除失败')  
    } 
  }

  const doDownload = () =>{
    downloadImage(picture.value.url) 
  }

  // 分享弹窗引用
  const shareModalRef = ref()
  // 分享链接
  const shareLink = ref<string>()
  // 分享
  const doShare = (picture:API.PictureVO, e:Event) => {
    e.stopPropagation()
    shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
    if(shareModalRef.value){
      shareModalRef.value.showModal()
    }
  }

  onMounted(()=>{
    console.log('组件已挂载');
    fetchData()
  })
</script>