<template>
  <ContentBox theme="orange">
    <ul>
      <li v-for="constraint in constraints" :key="constraint">
        {{ constraint }}
      </li>
    </ul>
  </ContentBox>
</template>

<script setup lang="ts">
import ContentBox from './ContentBox.vue';

interface Props {
  constraints: string[];
}

defineProps<Props>();
</script>
