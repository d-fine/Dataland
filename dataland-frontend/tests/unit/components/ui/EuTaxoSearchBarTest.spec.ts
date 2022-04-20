import EuTaxoSearchBar from '@/components/ui/EuTaxoSearchBar.vue'
import {mount} from "@vue/test-utils"
import { createRouter, createMemoryHistory } from "vue-router"
import {routes} from '@/router'

describe("EuTaxoSearchBarTest", () => {
    it("renders a child component via routing", async () => {
        const router = createRouter({
            history: createMemoryHistory(),
            routes
        })
        router.push("/searchtaxonomy")
        await router.isReady()
        const wrapper = mount(EuTaxoSearchBar, {
            global: {
                plugins: [router]
            }
        })
        expect(wrapper.vm.model).toBeDefined()
        expect(wrapper.vm.response).toBeDefined()
        expect(wrapper.vm.processed).toBeDefined()
        async () => {
            jest.spyOn(console, 'error');
            expect(wrapper.vm.searchCompany()).toBeDefined()
            await wrapper.vm.searchCompany();
            expect(console.error).toHaveBeenCalled();
        };
    })
})

