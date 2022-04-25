<template>
  <div class="grid align-items-center col-12 cursor-pointer" @click="goBack">
    <span class="material-icons text-primary">arrow_back_ios</span>
    <span class="text-primary font-semibold" >BACK</span>
  </div>
</template>
<script>
export default {
  name: 'BackButton',
  methods: {
    goBack(){
      this.$router.go(-1)
    }
  }
}
</script>