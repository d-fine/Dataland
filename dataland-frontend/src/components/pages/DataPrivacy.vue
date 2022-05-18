<template>
  <div class="pb-8">
    <div class="m-0">
      <div class="grid align-items-center m-0">
        <div class="col-6 text-left">
          <router-link to="/">
            <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pt-2"/>
          </router-link>
          <p>
            <MarginWrapper class="text-left pl-2">
              <BackButton/>
            </MarginWrapper>
          </p>
          <div class="pl-4">
            <h2>
              Data Privacy
            </h2>
            <p>
              to do
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import BackButton from "@/components/general/BackButton";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "DataPrivacy",
  components: {
    BackButton, MarginWrapper
  }
}
</script>
