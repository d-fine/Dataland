<template>
  <AuthenticationWrapper>
    <TheHeader />
    <div class="sheet">
      <div class="headline">
        <BackButton />
      </div>
      <h1 class="text-left">Data Request</h1>
    </div>
    <div class="py-4 paper-section">
      <div class="grid col-9 justify-content-around">
        <div class="col-4">
          <div class="card">
            <div class="card__title">Request Details</div>
            <div class="card__separator" />
          </div>
        </div>
        <div class="grid col-8 flex-direction-column">
          <div class="col-12">
            <div class="card">
              <div class="card__title">Request Status</div>
              <a class=""></a>
              <div class="card__separator" />
              <!-- todo -->
              Test blabla {{ requestId }}
            </div>
            <div class="card">
              <div class="card__title">Withdraw Request</div>
              <div class="card__separator" />
              Once a data request is withdrawn, it will be removed from your data request list. The data owner will not
              be notified anymore.
              <a class="link" @click="withdrawRequest"> Withdraw request.</a>
            </div>
          </div>
        </div>
      </div>
    </div>
    <TheFooter :is-light-version="true" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { type StoredDataRequest } from "@clients/communitymanager";
import type Keycloak from "keycloak-js";
export default defineComponent({
  name: "ViewDataRequest",
  components: { BackButton, AuthenticationWrapper, TheHeader, TheFooter },
  props: {
    requestId: {
      type: String,
      required: true,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      storedDataRequest: {} as StoredDataRequest,
    };
  },
  mounted() {
    this.getRequest().catch((error) => console.error(error));
  },
  methods: {
    /**
     * Method to get the request from the api
     */
    async getRequest() {
      try {
        if (this.getKeycloakPromise) {
          this.storedDataRequest = (
            await new ApiClientProvider(this.getKeycloakPromise()).apiClients.requestController.getDataRequestById(
              this.requestId,
            )
          ).data;
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Method to withdraw the request when clicking on the button
     */
    withdrawRequest() {},
  },
});
</script>
<style lang="scss" scoped>
.card {
  width: 100%;
  background-color: var(--surface-card);
  padding: $spacing-md;
  border-radius: $radius-xxs;
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 1rem;

  &__subtitle {
    font-size: 16px;
    font-weight: 400;
    line-height: 21px;

    margin-top: 8px;
  }

  &__title {
    font-size: 21px;
    font-weight: 700;
    line-height: 27px;
  }

  &__separator {
    width: 100%;
    border-bottom: #e0dfde solid 1px;
    margin-top: 8px;
    margin-bottom: 24px;
  }
}
</style>
