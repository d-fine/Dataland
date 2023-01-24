<template>
  <div class="col-12">
    <div class="inline-block">
      <div @click="goBack" class="cursor-pointer grid align-items-center">
        <span class="material-icons text-primary">arrow_back_ios</span>
        <span class="text-primary font-semibold d-letters" title="back_button">{{ label }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "BackButton",
  props: {
    label: {
      type: String,
      default: "BACK",
    },
  },
  methods: {
    goBack(): void {
      this.$router.go(-1);
    },
  },
});
</script>
