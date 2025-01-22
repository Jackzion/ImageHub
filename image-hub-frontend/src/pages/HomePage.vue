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
    

    <a-list
        :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"  
        :data-source="dataList"
        :pagenation="pagenation"
        :loading="loading"
    >
        <template #renderItem="{item:picture}">
            <a-list-item style="padding: 0%;">
              <a-card hoverable @click="doClickPicture(picture)">
                <template #cover>
                  <img 
                    style="height: 100%; object-fit: cover;"
                    :alt="picture.name"
                    :src="picture.thumbnailUrl ?? picture.url"/>
                  <a-card-meta :title="picture.name">
                    <template #discription>
                      <a-flx>
                        <a-tag color="green">
                          {{ picture.category ?? "default" }}
                        </a-tag>
                        <a-tag v-for="tag in picture.tags" :key="tag">
                          {{ tag }}
                        </a-tag>
                      </a-flx>
                    </template>
                  </a-card-meta>
                    
                </template>
              </a-card>
            </a-list-item>
        </template>

    </a-list>
  </template>
  <script lang="ts" setup>
  import { listPictureTagCategoryUsingGet, listPictureVoByPageUsingPost } from '@/api/pictureController';
  import router from '@/router';
  import { message } from 'ant-design-vue';
  import { computed, onMounted, reactive, ref } from 'vue';
  const dataList = ref<API.PictureVO[]>()
  const total = ref(0)
  const loading = ref(true)

  const searchParams = reactive<API.PictureQueryRequest>({
    current:1,
    pageSize:12,
    sortField:'createTime',
    sortOrder:'descend'
  })

  const pagenation = computed(()=>{
    return{
        current: searchParams.current ?? 1,  
        pageSize: searchParams.pageSize ?? 10,  
        total: total.value,  
        onChange:(page,pageSize) => {
            searchParams.current = page
            searchParams.pageSize = pageSize
            fetchData()
        }
    }
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

  const doClickPicture = (picture:API.PictureVO) =>{
    router.push({
      path:`/picture/${picture.id}`
    })
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
  
  