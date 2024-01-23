<template>
  <h2>Bulk Data Response Dialog</h2>
  <pre wrap>{{ bulkDataRequestModel }}</pre>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  components: {},
  name: "BulkDataResponseDialog",
  data() {
    return {
      bulkDataRequestModel: {},
    };
  },
  computed: {},
});
</script>

<style scoped lang="scss"></style>
