import PrimeVue from "primevue/config";

const dummyPromise: Promise<any> = Promise.resolve({
  objectDescription:
    "Dummy object which represents the acutal object which is returned if the keycloak promise" + "is resolved.",
});

export function getInjectedKeycloakObjectsForTest(): any {
  return {
    getKeycloakPromise() {
      return dummyPromise;
    },
    keycloak_init: dummyPromise,
  };
}

export function getRequiredPlugins(): any[] {
  return [PrimeVue];
}
