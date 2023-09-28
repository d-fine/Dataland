<template>
  <div class="search" role="search">
    <input :placeholder="placeholderText" class="search__field" aria-label="Search" />
    <img :src="icon" alt="Search" class="search__icon" />
  </div>
</template>

<script setup lang="ts">
import { defineProps } from "vue";

const { icon, placeholderText } = defineProps<{
  icon: string;
  placeholderText: string;
}>();
</script>

<style scoped lang="scss">
.search {
  position: relative;
  margin: 80px auto 0;
  max-width: 701px;
  height: 80px;
  padding: 28px 24px;
  border-radius: 16px;
  border: 2px solid var(--grey-tones-100);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(16px);

  &__field {
    width: 100%;
    border: none;
    outline: none;
    padding-left: 40px;
    background: transparent;
    &::placeholder {
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 24px; /* 150% */
      letter-spacing: 0.25px;
      color: var(--grey-tones-400);
    }
  }

  &__icon {
    position: absolute;
    left: 24px;
    top: 50%;
    transform: translateY(-50%);
    width: 24px;
    height: 24px;
    pointer-events: none; // Let click events pass through to the input
  }
}
@media only screen and (max-width: 1440px) {
  .search {
    margin-top: 31px;
  }
}
</style>
