<template>
  <div v-if="sections">
    <div v-for="section in sections" :key="section.title">
      <h1>{{ section.title }}</h1>
      <p v-html="section.text"></p>
      <img :src="section.image" alt="" v-if="section.image" />
    </div>
  </div>
  <div>Liberate Data - Empower Autonomy. Break Monopolies.</div>
  <div><input type="text" /></div>
</template>

<script setup lang="ts">
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();
</script>

<style scoped></style>
