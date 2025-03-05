<template>
  <a-space>
        <a-button type="primary" href="/add_space" target="_blank">+ 创建空间</a-button>
        <a-button type="primary" ghost href="/space_analyze?queryPublic=1" target="_blank"
          >分析公共图库</a-button
        >
        <a-button type="primary" ghost href="/space_analyze?queryAll=1" target="_blank"
          >分析全部空间</a-button
        >
  </a-space>
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="spaceName">
            <a-input v-model:value="searchParams.spaceName" placeholder="请输入空间名称" />
        </a-form-item>
        <a-form-item label="空间级别" name="spaceLevel">
          <a-select
            v-model:value="searchParams.spaceLevel"
            :options="SPACE_LEVEL_OPTIONS"
            placeholder="请输入空间级别"
            style="min-width: 180px"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="用户 id" name="userId">
          <a-input v-model:value="searchParams.userId" placeholder="请输入用户 id" allow-clear />
        </a-form-item>
        <a-form-item>
            <a-button type="primary" html-type="submit">doSearch</a-button>
        </a-form-item>
    </a-form>
    <a-table :columns="columns" :data-source="dataList" :pagination="pagination" @change="doTableChange">
      <template #headerCell="{ column }">
        <template v-if="column.key === 'name'">
          <span>
            <smile-outlined />
            Name
          </span>
        </template>
      </template>
  
      <template #bodyCell="{ column, record }">
        <!-- 空间级别 -->
        <template v-if="column.dataIndex === 'spaceLevel'">
          <a-tag>{{ SPACE_LEVEL_MAP[record.spaceLevel] }}</a-tag>
        </template>
        <!-- 使用情况 -->
        <template v-if="column.dataIndex === 'spaceUseInfo'">
          <div>大小：{{ record.totalSize  }} / {{ record.maxSize  }}</div>
          <div>数量：{{ record.totalCount }} / {{ record.maxCount }}</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/edit_space?id=${record.id}`" target="_blank">
              编辑
            </a-button>
            <a-button type="link" :href="`/space_analyze?spaceId=${record.id}`" target="_blank">
              分析
            </a-button>
            <a-button type="link" danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
    </template>

    </a-table>
  </template>
  <script lang="ts" setup>
    import { SmileOutlined } from '@ant-design/icons-vue';
    import { message } from 'ant-design-vue';
    import { computed, onMounted, reactive, ref } from 'vue';
    import dayjs from 'dayjs';
import { deleteSpaceUsingPost, listSpaceByPageUsingPost } from '@/api/spaceController';
import { SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS } from '@/constant/space';
    const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
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
    // 数据
    const dataList = ref([])
    const total = ref(0)
    const searchParams = reactive<API.SpaceQueryRequest>({
        current:1,
        pageSize:10,
        sortField: 'createTime',
        sortOrder: 'descend',
    })

    const fetchData = async () => {
        const res = await listSpaceByPageUsingPost({
            ...searchParams
        })
        if(res.data.data){
            dataList.value = res.data.data.records ?? []
            total.value = res.data.data.total ?? 0
        }else{
            message.error('fail to fetch : '  + res.data.message)
        }
    }

    const pagination = computed(() => {
        return {
            current:searchParams.current ?? 1,
            pageSize:searchParams.pageSize ?? 10 , 
            total : total.value,
            showSizeChanger:true,
            showTotal: (total) => `共 ${total} 条`
        }
    })

    const doTableChange = (pagination : any) => {
        searchParams.current = pagination.current
        searchParams.pageSize = pagination.pageSize
        fetchData()
    }

    const doSearch = () => {
        searchParams.pageSize = 1
        fetchData()
    }

    // 页面首次加载一次
    onMounted(() => {
        fetchData()
    })

    const doDelete = async (id : string) => {
        if(!id){
            return 
        }
        const res = await deleteSpaceUsingPost({id})
        if(res.data.code === 0 ){
            message.success('delete successfully')
            fetchData()
        }else{
            message.error('fail to delete')
        }
    }
  </script>
  
  