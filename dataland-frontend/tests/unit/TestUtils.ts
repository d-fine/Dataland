const dummyPromise: Promise<any> = Promise.resolve({
  objectDescription: "Dummy object which represents the acutal object which is returned if the keycloak promise" +
      "is resolved.",
});

export function getInjectedKeycloakObjectsForTest(): any {
  return {
    getKeycloakInitPromise() {
      return dummyPromise;
    },
    keycloak_init: dummyPromise
  };
}
