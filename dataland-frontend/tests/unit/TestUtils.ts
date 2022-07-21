const a: Promise<any> = new Promise(function (resolve) {
  resolve({ object: "dummyobject" });
});

export function getInjectedKeycloakObjectsForTest(): any {
  return {
    getKeycloakInitPromise() {
      return a;
    },
    keycloak_init: a,
  };
}
