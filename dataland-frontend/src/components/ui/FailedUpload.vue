<template>
  <div>
    <Message severity="error" :sticky="false" :key="messageCount">
      <h4>Sorry, but an Error occurred.</h4>
      <h4>{{ msg }} was not uploaded!</h4>
    </Message>
  </div>
</template>

<script>
import Message from 'primevue/message';

import {StringHumanizer} from "@/utils/StringHumanizer"

const stringHumanizer = new StringHumanizer()

export default {
  name: "FailedUpload",
  components: {Message},
  props: {
    msg: String,
    messageCount: Number
  },
  methods: {
    humanize(text) {
      if (typeof text == "string") return stringHumanizer.humanize(text)
      else return null
    }
  }
}
</script>