<template>
  <div class="back-button cursor-pointer flex flex-row align-items-center" @click="goBack">
    <span class="material-icons text-primary">arrow_back_ios</span>
    <span class="text-primary font-semibold d-letters" title="back_button" :data-test="dataTestMarker">{{
      label
    }}</span>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import router from '@/router';

export default defineComponent({
  name: 'BackButton',
  props: {
    label: {
      type: String,
      default: 'BACK',
    },
    dataTestMarker: {
      type: String,
      default: 'back-button',
    },
  },
  methods: {
    /**
     * Performs a go-back operation on the vue router
     */
    goBack(): void {
      router.go(-1);
    },
  },
});
</script>

<style scoped lang="scss">
.back-button {
  width: fit-content;
  @media only screen and (min-width: $small) {
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
  }
}
</style>
