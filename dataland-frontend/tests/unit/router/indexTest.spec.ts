import {routes} from '@/router'
import {shallowMount, mount} from "@vue/test-utils"
import App from '@/App.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { expect } from '@jest/globals';

describe('routerTest', () => {
    it('checks if the router is mounted', () => {
        const wrapper = shallowMount(routes)
        expect(wrapper.text()).toBeDefined()
    });
    it('home', async () => {
        const router = createRouter({
            history: createWebHistory(),
            routes: routes,
        })
        await router.push('/')
        await router.isReady()
        const wrapper = mount(App, {
            global: {
                plugins: [router]
            }
        })
        expect(wrapper.html()).toContain('COME TOGETHER TO CREATE A DATASET THAT NOBODY CAN CREATE ALONE WHILE SHARING THE COSTS')
    });

    it('searchtaxonomy', async () => {
        const router = createRouter({
            history: createWebHistory(),
            routes: routes,
        })
        await router.push('/searchtaxonomy')
        await router.isReady()
        const wrapper = mount(App, {
            global: {
                plugins: [router]
            }
        })
        expect(wrapper.html()).toContain('Search EU Taxonomy data')
    });

})
