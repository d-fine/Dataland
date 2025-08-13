<template>
  <ContentBox theme="orange">
    <ul style="list-style: disc; list-style-position: inside">
      <li
        v-for="(constraint, index) in constraints"
        :key="constraint"
        :style="{ marginTop: index > 0 ? 'var(--spacing-xxxs)' : '0' }"
      >
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
