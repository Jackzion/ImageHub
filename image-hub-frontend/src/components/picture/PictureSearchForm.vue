<template>
    <div class="picture-search-form">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="关键词" name="searchText">
          <a-input
              v-model:value="searchParams.searchText"
              placeholder="从名称和简介搜索"
              allow-clear
          />
        </a-form-item>
        <a-form-item label="分类" name="category">
          <a-auto-complete
              v-model:value="searchParams.category"
              style="min-width: 180px"
              :options="categoryOptions"
              placeholder="请输入分类"
              allowClear
          />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
              v-model:value="searchParams.tags"
              style="min-width: 180px"
              :options="tagOptions"
              mode="tags"
              placeholder="请输入标签"
              allowClear
          />
        </a-form-item>
        <a-form-item label="日期" name="">
          <a-range-picker
              style="width: 400px"
              show-time
              v-model:value="dateRange"
              :placeholder="['编辑开始日期', '编辑结束时间']"
              format="YYYY/MM/DD HH:mm:ss"
              :presets="rangePresets"
              @change="onRangeChange"
          />
        </a-form-item>
        <a-form-item label="名称" name="name">
          <a-input v-model:value="searchParams.name" placeholder="请输入名称" allow-clear />
        </a-form-item>
        <a-form-item label="简介" name="introduction">
          <a-input v-model:value="searchParams.introduction" placeholder="请输入简介" allow-clear />
        </a-form-item>
        <a-form-item label="宽度" name="picWidth">
          <a-input-number v-model:value="searchParams.picWidth" />
        </a-form-item>
        <a-form-item label="高度" name="picHeight">
          <a-input-number v-model:value="searchParams.picHeight" />
        </a-form-item>
        <a-form-item label="格式" name="picFormat">
          <a-input v-model:value="searchParams.picFormat" placeholder="请输入格式" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 96px;margin-right: 20px;">搜索</a-button>
          <a-button html-type="reset" @click="doClear" style="width: 96px;">重置</a-button>
        </a-form-item>
        <!-- 按颜色搜索 -->
        <a-form-item label="按颜色搜索" style="margin-top: 16px">
          <color-picker format="hex" @pureColorChange="onColorChange" />
        </a-form-item>
    </a-form>
    </div>
</template>

<script setup lang="ts">
import { listPictureTagCategoryUsingGet } from '@/api/pictureController';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { onMounted, reactive, ref } from 'vue';

interface Props {
  onSearch?: (searchParams: API.PictureQueryRequest) => void
}

const props = defineProps<Props>()

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({})
// 触发父级Search方法
const doSearch = () => {
  props.onSearch?.(searchParams)
}

const dateRange = ref<[]>([])

/**
 * 日期范围更改触发
 */
const onRangeChange = (date: any[], dateString: string[]) => {
    if (dateString.length < 2) {
        searchParams.startEditTime = undefined
        searchParams.endEditTime = undefined
    } else {
        searchParams.startEditTime = date[0].toDate()
        searchParams.endEditTime = date[1].toDate()
    }
}

const rangePresets = ref([
  { label: '过去 7 天', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: '过去 14 天', value: [dayjs().add(-14, 'd'), dayjs()] },
  { label: '过去 30 天', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: '过去 90 天', value: [dayjs().add(-90, 'd'), dayjs()] },
])

// 获取标签和目录
interface Option {
  value:string , 
  label:string
}

const categoryOptions = ref<Option[]>()
const tagOptions = ref<Option[]>()
// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('加载选项失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

// 清理
const doClear = () => {
  // 取消所有对象的值
  Object.keys(searchParams).forEach((key)=>{
    searchParams[key] = undefined
  })
  dateRange.value = []
  props.onSearch?.(searchParams)
}

</script>

<style scoped>
.picture-search-form .ant-form-item {
  margin-top: 16px;
}
</style>