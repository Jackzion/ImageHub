<template>
    <a-flex justify="space-between">
        <h2>图片管理</h2>
        <a-button type="primary" href="/add_picture" target="_blank">+ 创建图片</a-button>
    </a-flex>
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="searchText">
            <a-input v-model:value="searchParams.searchText" placeholder="hihihi" />
        </a-form-item>
        <a-form-item>
            <a-input v-model:value="searchParams.category" placeholder="input category" allow-clear></a-input>
        </a-form-item>
        <a-form-item>
            <a-select style="min-width: 180px;" v-model:value="searchParams.tags" placeholder="select tags"  mode="tags" allow-clear/>
        </a-form-item>
        <a-form-item>
            <a-button type="primary" html-type="submit">doSearch</a-button>
        </a-form-item>
    </a-form>

    <a-table :columns="columns" :data-source="dataList" :pagination="pagenation" @change="doTableChange">
      <template #headerCell="{ column }">
        <template v-if="column.key === 'name'">
          <span>
            <smile-outlined />
            Name
          </span>
        </template>
      </template>
  
      <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'url'">
                <a-image :src="record.url" :width="120"/>
            </template>
            <template v-if="column.dataIndex === 'tags'">
                <a-space wrap>
                    <a-tag v-for="tag in JSON.parse(record.tags||'[]')" :key="tag">{{ tag }}</a-tag>
                </a-space>
            </template>
            <template v-if="column.dataIndex === 'picInfo'">  
                <div>格式：{{ record.picFormat }}</div>  
                <div>宽度：{{ record.picWidth }}</div>  
                <div>高度：{{ record.picHeight }}</div>  
                <div>宽高比：{{ record.picScale }}</div>  
                <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>  
            </template>  
            <template v-else-if="column.dataIndex === 'createTime'">  
                {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}  
            </template>  
            <template v-else-if="column.dataIndex === 'editTime'">  
                {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}  
            </template> 
            <template v-else-if="column.key === 'action'">
                <a-button danger @click="doDelete">delete</a-button>
                <a-button type="link" :href="`/edit_picture?id=${record.id}`">edit</a-button>
            </template>
      </template>
    </a-table>
</template>
  <script lang="ts" setup>
    import { deletePictureUsingPost, listPictureByPageUsingPost } from '@/api/pictureController';
    import { SmileOutlined } from '@ant-design/icons-vue';
    import { message } from 'ant-design-vue';
    import dayjs from 'dayjs';
    import { computed, onMounted, reactive, ref } from 'vue';
    const columns = [  
    {  
        title: 'id',  
        dataIndex: 'id',  
        width: 80,  
    },  
    {  
        title: '图片',  
        dataIndex: 'url',  
    },  
    {  
        title: '名称',  
        dataIndex: 'name',  
    },  
    {  
        title: '简介',  
        dataIndex: 'introduction',  
        ellipsis: true,  
    },  
    {  
        title: '类型',  
        dataIndex: 'category',  
    },  
    {  
        title: '标签',  
        dataIndex: 'tags',  
    },  
    {  
        title: '图片信息',  
        dataIndex: 'picInfo',  
    },  
    {  
        title: '用户 id',  
        dataIndex: 'userId',  
        width: 80,  
    },  
    {  
        title: '创建时间',  
        dataIndex: 'createTime',  
    },  
    {  
        title: '编辑时间',  
        dataIndex: 'editTime',  
    },  
    {  
        title: '操作',  
        key: 'action',  
    },  
    ]

    // 搜索条件
    const dataList = ref([])
    const total = ref(0)
    const searchParams = reactive<API.PictureQueryRequest>({
        current:1,
        pageSize:10,
        sortField:'createTime',
        sortOrder:'descend'
    })

    // 分页参数
    const pagenation = computed(()=>{
        return{
            current: searchParams.current?? 1,
            pageSize:searchParams.pageSize?? 10,
            total:total.value,
            showSizeChange:true,
            showTotal:(total) => `共 ${total} 条`
        }
    })

    const fetchData = async () => {
        const res = await listPictureByPageUsingPost({
            ...searchParams
        })
        if(res.data.data){
            dataList.value = res.data.data.records ?? []
            total.value = res.data.data.total ?? 0
        }else{
            message.error('fail to fetch : '  + res.data.message)
        }
    }

    const doTableChange = (pagination : any) => {
        searchParams.current = pagination.current
        searchParams.pageSize = pagination.pageSize
        fetchData()
    }

    const doSearch = () => {
        searchParams.pageSize = 1
        fetchData()
    }

    const doDelete = async (id : string) => {
        if(!id){
            return 
        }
        const res = await deletePictureUsingPost({id})
        if(res.data.code === 0 ){
            message.success('delete successfully')
            fetchData()
        }else{
            message.error('fail to delete')
        }
    }

    onMounted(() => {
        fetchData()
    })
  </script>
  
  