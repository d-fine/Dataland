import EuTaxoSearchBar from '@/components/resources/taxonomy/search/EuTaxoSearchBar.vue'
import {mount} from "@vue/test-utils"
import { createRouter, createMemoryHistory } from "vue-router"
import {routes} from '@/router'
import { expect } from '@jest/globals';

describe("EuTaxoSearchBarTest", () => {
    let wrapper:any
        it('checks field properties', async () => {
            const router = createRouter({
                history: createMemoryHistory(),
                routes
            })
            router.push("/searchtaxonomy")
            await router.isReady()
            wrapper = mount(EuTaxoSearchBar, {
                global: {
                    plugins: [router]
                }
            })
        })

        it('checks field properties', () => {
            expect(wrapper.vm.autocompleteArray).toBeDefined()
            expect(wrapper.vm.autocompleteArrayDisplayed).toBeDefined()
            expect(wrapper.vm.loading).toBeDefined()
            expect(wrapper.vm.modelValue).toBeDefined()
            expect(wrapper.vm.route).toBeDefined()
        })

        it('checks getCompanyByName()', async () => {
            jest.spyOn(console, 'error');
            expect(wrapper.vm.searchCompanyName()).toBeDefined()
            await wrapper.vm.searchCompanyName();
            expect(console.error).toHaveBeenCalled();
        });
    });
