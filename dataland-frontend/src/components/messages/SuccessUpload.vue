<template>
  <div>
    <Message severity="success" :sticky="false" :key="messageCount">
      <h4>You successfully uploaded the {{ msg }}</h4>
      <ul class="list-none align-content-start">
        <li v-for="(value, key) in data" :key="key">
          <span>{{ humanize(key) }}</span
          >: <span :title="key">{{ value }}</span>
        </li>
      </ul>
    </Message>
  </div>
</template>

<script lang="ts">
import { humanizeString } from "@/utils/StringHumanizer";
import Message from "primevue/message";

export default {
  name: "SuccessUpload",
  components: { Message },
  props: {
    data: Object,
    msg: String,
    messageCount: Number,
  },
  methods: {
    humanize(text: unknown): string | null {
      if (typeof text == "string") return humanizeString(text);
      else return null;
    },
  },
};
</script>
