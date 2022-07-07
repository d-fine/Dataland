<template>
  <div @click="toggle" class="max-w-full max-h-full flex justify-content-center">
    <img ref="profile-picture" class="d-profile-picture" src="@/assets/images/logos/favicon-32x32.png">
    <i class="pi pi-caret-down pt-2 ml-2" style="color:white;"></i>
  </div>
  <Menu ref="menu" :model="items" :popup="true" />
</template>

<script>
import Menu from 'primevue/menu';
export default {
  name: "UserProfileDropDown",
  inject: ['authenticated', 'getKeycloakInitPromise'],
  components: {Menu},
  methods: {
    toggle(event) {
      this.$refs.menu.toggle(event);
    }
  },
  created() {
    this.getKeycloakInitPromise().then((keycloak) => {
      if (keycloak.authenticated && keycloak.idTokenParsed.picture) {
        this.$refs["profile-picture"].src = keycloak.idTokenParsed.picture;
      }
    }).
    catch((error) => console.log("error: " + error));
  },
  data() {
    return {
      items: [
        {
          label: 'Logout',
          icon: 'pi pi-sign-out',
          command: () => {
            this.getKeycloakInitPromise().then((keycloak) => {
              if (keycloak.authenticated) {
                keycloak.logout()
              }
            }).catch((error) => console.log("error: " + error))
          }
        }
      ]
    }
  }
}
</script>

<style scoped>
  .d-profile-picture {
    border-radius: 50%;
    height: 2rem;
    /* TODO: Make the profile picture adjust size to fit parent automatically */
  }
</style>