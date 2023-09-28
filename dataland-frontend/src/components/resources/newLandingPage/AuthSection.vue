<template>
  <div class="header__authsection">
    <a
      aria-label="Login to preview account"
      class="header__authsection-login"
      name="login_dataland_button"
      @click="login"
    >
      Login
    </a>
    <button aria-label="Sign up to preview account" class="header__authsection-button" name="signup_dataland_button">
      Sign Up
    </button>
  </div>
</template>

<script setup lang="ts">
import { inject } from "vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { loginAndRedirectToSearchPage } from "@/utils/KeycloakUtils";
import type Keycloak from "keycloak-js";

const getKeycloakPromise = inject<() => Promise<Keycloak>>("getKeycloakPromise");

/**
 * Sends the user to the keycloak login page (if not authenticated already)
 */
const login = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (!keycloak.authenticated) {
        loginAndRedirectToSearchPage(keycloak);
      }
    })
    .catch((error) => console.log(error));
};
</script>
<style scoped lang="scss">
.d-text-register {
  color: #5a4f36;
}

.header {
  &__authsection {
    display: flex;
    gap: 32px;
    align-items: center;

    &-login {
      position: relative;
      text-decoration: none;
      color: #1b1b1b;
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 20px;
      letter-spacing: 0.75px;
      text-transform: uppercase;
      cursor: pointer;
      border-bottom: 2px solid transparent;

      &::before {
        content: "";
        display: block;
        position: absolute;
        left: -20px;
        top: 50%;
        transform: translateY(-50%);
        width: 16px;
        height: 16px;
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 16 16' fill='none'%3E%3Crect width='16' height='16' fill='white' fill-opacity='0.01' style='mix-blend-mode:multiply'/%3E%3Cpath d='M13 15.2H13.2V15V12.5C13.2 11.5187 12.8102 10.5776 12.1163 9.88371C11.4224 9.18982 10.4813 8.8 9.5 8.8H6.5C5.5187 8.8 4.57759 9.18982 3.88371 9.88371C3.18982 10.5776 2.8 11.5187 2.8 12.5V15V15.2H3H4H4.2V15V12.5C4.2 11.89 4.44232 11.305 4.87365 10.8737C5.30499 10.4423 5.89 10.2 6.5 10.2H9.5C9.80204 10.2 10.1011 10.2595 10.3802 10.3751C10.6592 10.4907 10.9128 10.6601 11.1263 10.8737C11.3399 11.0872 11.5093 11.3408 11.6249 11.6198C11.7405 11.8989 11.8 12.198 11.8 12.5V15V15.2H12H13ZM8 2.2C8.4549 2.2 8.89958 2.33489 9.27781 2.58762C9.65604 2.84035 9.95084 3.19956 10.1249 3.61983C10.299 4.0401 10.3446 4.50255 10.2558 4.94871C10.1671 5.39486 9.94801 5.80468 9.62635 6.12635C9.30468 6.44801 8.89486 6.66706 8.44871 6.75581C8.00255 6.84455 7.5401 6.799 7.11983 6.62492C6.69956 6.45084 6.34035 6.15604 6.08762 5.77781C5.83489 5.39958 5.7 4.9549 5.7 4.5C5.7 3.89 5.94232 3.30499 6.37365 2.87365C6.80499 2.44232 7.39 2.2 8 2.2ZM8 0.8C7.26821 0.8 6.55285 1.017 5.94439 1.42356C5.33593 1.83012 4.86169 2.40798 4.58165 3.08407C4.3016 3.76016 4.22833 4.5041 4.37109 5.22183C4.51386 5.93956 4.86625 6.59884 5.38371 7.11629C5.90116 7.63375 6.56044 7.98614 7.27817 8.12891C7.9959 8.27167 8.73984 8.1984 9.41593 7.91835C10.092 7.63831 10.6699 7.16407 11.0764 6.55561C11.483 5.94715 11.7 5.23179 11.7 4.5C11.7 3.5187 11.3102 2.57759 10.6163 1.8837C9.92241 1.18982 8.9813 0.8 8 0.8Z' fill='%231B1B1B' stroke='%231B1B1B' stroke-width='0.4'/%3E%3C/svg%3E");
        background-size: cover;
      }
      &:hover {
        border-bottom: 2px solid #1b1b1b;
        &::before {
          content: none;
        }
      }
    }
    &-button {
      padding: 14px 32px;
      border-radius: 32px;
      background-color: #ff6813;
      color: #fff;
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 20px;
      letter-spacing: 0.75px;
      text-transform: uppercase;
      border: 2px solid #ff6813;
      cursor: pointer;
      &:hover {
        background-color: #fff;
        color: #1b1b1b;
      }
    }
  }
}
</style>
