<template>
  <div id="addSpacePage">  
    <h2 style="margin-bottom: 16px">创建空间</h2>  
    <a-form layout="vertical" :model="formData" @finish="handleSubmit">
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
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        * 目前仅支持开通普通版，如需升级空间，请联系
        <a href="https://www.baidu.com/" target="_blank">Ziio</a>。
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        {{ spaceLevel.text }}： 大小 {{ spaceLevel.maxSize }}， 数量: {{ spaceLevel.maxCount }}
      </a-typography-paragraph>
    </a-card>  
  </div>
</template>

<script setup lang="ts">
import { addSpaceUsingPost, listSpaceLevelUsingGet } from '@/api/spaceController';
import { SPACE_LEVEL_ENUM, SPACE_LEVEL_OPTIONS } from '@/constant/space';
import { message } from 'ant-design-vue';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter()
const formData = ref<API.SpaceAddRequest>({
  spaceName: '',
  spaceLevel: SPACE_LEVEL_ENUM.COMMON,
})
const loading = ref<boolean>(false)

const handleSubmit = async (values: any) => {  
  loading.value = true;  
  const res = await addSpaceUsingPost({
    spaceName: formData.value.spaceName,
    spaceLevel: formData.value.spaceLevel,
  })  
  if (res.data.code === 0 && res.data.data) {
    message.success("创建成功")
    router.push({
      path: `/space/${res.data.data}`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
  loading.value = false;  
}

const spaceLevelList = ref<API.SpaceLevel[]>([])

// 获取空间级别
const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('加载空间级别失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchSpaceLevelList()
})

</script>