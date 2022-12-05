<template>
  <TheHeader />
  <Dialog
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
    <b>If you confirm, your previous token will be invalidated and your applications will stop working.</b>
    <template #footer>
      <Button label="CANCEL" @click="regenerateConfirmToggle" class="p-button-outlined text-sm" />
      <Button label="CONFIRM" class="text-sm" />
    </template>
  </Dialog>
  <TheContent class="surface-800 flex">
    <div class="col-12 text-left pb-0">
      <BackButton />
      <h1>API</h1>
      <Button @click="thereIsApiKey = !thereIsApiKey" label="There is API key" class="align-self-end"></Button>
    </div>

    <MiddleCenterDiv v-if="!thereIsApiKey" class="col-12">
      <div>
        <img src="@/assets/images/elements/bulb_icon.svg" />
        <p class="font-medium text-xl text-color-third">You have no API Key!</p>
        <Button @click="gotoCreateApiKeyPage" label="CREATE NEW API KEY" icon="pi pi-plus"></Button>
      </div>
    </MiddleCenterDiv>

    <div v-if="thereIsApiKey && !waitingForData">
      <div class="col-12 md:col-8 lg:col-6">
        <MessageComponent severity="success" :closable="false" class="border-2">
          <template #text-info>
            <div class="col-12">Make sure to copy your API Key now. You will not be able to access it again.</div>
            <div class="my-2">
              <div class="p-input-icon-right border-round-sm form-inputs-bg pl-1 col-10">
                <InputText
                  ref="newKeyHolderRef"
                  v-on:focus="$event.target.select()"
                  type="text"
                  v-model="newKey"
                  id="newKeyHolder"
                  readonly
                  placeholder="Key goes here"
                  class="p-inputtext p-component col-10"
                />
                <i @click="copyToClipboard" class="pi pi-clone form-inputs-bg primary-color coppy-button" />
              </div>
            </div>
          </template>
        </MessageComponent>
        <MessageComponent severity="block" class="border-2">
          <template #text-info> If you don't have access to your API Key you can generate a new one. </template>
          <template #action-button>
            <Button @click="regenerateConfirmToggle" label="REGENERATE API KEY"></Button>
          </template>
        </MessageComponent>
        <ApiKeyCard />
      </div>
    </div>
  </TheContent>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Tooltip from "primevue/tooltip";
import Button from "primevue/button";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import ApiKeyCard from "@/components/general/ApiKeyCard.vue";
import MessageComponent from "@/components/messages/MessageComponent.vue";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";

export default defineComponent({
  name: "ApiKeysPage",
  components: {
    TheContent,
    TheHeader,
    MiddleCenterDiv,
    BackButton,
    MarginWrapper,
    Button,
    Dialog,
    InputText,
    ApiKeyCard,
    MessageComponent,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      thereIsApiKey: false,
      waitingForData: false,
      regenerateConfirmationVisible: false,
      newKey: "bkjdbaksbdase3udj3y432464329",
    };
  },
  props: {},
  mounted() {
    void this.getApiKeyMetaInfoForUser()
  },
  watch: {},
  directives: {
    tooltip: Tooltip,
  },
  methods: {

    async getApiKeyMetaInfoForUser() {
      try {

        const keycloakPromiseGetter = assertDefined(this.getKeycloakPromise)
        const resolvedKeycloakPromise = await keycloakPromiseGetter() as Keycloak
        const apiKeyManagerController = await new ApiClientProvider(
            keycloakPromiseGetter()
        ).getApiKeyManagerController();
        const apiKeyMetaInfoForUser = await apiKeyManagerController.getApiKeyMetaInfoForUser(
            resolvedKeycloakPromise.subject!!
        )

        console.log( apiKeyMetaInfoForUser ) // TODO debug statement to show what you get
        this.thereIsApiKey = apiKeyMetaInfoForUser.data.active!!

      } catch (error) {
        console.error(error);
      }
    },

    async generateApiKey() {
      try {
        this.waitingForData = true;

        const apiKeyManagerController = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getApiKeyManagerController();
        const response = await apiKeyManagerController.generateApiKey();
        this.newKey = response.data.apiKey;
        this.waitingForData = false;
        this.thereIsApiKey = true;
      } catch (error) {
        console.error(error);
      }
    },

    gotoCreateApiKeyPage() {
      this.$router.push("/create-api-key");
    },
    regenerateConfirmToggle() {
      this.regenerateConfirmationVisible = !this.regenerateConfirmationVisible;
    },
    copyToClipboard() {
      console.log("newKeyHolderRef", this.$refs.newKeyHolderRef);
      this.$refs.newKeyHolderRef.$el.focus();
      navigator.clipboard.writeText(this.newKey);
      console.log("COPIED", document.execCommand("pased"));
      // this.$buefy.toast.open('Copied!');
    },
  },
});
</script>

<style scoped>
.coppy-button {
  cursor: pointer;
}
.p-inputtext:enabled:focus {
  box-shadow: none;
}
</style>
