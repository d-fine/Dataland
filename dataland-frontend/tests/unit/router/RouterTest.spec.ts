import { routes } from "@/router";
import { shallowMount, mount } from "@vue/test-utils";
import App from "@/App.vue";
import { createRouter, createWebHistory, Router } from "vue-router";
import { expect } from "@jest/globals";

function mountAppWithRouter(routerToBeUsedWithMount: Router) {
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
    expect(routesWrapper.text()).toBeDefined();
  });

  it("checks if the router pushes to Welcome page as expected", async () => {
    await router.push("/");
    await router.isReady();
    expect(mountAppWithRouter(router).html()).toContain(
      "COME TOGETHER TO CREATE A DATASET THAT NOBODY CAN CREATE ALONE WHILE SHARING THE COSTS"
    );
  });

  it("checks if the router pushes to Searchtaxonomy page as expected", async () => {
    await router.push("/searchtaxonomy");
    await router.isReady();
    expect(mountAppWithRouter(router).html()).toContain("Search EU Taxonomy data");
  });
});
