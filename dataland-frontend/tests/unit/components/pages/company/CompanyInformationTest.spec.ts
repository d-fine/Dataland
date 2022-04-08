import CompanyInformation from '@/components/pages/company/CompanyInformation.vue'
import {shallowMount} from "@vue/test-utils"

describe('CompanyInformation', () => {

    const wrapper = shallowMount(CompanyInformation)

    it('checks field properties', () => {
        expect(wrapper.vm.company).toBeDefined()
        expect(wrapper.vm.response).toBeDefined()
        expect(wrapper.vm.companyInformation).toBeDefined()
    })

    it('checks getCompanyInformation()', async () => {
        expect.assertions(2)
        jest.spyOn(console, 'error');
        wrapper.setData({model: {"something": "none"}})
        expect(wrapper.vm.getCompanyInformation()).toBeDefined()
        await wrapper.vm.getCompanyInformation();
        expect(console.error).toHaveBeenCalled();
    });

})
