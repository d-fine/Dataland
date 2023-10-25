<template>
  <div class="header__authsection">
    <a aria-label="Login to preview account" class="header__authsection-login" @click="login"> Login </a>
    <button
      aria-label="Sign up to preview account"
      class="header__authsection-button"
      name="signup_dataland_button"
      @click="register"
    >
      Sign Up
    </button>
  </div>
</template>

<script setup lang="ts">
import { inject } from "vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { loginAndRedirectToSearchPage, registerAndRedirectToSearchPage } from "@/utils/KeycloakUtils";
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

/**
 * Sends the user to the keycloak register page (if not authenticated already)
 */
const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (!keycloak.authenticated) {
        registerAndRedirectToSearchPage(keycloak);
      }
    })
    .catch((error) => console.log(error));
};
</script>
<style scoped lang="scss">
.header {
  &__authsection {
    display: flex;
    gap: 32px;
    align-items: center;

    &-login {
      position: relative;
      text-decoration: none;
      color: var(--basic-dark);
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
        background-image: url("/static/icons/User.svg");
        background-size: cover;
      }
      &:hover {
        border-bottom: 2px solid var(--primary-orange);
        color: var(--primary-orange);
        &::before {
          background-image: url("/static/icons/User-hover.svg");
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
@media only screen and (max-width: $small) {
  .header {
    &__authsection {
      display: none;
      flex-direction: row-reverse;
      &-login {
        font-size: 0;
        color: transparent;
        &:hover {
          font-size: 0;
          color: transparent;
          border-bottom: 2px solid transparent;
          &::before {
            content: "";
          }
        }
      }
      &-button {
        padding: 10px 16px;
        font-size: 14px;
        letter-spacing: 0.25px;
        text-transform: capitalize;
      }
    }
  }
}
</style>
