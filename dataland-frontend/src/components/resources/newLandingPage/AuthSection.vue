<template>
  <template v-if="isLandingPage">
    <div v-if="isUserLoggedIn == true" data-test="backToPlatformLink">
      <a class="fw-semi-bold vertical-middle cursor-pointer" @click="backToPlatform"
        >BACK TO PLATFORM <i class="material-icons pl-1" aria-hidden="true" alt="arrow_forward">arrow_forward</i></a
      >
    </div>
    <div v-if="!isUserLoggedIn" class="header__authsection">
      <a aria-label="Login to account" class="header__authsection-login" @click="login"> Login </a>
      <ButtonComponent label="Sign Up" ariaLabel="Sign up to account" name="signup_dataland_button" @click="register" />
    </div>
  </template>
  <template v-else>
    <div class="header__authsection">
      <div v-if="!isUserLoggedIn && isSmallScreen" class="dropdown">
        <div class="dropdown-toggle" role="button" tabindex="0" @click="toggleDropdown">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <g id="SVGRepo_bgCarrier" stroke-width="0"></g>
            <g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g>
            <g id="SVGRepo_iconCarrier">
              <path d="M4 18L20 18" stroke="#5b5b5b" stroke-width="2" stroke-linecap="round"></path>
              <path d="M4 12L20 12" stroke="#5b5b5b" stroke-width="2" stroke-linecap="round"></path>
              <path d="M4 6L20 6" stroke="#5b5b5b" stroke-width="2" stroke-linecap="round"></path>
            </g>
          </svg>
        </div>
        <div v-if="isDropdownOpen" class="dropdown-menu">
          <ButtonComponent
            label="Log in"
            buttonType="login-button"
            ariaLabel="Login to account"
            name="login_dataland_button"
            @click="login"
          />
          <ButtonComponent
            label="Sign Up"
            buttonType="registration-button"
            ariaLabel="Sign up to account"
            name="signup_dataland_button"
            @click="register"
          />
        </div>
      </div>
      <div v-else class="menu-large-screen">
        <ButtonComponent
          label="Log in"
          buttonType="login-button"
          ariaLabel="Login to account"
          name="login_dataland_button"
          @click="login"
        />
        <ButtonComponent
          label="Sign Up"
          buttonType="registration-button"
          ariaLabel="Sign up to account"
          name="signup_dataland_button"
          @click="register"
        />
      </div>
    </div>
  </template>
</template>

<script setup lang="ts">
import { inject, onMounted, onBeforeUnmount, ref } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import ButtonComponent from '@/components/resources/newLandingPage/ButtonComponent.vue';
import { useRouter } from 'vue-router';
import { loginAndRedirectToSearchPage, registerAndRedirectToSearchPage } from '@/utils/KeycloakUtils';

const router = useRouter();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const isUserLoggedIn = ref<undefined | boolean>(undefined);
const { isLandingPage } = defineProps<{
  isLandingPage: boolean;
}>();
const isDropdownOpen = ref(false);
const isSmallScreen = ref(window.innerWidth < 992);

const toggleDropdown = (): void => {
  isDropdownOpen.value = !isDropdownOpen.value;
};
const handleResize = (): void => {
  isSmallScreen.value = window.innerWidth < 992;
};

// Ensure to close the dropdown on clicking outside
const handleClickOutside = (event: MouseEvent): void => {
  const target = event.target as HTMLElement;
  if (!target.closest('.dropdown')) {
    isDropdownOpen.value = false;
  }
};

onMounted(() => {
  window.addEventListener('click', handleClickOutside);
  window.addEventListener('resize', handleResize);
  handleResize();
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      isUserLoggedIn.value = keycloak.authenticated;
    })
    .catch((error) => console.error(error));
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});
/**
 * Sends the user to the keycloak login page (if not authenticated already)
 */
const login = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) return;
      if (window.location.pathname == '/') {
        loginAndRedirectToSearchPage(keycloak);
      } else {
        keycloak.login().catch((error) => console.error(error));
      }
    })
    .catch((error) => console.error(error));
};

/**
 * Sends the user to back to the platform
 */
const backToPlatform = (): void => {
  void router.push({ path: '/companies' });
};

/**
 * Sends the user to the keycloak register page (if not authenticated already)
 */
const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) return;
      if (window.location.pathname == '/') {
        registerAndRedirectToSearchPage(keycloak);
      } else {
        keycloak.register().catch((error) => console.error(error));
      }
    })
    .catch((error) => console.error(error));
};
</script>
<style scoped lang="scss">
.header {
  &__authsection {
    display: flex;
    gap: 32px;
    align-items: baseline;

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
        content: '';
        display: block;
        position: absolute;
        left: -20px;
        top: 50%;
        transform: translateY(-50%);
        width: 16px;
        height: 16px;
        background-image: url('/static/icons/User.svg');
        background-size: cover;
      }
      &:hover {
        border-bottom: 2px solid var(--primary-orange);
        color: var(--primary-orange);
        &::before {
          background-image: url('/static/icons/User-hover.svg');
        }
      }
    }
  }
}
.dropdown {
  .dropdown-toggle {
    background: none;
    border: none;
    cursor: pointer;
    width: 40px;
    height: 40px;
    margin-right: 5px;
    margin-bottom: 2px;
  }

  .dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    background-color: var(--basic-dark);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    z-index: 15;
    display: flex;
    flex-direction: column;
    padding: 8px;
    margin-top: 0;
    gap: 8px;

    ButtonComponent {
      width: 100%;
    }
  }
}

.menu-large-screen {
  display: flex;
  flex-direction: row;
  gap: 8px;
}
@media only screen and (max-width: $small) {
  .header {
    padding: 16px;
    margin: 0;
    width: 100%;
    border-radius: 0;
  }
}
</style>

<style lang="scss">
.login-button {
  padding: 10px 38px;
  border-radius: 0;
  color: var(--primary-orange);
  background-color: var(--basic-dark);
  border: 2px solid var(--primary-orange);
  text-transform: uppercase;
  cursor: pointer;

  &:hover {
    border: 2px solid var(--default-neutral-white);
    background-color: var(--basic-dark);
    color: var(--default-neutral-white);
  }
}

.registration-button {
  padding: 10px 38px;
  border-radius: 0;
  background-color: var(--primary-orange);
  color: var(--default-neutral-white);
  border: 2px solid var(--primary-orange);
  text-transform: uppercase;
  cursor: pointer;

  &:hover {
    color: var(--basic-dark);
    background-color: var(--default-neutral-white);
  }
}
</style>
