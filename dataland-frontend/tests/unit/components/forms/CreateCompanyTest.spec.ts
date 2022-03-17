import CreateCompany from '@/components/forms/CreateCompany.vue'
import {shallowMount} from "@vue/test-utils"

describe('CreateCompanyTest', () => {

    const wrapper = shallowMount(CreateCompany)

    it('checks field properties', () => {
        expect(wrapper.vm.data).toBeDefined()
        expect(wrapper.vm.schema).toBeDefined()
    })

    it('checks postCompanyData()', async () => {
        expect(wrapper.vm.postCompanyData()).toBeDefined()
    })

})
