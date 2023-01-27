import { routes } from "@/router";
import { shallowMount, mount, VueWrapper } from "@vue/test-utils";
import App from "@/App.vue";
import { createRouter, createWebHistory, Router } from "vue-router";

/**
 * Mounts the main vue component with the specified router
 *
 * @param routerToBeUsedWithMount the router to be used
 * @returns the mounted component
 */
function mountAppWithRouter(routerToBeUsedWithMount: Router): VueWrapper {
  return mount(App, {
    global: {
      plugins: [routerToBeUsedWithMount],
    },
  });
}

describe("routerTest", () => {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  });

  it("checks if the router is mounted", () => {
    const routesWrapper = shallowMount(routes);
    expect(routesWrapper.text()).to.exist;
  });

  it("checks if the router pushes to Welcome page as expected", async () => {
    await router.push("/");
    await router.isReady();
    expect(mountAppWithRouter(router).html()).to.contain("THE ALTERNATIVE TO DATA MONOPOLIES");
  });
});
