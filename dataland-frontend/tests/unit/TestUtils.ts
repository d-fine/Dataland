const a: Promise<any> = Promise.resolve({
  objectName: "dummyobject",
});

export function getInjectedKeycloakObjectsForTest(): any {
  return {
    getKeycloakInitPromise() {
      return a;
    },
    keycloak_init: a,
  };
}
