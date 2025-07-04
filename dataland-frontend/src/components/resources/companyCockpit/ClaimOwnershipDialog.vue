<template>
  <PrimeDialog
    id="claimOwnerShipDialog"
    :dismissable-mask="true"
    :modal="true"
    header="Header"
    :closable="!claimIsSubmitted"
    footer="Footer"
    class="col-6"
    v-model:visible="dialogIsVisible"
  >
    <template #header>
      <h2 v-if="!claimIsSubmitted" class="m-0">Claim ownership for your company.</h2>
      <h2 v-else class="m-0">Thank you for claiming company ownership for {{ companyName }}.</h2>
    </template>

    <div v-if="!claimIsSubmitted">
      <p data-test="claimOwnershipDialogMessage">
        Are you responsible for the datasets of {{ companyName }}? Claim company ownership in order to ensure high
        quality and transparency over your company's data.
      </p>
      <p>Feel free to share any additional information with us:</p>

      <FormKit
        v-model="claimOwnershipMessage"
        type="textarea"
        name="claimOwnershipMessage"
        placeholder="Write your message."
        data-test="messageInputField"
        wrapper-class="full-width-wrapper"
        input-class="textarea"
        inner-class="no-shadow"
      />
    </div>
    <div v-else>
      <p data-test="claimOwnershipDialogSubmittedMessage">We will reach out to you soon via email.</p>
    </div>
    <template #footer v-if="!claimIsSubmitted">
      <PrimeButton class="w-full" label="SUBMIT" @click="submitInput" data-test="submitButton" />
    </template>
    <template #footer v-else>
      <PrimeButton class="p-button p-button-outlined" label="CLOSE" @click="closeDialog" data-test="closeButton" />
    </template>
  </PrimeDialog>
</template>

<script lang="ts">
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { inject, defineComponent } from 'vue';
import type Keycloak from 'keycloak-js';
import { FormKit } from '@formkit/vue';

export default defineComponent({
  name: 'ClaimOwnershipDialog',
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  components: {
    PrimeDialog,
    PrimeButton,
    FormKit,
  },
  data() {
    return {
      claimOwnershipMessage: '',
      dialogIsVisible: false,
    };
  },
  props: {
    dialogIsOpen: {
      type: Boolean,
      required: false,
      default: false,
    },
    companyName: {
      type: String,
      required: true,
    },
    companyId: {
      type: String,
      required: true,
    },
    claimIsSubmitted: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  emits: ['claimSubmitted', 'closeDialog'],
  methods: {
    /**
     * Makes the API request in order to post the request for company ownership
     */
    async submitInput(): Promise<void> {
      const companyRolesControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
        .companyRolesController;
      try {
        const axiosResponse = await companyRolesControllerApi.postCompanyOwnershipRequest(
          this.companyId,
          this.claimOwnershipMessage ? this.claimOwnershipMessage : undefined
        );
        if (axiosResponse.status == 200) {
          this.$emit('claimSubmitted');
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * closes the dialog window and emits this event
     */
    closeDialog(): void {
      this.dialogIsVisible = false;
      this.$emit('closeDialog');
    },
  },
  watch: {
    dialogIsOpen(newValue: boolean): void {
      this.dialogIsVisible = newValue;
    },
  },
});
</script>

<style lang="scss">
.full-width-wrapper {
  max-width: 100%;
}

.textarea {
  background-color: var(--gray-200);
}

.no-shadow {
  box-shadow: none;
}

.p-button {
  white-space: nowrap;
  cursor: pointer;
  font-weight: var(--button-fw);
  text-decoration: none;
  min-width: 10em;
  width: fit-content;
  justify-content: center;
  display: inline-flex;
  align-items: center;
  vertical-align: bottom;
  flex-direction: row;
  letter-spacing: 0.05em;
  font-family: inherit;
  transition: all 0.2s;
  border-radius: 0;
  text-transform: uppercase;
  font-size: 0.875rem;

  &:enabled:hover {
    color: white;
    background: hsl(from var(--btn-primary-bg) h s calc(l - 20));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 20));
  }

  &:enabled:active {
    background: hsl(from var(--btn-primary-bg) h s calc(l - 10));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 10));
  }

  &:disabled {
    background-color: transparent;
    border: 0;
    color: var(--btn-disabled-color);
    cursor: not-allowed;
  }

  &:focus {
    outline: 0 none;
    outline-offset: 0;
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }
}

