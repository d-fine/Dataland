import EuTaxoSearchBar from '@/components/resources/taxonomy/search/EuTaxoSearchBar.vue'
import {mount} from "@vue/test-utils"
import { createRouter, createMemoryHistory } from "vue-router"
import {routes} from '@/router'
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
            expect(wrapper.vm.showIndexTabs).toBeDefined()
            expect(wrapper.vm.index).toBeDefined()
            expect(wrapper.vm.scrolled).toBeDefined()
            expect(wrapper.vm.responseArray).toBeDefined()
            expect(wrapper.vm.collection).toBeDefined()
            expect(wrapper.vm.autocompleteArray).toBeDefined()
            expect(wrapper.vm.loading).toBeDefined()
            expect(wrapper.vm.selectedCompany).toBeDefined()
            expect(wrapper.vm.filteredCompaniesBasic).toBeDefined()
            expect(wrapper.vm.route).toBeDefined()
        })

        it('checks getCompanyByName()', async () => {
            jest.spyOn(console, 'error');
            expect(wrapper.vm.searchCompany()).toBeDefined()
            await wrapper.vm.searchCompany();
            expect(console.error).toHaveBeenCalled();
        });
    });

