<template>
  <div :class="{'progressbar-finished':processIsFinished }" id="progress-bar">
    <ProgressBar :value="progressInPercent" :showValue="false" />
    <p :class="[processIsFinished ? 'progressbar-finished' : 'text-primary']" class="m-2 font-medium text-3xl">{{ progressInPercent + "%" }}</p>
  </div>
  <div v-if="!processIsFinished" class="mt-5">
    <span class="pl-3 pr-3 pt-1 pb-1 bg-white border-1 progressbar-window-indication font-semibold"
      >Please don't close the window</span
    >
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeProgressNar from "primevue/progressbar";

export default defineComponent({
  name: "BackButton",
  components: {
    ProgressBar: PrimeProgressNar,
  },
  emits: ["finished"],

  data() {
    return {
      processIsFinished: false,
    };
  },

  watch: {
    progressInPercent(newValue) {
      if (newValue === 100) {
        this.processIsFinished = true;
        this.$emit("finished");
      }
    },
  },

  props: {
    progressInPercent: {
      type: Number,
      default: 0,
    },
    progressBarSuccessColorCode: {
      type: String,
      default: "#4BB917",
    },
  },
  methods: {
    goBack(): void {
      this.$router.go(-1);
    },
  },
});
</script>

<style>

.progressbar-finished p {
  color: #4bb917;
}

.progressbar-finished .p-progressbar-value {
  background-color: #4bb917;
  color: #4bb917;
}


.progressbar-window-indication {
  border-color: #ee1a1a;
  border-radius: 4px;
  color: #ee1a1a;
}
</style>