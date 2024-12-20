<template>
    <div class="basicLayout">
      <a-layout-header class="header">
        <GlobalHeader/>
      </a-layout-header>

      <a-layout-content style="  
            margin-top: 20px;
            margin-bottom: 28px;
            padding: 20px;">
        <h1>content</h1>
        <router-view />
      </a-layout-content>

      <a-layout-footer 
            style="    
                background: #efefef;
                padding: 16px;
                position: fixed;
                bottom: 0;
                left: 0;
                right: 0;
                text-align: center;">
        Ant Design ©2018 Created by Ant UED
      </a-layout-footer>
    </div>
  </template>
  <script lang="ts" setup>
  import GlobalHeader from "@/components/GlobalHeader.vue";
  </script>
  <style scoped>
  .site-layout-content {
    min-height: 280px;
    padding: 24px;
    background: #fff;
  }

  .ant-row-rtl #components-layout-demo-top .logo {
    float: right;
    margin: 16px 0 16px 24px;
  }

  #basicLayout .header {
  padding-inline: 20px;
  margin-bottom: 16px;
  color: unset;
  background: white;
}
  </style>
  