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
              <div class="card__title">Request Status:</div>
              <div class="card__separator" />
              Test blabla {{ requestId }}
            </div>
            <div class="card">
              <div class="card__title">Withdraw Request</div>
              <div class="card__separator" />
              Test blabla {{ requestId }} Withdraw request
            </div>
          </div>
        </div>
      </div>
    </div>
    <TheFooter :is-light-version="true" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
export default defineComponent({
  name: "ViewDataRequest",
  components: { BackButton, AuthenticationWrapper, TheHeader, TheFooter },
  props: {
    requestId: {
      type: String,
      required: false, //todo
      default: "12345",
    },
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

  &--interactive {
    cursor: pointer;

    &:hover {
      box-shadow: 0 0 32px 8px #1e1e1e14;

      .summary-panel__separator {
        border-bottom-color: var(--primary-color);
      }
    }
  }
  &__title {
    font-size: 21px;
    font-weight: 700;
    line-height: 27px;
  }

  &__subtitle {
    font-size: 16px;
    font-weight: 400;
    line-height: 21px;

    margin-top: 8px;
  }

  &__subtitle-placeholder {
    margin-top: 8px;
    height: 21px;
  }

  &__separator {
    width: 100%;
    border-bottom: #e0dfde solid 1px;
    margin-top: 8px;
    margin-bottom: 24px;
  }

  &__data {
    font-size: 16px;
    font-weight: 300;
    line-height: 21px;
  }

  &__value {
    font-weight: 600;
  }

  &__provide-button {
    display: block;
    cursor: pointer;
    width: 100%;
    color: var(--primary-color);
    border: var(--primary-color) solid 2px;
    text-decoration-line: none;
    font-size: 16px;
    font-weight: 600;
    line-height: 20px;
    padding-top: 10px;
    padding-bottom: 10px;
    text-align: center;
  }
}
</style>
