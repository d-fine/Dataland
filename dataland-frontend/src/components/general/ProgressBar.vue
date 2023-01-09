<template>
  <div id="centralized-container" class="col-6 col-offset-3 p-0">
    <div id="result-message-container" class="ml-3 mr-3">
      <div
        v-if="processIsFinished"
        class="p-2 mb-5 mt-8 border-2 p-message"
        :class="[wasProcessSuccessful ? 'p-message-success' : 'p-message-error']"
      >
        {{ processResultMessage }}
      </div>
    </div>

    <div id="dataland-progress-bar-absolute-positioned-container">
      <h1 id="current-progress-title" class="pb-5 m-0">
        <img
          v-if="wasProcessSuccessful"
          src="@/assets/images/elements/successful_invite_submission_img.svg"
          alt="success-img"
        />
        {{ progressTitle }}
      </h1>

      <div
        id="progress-container"
        v-if="processHasStarted || wasProcessSuccessful"
        :class="{ 'progressbar-finished': processIsFinished }"
      >
        <PrimeProgressBar :value="progressInPercent" :showValue="false" />
        <p
          id="current-progress-percentage"
          :class="[processIsFinished ? 'progressbar-finished' : 'text-primary']"
          class="m-2 font-medium text-3xl"
        >
          {{ progressInPercent + "%" }}
        </p>
      </div>

      <div v-if="!processIsFinished" id="dont-close-warning" class="mt-5">
        <span class="pl-3 pr-3 pt-1 pb-1 bg-white border-1 progressbar-window-indication font-semibold">
          Please don't close the window
        </span>
      </div>

      <slot name="options"></slot>

      <PrimeButton
        v-if="processIsFinished"
        label="return to home"
        class="mt-6 p-button-sm border-2 uppercase text-primary d-letters bg-white-alpha-10"
        name="back_to_home_button"
        @click="returnToHome"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeProgressBar from "primevue/progressbar";
import PrimeButton from "primevue/button";

export default defineComponent({
  name: "BackButton",
  components: {
    PrimeProgressBar,
    PrimeButton,
  },

  props: {
    processHasStarted: {
      type: Boolean,
      default: false,
    },
    processIsFinished: {
      type: Boolean,
      default: false,
    },
    wasProcessSuccessful: {
      type: Boolean,
      default: false,
    },
    processResultMessage: {
      type: String,
    },
    progressTitle: {
      type: String,
    },
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
    returnToHome() {
      void this.$router.push("/");
    },
  },
});
</script>

<style>
#dataland-progress-bar-absolute-positioned-container {
  width: 50%;
  position: absolute;
  top: 16rem;
}
</style>
