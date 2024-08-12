<template>
  <AuthenticationWrapper>
    <TheHeader />

    <TheContent class="paper-section flex">
      <div class="col-12 text-left pb-0">
        <BackButton />
        <h1>{{ pageTitle }}</h1>
      </div>
      <div class="grid m-0">
        <MiddleCenterDiv v-if="waitingForData" class="col-12">
          <div class="col-6 md:col-8 lg:col-12">
            <p class="font-medium text-xl">Loading Api Key information...</p>
            <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
          </div>
        </MiddleCenterDiv>

        <MiddleCenterDiv
          data-test="noApiKeyWelcomeComponent"
          v-if="!userAlreadyHasApiKey && !waitingForData && pageState !== 'create'"
          class="col-12"
        >
          <div>
            <img alt="light bulb" src="@/assets/images/elements/bulb_icon.svg" />
            <p class="font-medium text-xl text-color-third">You have no API Key!</p>
            <PrimeButton @click="setActivePageState('create')" label="CREATE NEW API KEY" icon="pi pi-plus" />
          </div>
        </MiddleCenterDiv>

        <div
          data-test="CreateApiKeyCard"
          class="col-12 md:col-7 lg:col-7"
          v-if="pageState === 'create' && !waitingForData"
        >
          <CreateApiKeyCard
            :userRoles="userRolesAccordingToKeycloak"
            @cancelCreate="setActivePageState('view')"
            @generateApiKey="generateApiKey"
          />
        </div>

        <div
          data-test="apiKeyInfo"
          class="apiKeyInfo col-12 md:col-7 lg:col-7"
          v-if="userAlreadyHasApiKey && !waitingForData && pageState === 'view'"
        >
          <div class="">
            <MessageComponent
              data-test="newKeyHolder"
              v-if="newKey.length"
              severity="success"
              :closable="false"
              class="border-2"
            >
              <template #text-info>
                <div class="col-12">Make sure to copy your API Key now. You will not be able to access it again.</div>
                <div class="my-2">
                  <div class="p-input-icon-right border-round-sm paper-section pl-1 col-10 fs-lg">
                    <PrimeTextarea
                      ref="newKeyHolderRef"
                      @focus="($event.target as HTMLTextAreaElement).select()"
                      :autoResize="true"
                      cols="5"
                      v-model="newKey"
                      id="newKeyHolder"
                      readonly
                      placeholder="Key goes here"
                      class="p-inputText p-component col-10"
                    />
                    <em @click="copyToClipboard" class="pi pi-clone form-inputs-bg primary-color copy-button fs-lg" />
                  </div>
                </div>
              </template>
            </MessageComponent>

            <MessageComponent data-test="regenerateApiKeyMessage" v-if="!newKey" severity="block" class="border-2">
              <template #text-info> If you don't have access to your API Key you can generate a new one. </template>
              <template #action-button>
                <PrimeButton @click="regenerateConfirmToggle" label="REGENERATE API KEY" />
              </template>
            </MessageComponent>

            <ApiKeyCard
              :userRoles="userRolesAccordingToApiKey"
              :expiryDateInMilliseconds="expiryDate"
              @revokeKey="revokeApiKey"
            />
            <div id="apiKeyUsageInfoMessage" class="surface-card shadow-1 p-3 border-round-sm border-round mt-3">
              <div>
                <div class="text-900 font-medium text-xl text-left">API Key usage info</div>
                <div class="block text-600 mb-2 mt-4 text-left">
                  You can use the API-Keys as bearer-tokens by including them in the authorization header of any
                  requests you make to Dataland (i.e., "Authorization: Bearer &lt;API Key&gt;").
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-12 md:col-4 lg:col-4 api-doc">
          <div class="text-900 font-medium text-xl text-left">API Documentation</div>
          <div class="api-doc_links">
            <a href="/api/swagger-ui/index.html" target="_blank" rel="noopener noreferrer">Datasets</a>
            <a href="/documents/swagger-ui/index.html" target="_blank" rel="noopener noreferrer">Documents</a>
            <a href="/community/swagger-ui/index.html" target="_blank" rel="noopener noreferrer">Community</a>
            <a href="/qa/swagger-ui/index.html" target="_blank" rel="noopener noreferrer">Quality Assurance</a>
          </div>
        </div>
      </div>
    </TheContent>

    <PrimeDialog
      id="regenerateApiKeyModal"
      :dismissableMask="true"
      :modal="true"
      header="Header"
      footer="Footer"
      v-model:visible="regenerateConfirmationVisible"
    >
      <template #header>
        <h2 class="m-0">Regenerate API Key</h2>
      </template>
      Are you sure you want to Regenerate your API Key?
      <strong>If you confirm, your previous token will be invalidated and your applications will stop working.</strong>
      <template #footer>
        <PrimeButton
          data-test="regenerateApiKeyCancelButton"
          label="CANCEL"
          @click="regenerateConfirmToggle"
          class="p-button-outlined"
        />
        <PrimeButton
          data-test="regenerateApiKeyConfirmButton"
          label="CONFIRM"
          @click="
            () => {
              setActivePageState('create');
              regenerateConfirmToggle();
            }
          "
        />
      </template>
    </PrimeDialog>
    <TheFooter :is-light-version="true" :sections="footerContent?.sections" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { type ComponentPublicInstance, defineComponent, inject, ref } from 'vue';
import PrimeButton from 'primevue/button';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import MiddleCenterDiv from '@/components/wrapper/MiddleCenterDivWrapper.vue';
import BackButton from '@/components/general/BackButton.vue';
import ApiKeyCard from '@/components/resources/apiKey/ApiKeyCard.vue';
import CreateApiKeyCard from '@/components/resources/apiKey/CreateApiKeyCard.vue';
import MessageComponent from '@/components/messages/MessageComponent.vue';
import PrimeDialog from 'primevue/dialog';
import PrimeTextarea from 'primevue/textarea';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import { type ApiKeyControllerApiInterface } from '@clients/apikeymanager';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';

