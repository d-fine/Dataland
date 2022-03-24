<template>
<div>
    <div class="card">
      <div class="card-title red-text">
        <h2>Sorry, but an Error occurred.</h2>
        <h4>{{msg}} was not uploaded!</h4>
      </div>

    </div>
</div>
</template>

<script>
import {humanizeString} from "@/utils/stringHumanizer"
export default {
  name: "FailedUpload",
  props: {
    msg: String,
    status: Number
  },
  methods: {
    humanize(text) {
      if (typeof text == "string") return humanizeString(text)
      else return null
    }
}}
</script>