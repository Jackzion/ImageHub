<template>
    <a-input-search
      v-model:value="searchParams.searchText"
      placeholder="input search text"
      enter-button="Search"
      size="large"
      @search="onSearch"
    />

    <a-tabs v-model:activeKey="selectedCategory" centered @change="onSearch">
      <a-tab-pane key="all" tab="all" />  
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category"/>
    </a-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px;">Tags:</span>
      <a-space :size="[0,8]" wrap>
        <a-checkable-tag  
          v-for="(tag, index) in tagList"  
          :key="tag"  
          v-model:checked="selectedTagList[index]"  
          @change="onSearch"  
        >  
          {{ tag }}  
        </a-checkable-tag>  
      </a-space>
    </div>
        
    <!-- 图片列表 -->
    <PictureList :dataList="dataList" :loading="loading" style="margin-top: 20px;"/>
    <!-- 补充分页组件 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="fetchData()"
    />
  </template>
  <script lang="ts" setup>
  import { listPictureTagCategoryUsingGet, listPictureVoByPageUsingPost } from '@/api/pictureController';
  import PictureList from '@/components/picture/PictureList.vue';
  import { message } from 'ant-design-vue';
  import { onMounted, reactive, ref } from 'vue';
  const dataList = ref<API.PictureVO[]>()
  const total = ref(0)
  const loading = ref(true)

  const searchParams = reactive<API.PictureQueryRequest>({
    current:1,
    pageSize:12,
    sortField:'createTime',
    sortOrder:'descend'
  })

  const fetchData = async () =>{
    loading.value = true
    // 转换参数 tags and category
    const params = {
      ...searchParams,
      tags:[]
    }
    if(selectedCategory.value !== 'all'){
      params.category = selectedCategory.value
    }
    selectedTagList.value?.forEach((useTag,index) => {
      if(useTag){
        params.tags.push(tagList.value[index])
      }
    })
    const res = await listPictureVoByPageUsingPost(params)
    if(res.data.data){
        dataList.value = res.data.data.records??[]
        total.value = res.data.data.total??0
    }else{
        message.error('fail')
    }
    loading.value = false
  }

  const onSearch = () => {
    // 重置搜索条件
    searchParams.current = 1
    fetchData()
  }

  const categoryList = ref<string[]>([])
  const selectedCategory = ref<string>('all')
  const tagList = ref<string[]>([])
  const selectedTagList = ref<boolean[]>([])

  const getTagCategoryOptions = async () =>{
    const res = await listPictureTagCategoryUsingGet()
    if(res.data.code === 0 && res.data.data){
      categoryList.value = res.data.data.categoryList
      tagList.value = res.data.data.tagList
    }else{
      message.error('fail to get tags and category')
    }
  }

  onMounted(() =>{
    getTagCategoryOptions()  
    fetchData()
  })

  </script>

  <style>
    #homePage .search-bar {  
      max-width: 480px;  
      margin: 0 auto 16px;  
    }
  </style>
  
  