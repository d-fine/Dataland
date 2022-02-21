<template>
  <div class="container">
    <span>
      <input v-model="input_message"/>
      <button>Search</button>
    </span>
  </div>

</template>

<script>
export default {
  name: "ESG",
  data() {
    return {
      input_message: 'Search for ESG data'
    }
  }
}
</script>

<style scoped>

</style>