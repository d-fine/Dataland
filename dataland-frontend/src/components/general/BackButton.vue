<template>
  <div class="col-12">
    <div class="inline-block">
      <div @click="goBack" class="cursor-pointer grid align-items-center">
        <span class="material-icons text-primary">arrow_back_ios</span>
        <span class="text-primary font-semibold d-letters" title="back_button">BACK</span>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "BackButton",
  methods: {
    goBack() {
      this.$router.go(-1);
    },
  },
};
</script>
