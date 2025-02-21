<template>
  <div id="searchPicturePage">
    <h2 style="margin-bottom: 16px">以图搜图</h2>
    <h3 style="margin: 16px 0">原图</h3>
    <a-card style="width: 240px">
      <template #cover>
        <img
          style="height: 180px; object-fit: cover"
          :alt="picture.name"
          :src="picture.thumbnailUrl ?? picture.url"
        />
      </template>
    </a-card>
    <h3 style="margin: 16px 0">识图结果</h3>
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
    >
      <template #renderItem="{ item }">
        <a-list-item style="padding: 0">
          <a :href="item.fromUrl" target="_blank">
            <a-card>
              <template #cover>
                <img style="height: 180px; object-fit: cover" :src="item.thumbUrl" />
              </template>
            </a-card>
          </a>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script lang="ts" setup>
import { getPictureVoByIdUsingGet, searchPictureByPictureUsingPost } from '@/api/pictureController';
import { message } from 'ant-design-vue';
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';


const route = useRoute()
// get id from path
const pictureId = computed(() => route.query.pictureId as string)
const picture = ref<API.PictureVO>({})

// get picturevo by id
const getOldPicture =async () => {
  const id = route.query.pictureId
  if(id){
    const res = await getPictureVoByIdUsingGet({
      id : id
    })
    if(res.data.code === 0 && res.data.data){
      const data = res.data.data
      picture.value = data
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  }
}

// get searchPictureList by pictureId
const dataList = ref<API.SearchPictureByPictureRequest[]>([])
const fetchData = async () => {
  const res = await searchPictureByPictureUsingPost({
    pictureId: pictureId.value
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

onMounted(() => {
  getOldPicture()
  fetchData()
})

</script>