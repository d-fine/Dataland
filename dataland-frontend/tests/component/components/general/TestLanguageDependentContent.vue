<template>
  <LanguageDependentContent>
    <template #english> English content </template>
    <template #german> Deutsche Inhalte </template>
  </LanguageDependentContent>
</template>

<script setup lang="ts">
import LanguageDependentContent from "@/components/general/LanguageDependentContent.vue";
</script>
