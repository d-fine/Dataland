<template>
  <div @click="toggle" class="max-w-full max-h-full flex justify-content-center">
    <img ref="profile-picture" class="d-profile-picture" src="@/assets/images/logos/favicon-32x32.png"/>
    <img src="@/assets/images/elements/triangle_down.svg" class="d-triangle-down"/>
  </div>
  <Menu ref="menu" :model="items" :popup="true" style="transform:translate(0px,1rem);" class="text-primary">
    <template #item="{item}">
      <a class="p-menuitem-link" role="menuitem" tabindex="0" @click="item.command()"><span class="p-menuitem-icon text-primary" :class="item.icon"></span><span class="p-menuitem-text text-primary">{{item.label}}</span></a>
    </template>
  </Menu>
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

  .d-triangle-down {
    height: 0.5rem;
    margin-top: 1rem;
    margin-left: 0.5rem;
  }
</style>