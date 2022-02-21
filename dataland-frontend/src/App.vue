<template>
  <section class="hero container max-w-screen-lg mx-auto pb-10 flex">

    <img alt="Vue logo" src="./assets/dataland-logo-lr.png" class="mx-auto" >
  </section>
  <HelloWorld msg="Welcome to DataLand"/>
  <ESG />
</template>

<script>
import HelloWorld from './components/HelloWorld.vue'
import ESG from './components/ESG.vue'

export default {
  name: 'App',
  components: {
    HelloWorld,
    ESG
  }
}
</script>

<style>
/*@import 'https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css';*/
/*@import 'https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css';*/
/*@import 'https://cdnjs.cloudflare.com/ajax/libs/bulma/0.9.3/css/bulma.min.css';*/
@import 'https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css';
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

</style>
