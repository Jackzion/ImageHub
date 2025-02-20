<template>

  <div id = "editSpacePage">
    <h2 style="margin-bottom: 16px;">Edit Space</h2>
    <h2 style="margin-bottom: 16px;">please input Space Info</h2>
    
    <a-form layout="vertical" :model="formData" @finish="onFinish">
      <a-form-item label="空间名称" name="spaceName">
        <a-input v-model:value="formData.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select
          v-model:value="formData.spaceLevel"
          :options="SPACE_LEVEL_OPTIONS"
          placeholder="请输入空间级别"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
          提交
        </a-button>
      </a-form-item>
    </a-form>

  </div>

</template>

<script setup lang="ts">
import { addSpaceUsingPost, getSpaceVoByIdUsingGet, updateSpaceUsingPost } from '@/api/spaceController';
import { SPACE_LEVEL_OPTIONS } from '@/constant/space';
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter()
const route = useRoute()
const oldSpace = ref<API.SpaceVO>()
const formData = reactive<API.SpaceVO>({})
const loading = ref<boolean>(false)

// 获取老数据
const getOldSpace = async () => {
  // 获取数据
  const id = route.query?.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id: id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      oldSpace.value = data
      formData.spaceName = data.spaceName
      formData.spaceLevel = data.spaceLevel
    }
  }
}

const onFinish = async (values: any) => {
  const spaceId = oldSpace.value?.id
  loading.value = true
  let res
  // 更新
  if (spaceId) {
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...formData,
    })
  } else {
    // 创建
    res = await addSpaceUsingPost({
      ...formData,
    })
  }
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    const path = `/space/${spaceId ?? res.data.data}`
    router.push({
      path,
    })
  } else {
    message.error('操作失败，' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  getOldSpace()
})


</script>

<style>
#addPicturePage {  
  max-width: 720px;  
  margin: 0 auto;  
}
</style>