import CreateCompany from '@/components/forms/CreateCompany.vue'
import {shallowMount} from "@vue/test-utils"

describe('CreateCompanyTest', () => {

    const wrapper = shallowMount(CreateCompany)

    it('checks field properties', () => {
        expect(wrapper.vm.model).toBeDefined()
        expect(wrapper.vm.schema).toBeDefined()
        expect(wrapper.vm.processed).toBeDefined()
        expect(wrapper.vm.response).toBeDefined()
        expect(wrapper.vm.messageCount).toBeDefined()
    })

    it('checks postCompanyData()', async () => {
        expect.assertions(2)
        jest.spyOn(console, 'error');
        wrapper.setData({model: {"something": "none"}})
        expect(wrapper.vm.postCompanyData()).toBeDefined()
        await wrapper.vm.postCompanyData();
        expect(console.error).toHaveBeenCalled();
    });

})
