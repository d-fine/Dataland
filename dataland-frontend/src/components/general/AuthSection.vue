<template>
  <div class="header__authsection">
    <Button label="Log in" data-test="login-dataland-button" @click="login" variant="outlined" />
    <Button label="Try it free" data-test="signup-dataland-button" @click="register" />
  </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';

const login = (): void => {
  globalThis.location.assign('/login');
};

const register = (): void => {
  globalThis.location.assign('/register');
};
</script>
