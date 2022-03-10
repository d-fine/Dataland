<template>
  <img alt="Vue logo" src="@/assets/images/logos/dataland-logo-lr.png">
  <WelcomeDataland msg="Welcome to DataLand"/>
  <ApiClient/>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import WelcomeDataland from './components/WelcomeDataland.vue';
import ApiClient from './components/APIClient.vue';

@Options({
  components: {
    WelcomeDataland,
    ApiClient
  },
})
export default class App extends Vue {}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
