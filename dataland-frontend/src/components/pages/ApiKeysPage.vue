<template>
  <AuthenticationWrapper>
    <TheHeader />

    <TheContent class="surface-800 flex">
      <div class="col-12 text-left pb-0">
        <BackButton />
        <h1>{{ PageTitleState }}</h1>
      </div>

      <MiddleCenterDiv v-if="waitingForData" class="col-12">
        <div class="col-6 md:col-8 lg:col-12">
          <p class="font-medium text-xl">Loading Api Key information...</p>
          <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
        </div>
      </MiddleCenterDiv>

      <MiddleCenterDiv v-if="!existsApiKey && !waitingForData && this.pageState !== 'create'" class="col-12">
        <div>
          <img alt="light bulb" src="@/assets/images/elements/bulb_icon.svg" />
          <p class="font-medium text-xl text-color-third">You have no API Key!</p>
          <PrimeButton @click="setActivePageState('create')" label="CREATE NEW API KEY" icon="pi pi-plus" />
        </div>
      </MiddleCenterDiv>

      <div v-if="this.pageState === 'create' && !waitingForData" class="col-12 md:col-8 lg:col-6">
        <CreateApiKeyCard @cancelCreate="setActivePageState('view')" @generateApiKey="generateApiKey" />
      </div>

      <div v-if="existsApiKey && !waitingForData && pageState === 'view'">
        <div class="col-12 md:col-8 lg:col-6">
          <MessageComponent v-if="newKey.length" severity="success" :closable="false" class="border-2">
            <template #text-info>
              <div class="col-12">Make sure to copy your API Key now. You will not be able to access it again.</div>
              <div class="my-2">
                <div class="p-input-icon-right border-round-sm form-inputs-bg pl-1 col-10">
                  <PrimeTextarea
                    ref="newKeyHolderRef"
                    v-on:focus="$event.target.select()"
                    :autoResize="true"
                    rows="2"
                    cols="5"
                    v-model="newKey"
                    id="newKeyHolder"
                    readonly
                    placeholder="Key goes here"
                    class="p-inputText p-component col-10"
                  />
                  <em @click="copyToClipboard" class="pi pi-clone form-inputs-bg primary-color copy-button" />
                </div>
              </div>
            </template>
          </MessageComponent>

          <MessageComponent v-if="!newKey" severity="block" class="border-2">
            <template #text-info> If you don't have access to your API Key you can generate a new one. </template>
            <template #action-button>
              <PrimeButton @click="regenerateConfirmToggle" label="REGENERATE API KEY" />
            </template>
          </MessageComponent>

          <ApiKeyCard :expiryDate="expiryDate * 1000" @revokeKey="revokeApiKey" />
        </div>
      </div>
    </TheContent>

    <PrimeDialog
      id="regenerateApiKeyModal"
      :modal="true"
      header="Header"
      footer="Footer"
      v-model:visible="regenerateConfirmationVisible"
      :breakpoints="{ '960px': '75vw', '640px': '100vw' }"
      :style="{ width: '50vw' }"
    >
      <template #header>
        <h2 class="m-0">Regenerate API Key</h2>
      </template>
      Are you sure you want to Regenerate your API Key?
      <strong>If you confirm, your previous token will be invalidated and your applications will stop working.</strong>
      <template #footer>
        <PrimeButton label="CANCEL" @click="regenerateConfirmToggle" class="p-button-outlined text-sm" />
        <PrimeButton
          label="CONFIRM"
          @click="
            () => {
              setActivePageState('create');
              regenerateConfirmToggle();
            }
          "
          class="text-sm"
        />
      </template>
    </PrimeDialog>
  </AuthenticationWrapper>
</template>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import { defineComponent, inject, ref } from "vue";
import PrimeButton from "primevue/button";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import ApiKeyCard from "@/components/general/ApiKeyCard.vue";
import CreateApiKeyCard from "@/components/general/CreateApiKeyCard.vue";
import MessageComponent from "@/components/messages/MessageComponent.vue";
import PrimeDialog from "primevue/dialog";
import PrimeTextarea from "primevue/textarea";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";

export default defineComponent({
  name: "ApiKeysPage",
  components: {
    AuthenticationWrapper,
    TheContent,
    TheHeader,
    MiddleCenterDiv,
    BackButton,
    PrimeButton,
    PrimeDialog,
    ApiKeyCard,
    CreateApiKeyCard,
    MessageComponent,
    PrimeTextarea,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      newKeyHolderRef: ref(),
    };
  },
  data() {
    return {
      pageState: "view",
      existsApiKey: false,
      waitingForData: true,
      regenerateConfirmationVisible: false,
      newKey: "",
      expiryDate: 0,
    };
  },
  computed: {
    PageTitleState() {
      if (this.pageState === "view") return "API";
      if (this.pageState === "create") return "Create new API Key";
      return "API";
    },
  },
  props: {},
  mounted() {
    void this.getApiKeyMetaInfoForUser();
  },
  watch: {},
  methods: {
    setActivePageState(state: string) {
      this.pageState = state;
    },

    async getApiKeyMetaInfoForUser() {
      try {
        const keycloakPromiseGetter = assertDefined(this.getKeycloakPromise);
        const resolvedKeycloakPromise = await keycloakPromiseGetter();
        const apiKeyManagerController = await new ApiClientProvider(
          keycloakPromiseGetter()
        ).getApiKeyManagerController();
        const apiKeyMetaInfoForUser = await apiKeyManagerController.getApiKeyMetaInfoForUser(
          resolvedKeycloakPromise.subject as unknown as string
        );
        this.waitingForData = false;
        this.existsApiKey = apiKeyMetaInfoForUser.data.active ? apiKeyMetaInfoForUser.data.active : false;
        this.expiryDate = apiKeyMetaInfoForUser.data.expiryDate ? apiKeyMetaInfoForUser.data.expiryDate : 0;
      } catch (error) {
        console.error(error);
      }
    },

    async revokeApiKey() {
      try {
        const keycloakPromiseGetter = assertDefined(this.getKeycloakPromise);
        const apiKeyManagerController = await new ApiClientProvider(
          keycloakPromiseGetter()
        ).getApiKeyManagerController();
        await apiKeyManagerController.revokeApiKey();
        this.existsApiKey = false;
      } catch (error) {
        console.error(error);
      }
    },

    async generateApiKey(expirationTime: number) {
      try {
        this.waitingForData = true;
        const keycloakPromiseGetter = assertDefined(this.getKeycloakPromise);

        const apiKeyManagerController = await new ApiClientProvider(
          keycloakPromiseGetter()
        ).getApiKeyManagerController();
        const response = await apiKeyManagerController.generateApiKey(expirationTime);
        this.waitingForData = false;
        this.existsApiKey = true;
        this.expiryDate = response.data.apiKeyMetaInfo.expiryDate ? response.data.apiKeyMetaInfo.expiryDate : 0;
        this.newKey = response.data.apiKey;
        this.setActivePageState("view");
      } catch (error) {
        console.error(error);
        this.existsApiKey = false;
      }
    },

    regenerateConfirmToggle() {
      this.regenerateConfirmationVisible = !this.regenerateConfirmationVisible;
    },

    copyToClipboard() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      (this.newKeyHolderRef.$el as HTMLTextAreaElement).focus();
      void navigator.clipboard.writeText(this.newKey);
    },
  },
});
</script>

<style scoped>
.copy-button {
  cursor: pointer;
}
.p-inputText:enabled:focus {
  box-shadow: none;
}
</style>
