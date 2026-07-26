<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const name = ref('')

function handleGreet() {
  ElMessage.success(`Hello ${name.value || 'TrioCode'}! Element Plus is working.`)
}
</script>

<template>
  <div class="home">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>TrioCode Payments Processing System</span>
        </div>
      </template>

      <p>Vue 3 + Vite + Vue Router + Element Plus scaffold is ready.</p>

      <el-form :inline="true" @submit.prevent>
        <el-form-item label="Your name">
          <el-input v-model="name" placeholder="Enter your name" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGreet">Greet</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        title="Frontend scaffold created successfully"
        type="success"
        :closable="false"
        show-icon
      />
    </el-card>
  </div>
</template>

<style scoped>
.home {
  max-width: 720px;
  margin: 40px auto;
  padding: 0 16px;
}

.card-header {
  font-weight: 600;
}
</style>
