import EuTaxoSearchBar from '@/components/ui/EuTaxoSearchBar.vue'
import {shallowMount} from "@vue/test-utils"

describe('EuTaxoSearchBarTest', () => {

    const wrapper = shallowMount(EuTaxoSearchBar)

    it('checks field properties', () => {
        expect(wrapper.vm.model).toBeDefined()
        expect(wrapper.vm.response).toBeDefined()
        expect(wrapper.vm.processed).toBeDefined()
    })

    it('checks getCompanyByName()', async () => {
        expect.assertions(2)
        jest.spyOn(console, 'error');
        wrapper.setData({model: {"something": "none"}})
        expect(wrapper.vm.getCompanyByName()).toBeDefined()
        await wrapper.vm.getCompanyByName();
        expect(console.error).toHaveBeenCalled();
    });

})
