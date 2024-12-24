<template>
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="userAccount">
            <a-input v-model:value="searchParams.userAccount" placeholder="hihihi" />
        </a-form-item>
        <a-form-item label="userName">
            <a-input v-model:value="searchParams.userName" placeholder="hihihi" />
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
            <template v-if="column.dataIndex === 'userAvatar'">
                <a-image :src="record.userAvatar" :width="120"/>
            </template>
            <template v-if="column.dataIndex === 'userRole'">
                <div v-if="record.userRole === 'admin'">
                    <a-tag color="green">admin</a-tag>
                </div>
                <div v-else>
                    <a-tag color="blue">user</a-tag>
                </div>
            </template>
            <template v-if="column.dataIndex === 'createTime'">
                {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            </template>
            <template v-if="column.dataIndex === 'updateTime'">
                {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
            </template>
            <template v-else-if="column.key === 'action'">
                <a-button danger @click="doDelete">delete</a-button>
            </template>
      </template>
    </a-table>
  </template>
  <script lang="ts" setup>
    import { deleteUserUsingPost, listUserVoByPageUsingPost } from '@/api/userController';
    import { SmileOutlined } from '@ant-design/icons-vue';
    import { message } from 'ant-design-vue';
    import { computed, onMounted, reactive, ref } from 'vue';
    import dayjs from 'dayjs';
import { commentProps } from 'ant-design-vue/es/comment';
  const columns = [
    {
        title: 'id',
        dataIndex: 'id',
    },
    {
        title: 'userAccount',
        dataIndex: 'userAccount',
    },
    {
        title: 'userName',
        dataIndex: 'userName',
    },
    {
        title: 'userAvatar',
        dataIndex: 'userAvatar',
    },
    {
        title: 'userProfile',
        dataIndex: 'userProfile',
    },
    {
        title: 'userRole',
        dataIndex: 'userRole',
    },
    {
        title: 'createTime',
        dataIndex: 'createTime',
    },
    {
        title: 'updateTime',
        dataIndex: 'updateTime',
    },
    {
        title: 'action',
        key: 'action',
    },
  ];
    // 数据
    const dataList = ref([])
    const total = ref(0)
    const searchParams = reactive<API.UserQueryRequest>({
        current:1,
        pageSize:10
    })

    const fetchData = async () => {
        const res = await listUserVoByPageUsingPost({
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

    const doDelete = async (id : string) => {
        if(!id){
            return 
        }
        const res = await deleteUserUsingPost({id})
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
  
  