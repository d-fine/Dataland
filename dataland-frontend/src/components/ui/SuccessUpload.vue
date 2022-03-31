<template>
  <div>
    <Message severity="success" :sticky="false" :key="count">
        <h4>You successfully uploaded the {{ msg }}</h4>
        <ul class="list-none align-content-start">
          <li v-for="(value, key) in data" :key="key" ><span>{{ humanize(key) }}</span>: <span :title="key">{{
              value
            }}</span></li>
        </ul>
    </Message>
  </div>
</template>

<script>
import {humanizeString} from "@/utils/stringHumanizer"
import Message from 'primevue/message';

export default {
  name: "SuccessUpload",
  components: {Message},
  props: {
    data: Object,
    msg: String,
  },
  methods: {
    humanize(text) {
      if (typeof text == "string") return humanizeString(text)
      else return null
    }
  }
}
</script>