.p-button {
  color: var(--btn-primary-color);
  background: var(--btn-primary-bg);
  border: 1px solid var(--btn-primary-bg);
  padding: var(--spacing-xs) var(--spacing-md);
  line-height: 1rem;
  margin: var(--spacing-xxs);

  &.p-button-danger {
    background: var(--btn-danger-bg);
    border: 1px solid var(--btn-danger-bg);

    &:enabled:hover {
      background: hsl(from var(--btn-danger-bg) h s calc(l - 20));
      border-color: hsl(from var(--btn-danger-bg) h s calc(l - 20));
    }

    &:enabled:active {
      background: hsl(from var(--btn-danger-bg) h s calc(l - 10));
      border-color: hsl(from var(--btn-danger-bg) h s calc(l - 10));
    }
  }

  &.p-button-success {
    background: var(--btn-success-bg);
    border: 1px solid var(--btn-success-bg);

    &:enabled:hover {
      background: hsl(from var(--btn-success-bg) h s calc(l - 20));
      border-color: hsl(from var(--btn-success-bg) h s calc(l - 20));
    }

    &:enabled:active {
      background: hsl(from var(--btn-success-bg) h s calc(l - 10));
      border-color: hsl(from var(--btn-success-bg) h s calc(l - 10));
    }
  }

  &.p-button-sm {
    font-size: var(--font-size-sm);
    padding: var(--spacing-xs) var(--spacing-sm);
  }

  &.p-button-icon {
    padding: var(--spacing-xxs);
    line-height: 0.75rem;

    .pi {
      font-size: var(--font-size-xs);
    }
  }

  &.p-button-textcolor {
    background-color: transparent;
    color: var(--main-text-color);
    border-color: transparent;

    &:enabled:hover {
      color: var(--btn-primary-bg);
      background-color: transparent;
    }

    &:enabled:active {
      color: var(--btn-primary-bg);
      border-color: var(--btn-primary-color);
    }
  }

  &.p-button-outlined {
    background-color: transparent;
    color: var(--btn-primary-bg);
    border-style: solid;
    border-color: var(--btn-primary-bg);
    border-width: 1px;

    &:enabled:hover {
      background: hsl(from var(--btn-primary-bg) h s 45);
    }

    &:enabled:active {
      background: hsl(from var(--btn-primary-bg) h s 40);
    }

    &.p-button-danger {
      color: var(--btn-danger-bg);
      border-color: var(--btn-danger-bg);

      &:enabled:hover {
        background: hsl(from var(--btn-danger-bg) h s 45);
      }

      &:enabled:active {
        background: hsl(from var(--btn-danger-bg) h s 40);
      }
    }

    &.p-button-success {
      color: var(--btn-success-bg);
      border-color: var(--btn-success-bg);

      &:enabled:hover {
        background: hsl(from var(--btn-success-bg) h s 45);
      }

      &:enabled:active {
        background: hsl(from var(--btn-success-bg) h s 40);
      }
    }
  }

  &.p-button-text {
    background-color: transparent;
    color: var(--btn-primary-bg);
    border: 0;

    &:enabled:hover {
      color: hsl(from var(--btn-primary-bg) h s calc(l - 30));
      background: transparent;
      border: 0;
    }

    &:enabled:active {
      background: transparent;
      border: 0;
    }

    &.p-button-danger {
      color: var(--btn-danger-bg);

      &:enabled:hover {
        background: transparent;
        border: 0;
      }

      &:enabled:active {
        background: transparent;
        border: 0;
      }
    }

    &.p-button-success {
      color: var(--btn-success-bg);

      &:enabled:hover {
        background: transparent;
        border: 0;
      }

      &:enabled:active {
        background: transparent;
        border: 0;
      }
    }
  }

  .p-button-icon-left {
    margin-right: 0.5rem;
  }
}
</style>