export default defineComponent({
  name: 'ApiKeysPage',
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
    TheFooter,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      newKeyHolderRef: ref<ComponentPublicInstance<typeof PrimeTextarea> | null>(null),
    };
  },
  data() {
    return {
      pageState: 'view',
      userAlreadyHasApiKey: false,
      waitingForData: true,
      regenerateConfirmationVisible: false,
      newKey: '',
      expiryDate: undefined as undefined | number,
      userRolesAccordingToApiKey: [] as Array<string>,
      userRolesAccordingToKeycloak: [] as Array<string>,
      content: {} as Content,
      footerContent: undefined as Page | undefined,
    };
  },
  computed: {
    pageTitle() {
      if (this.pageState === 'view') return 'API Key';
      if (this.pageState === 'create') return 'Create new API Key';
      return 'API';
    },
  },
  props: {},
  mounted() {
    void this.getApiKeyMetaInfoForUser();
  },
  methods: {
    /**
     * Updates the page state. Possible options are "view" and "create"
     * @param state the new page state
     */
    setActivePageState(state: string) {
      this.pageState = state;
    },

    /**
     * Called during initialisation. Uses the Dataland API to check if the user already has an existing API key.
     * Updates the UI according to the retrieved meta-information.
     */
    async getApiKeyMetaInfoForUser() {
      this.content = contentData;
      this.footerContent = this.content.pages.find((page) => page.url === '/');
      try {
        const keycloakPromiseGetter = assertDefined(this.getKeycloakPromise);
        const resolvedKeycloakPromise = await keycloakPromiseGetter();
        const apiKeyManagerController: ApiKeyControllerApiInterface = new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).apiClients.apiKeyController;
        const apiKeyMetaInfoForUser = await apiKeyManagerController.getApiKeyMetaInfoForUser();
        this.waitingForData = false;
        this.userRolesAccordingToApiKey = apiKeyMetaInfoForUser.data.keycloakRoles
          ? apiKeyMetaInfoForUser.data.keycloakRoles
          : [];
        this.userRolesAccordingToKeycloak = resolvedKeycloakPromise.tokenParsed?.realm_access?.roles
          ? resolvedKeycloakPromise.tokenParsed?.realm_access?.roles
          : [];
        this.userAlreadyHasApiKey = apiKeyMetaInfoForUser.data.active ? apiKeyMetaInfoForUser.data.active : false;
        this.expiryDate = apiKeyMetaInfoForUser.data.expiryDate ? apiKeyMetaInfoForUser.data.expiryDate : undefined;
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * Called on the revokeApiKey event emitted from the ApiKeyCard. Uses the Dataland API to revoke any existing api key.
     * Updates the UI accordingly.
     */
    async revokeApiKey() {
      try {
        const keycloakPromiseGetter = assertDefined(this.getKeycloakPromise);
        const apiKeyManagerController = new ApiClientProvider(keycloakPromiseGetter()).apiClients.apiKeyController;
        await apiKeyManagerController.revokeApiKey();
        this.userAlreadyHasApiKey = false;
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * Called when the generateApiKey event is emitted form the CreateApiKeyCard. Uses the Dataland API to
     * generate a new API key with the specified validity time. Updates the UI to display the new key
     * @param daysValid the number of days the api key is valid for
     */
    async generateApiKey(daysValid?: number) {
      try {
        this.waitingForData = true;
        const keycloakPromiseGetter = assertDefined(this.getKeycloakPromise);

        const apiKeyManagerController = new ApiClientProvider(keycloakPromiseGetter()).apiClients.apiKeyController;
        const response = await apiKeyManagerController.generateApiKey(daysValid);
        this.waitingForData = false;
        this.userAlreadyHasApiKey = true;
        this.expiryDate = response.data.apiKeyMetaInfo.expiryDate ? response.data.apiKeyMetaInfo.expiryDate : undefined;
        this.newKey = response.data.apiKey;
        this.userRolesAccordingToApiKey = response.data.apiKeyMetaInfo.keycloakRoles
          ? response.data.apiKeyMetaInfo.keycloakRoles
          : [];
        this.setActivePageState('view');
      } catch (error) {
        console.error(error);
        this.userAlreadyHasApiKey = false;
      }
    },

    /**
     * Toggles the visibility of the "confirm regeneration" popup
     */
    regenerateConfirmToggle() {
      this.regenerateConfirmationVisible = !this.regenerateConfirmationVisible;
    },

    /**
     * Highlights the newly generated API key and copies it to the clipboard
     */
    copyToClipboard() {
      if (this.newKeyHolderRef) {
        (this.newKeyHolderRef.$el as HTMLTextAreaElement).focus();
      }
      void navigator.clipboard.writeText(this.newKey);
    },
  },
});
</script>

<style lang="scss" scoped>
.copy-button {
  cursor: pointer;
}
.p-inputText:enabled:focus {
  box-shadow: none;
}
.apiKeyInfo .p-message-success {
  background-color: var(--green-600);
  border-color: var(--green-600);
  color: white;
}
.api-doc {
  margin-left: auto;
  &_links {
    margin-top: 28px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    a {
      color: #0b191f;
      font-size: 14px;
      font-style: normal;
      font-weight: 600;
      line-height: 24px;
      letter-spacing: 0.25px;
      text-decoration: none;
      &::after {
        content: '';
        display: inline-block;
        top: 3px;
        width: 16px;
        height: 16px;
        background-image: url(/static/icons/Arrow--up-right.svg);
        background-size: cover;
        position: relative;
        margin-left: 8px;
      }
      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
