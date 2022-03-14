<template>
<div>
    <div class="card" v-if="status === 200">
      <div class="card-title green-text"><h4>You successfully uploaded the {{msg}}</h4>
      </div>
      <div class="card-content left-align">
          <p v-for="(value, key) in data" :key="key">{{ humanize(key) }}: {{value}}</p>
      </div>
    </div>
    <div class="card" v-else>
      <div class="card-title red-text">
        <h4>{{msg}} was not uploaded.</h4>
      </div>
    </div>
</div>
</template>

<script>
export default {
  name: "SuccessUpload",
  props: {
    msg: String,
    data: Object,
    status: Number
  },
  methods: {
    humanize(text) {
      const processedText = text.replace(/([A-Z])/g, " $1");
      return processedText.charAt(0).toUpperCase() + processedText.slice(1);
    }
}
}
</script>

<style scoped>

</style>