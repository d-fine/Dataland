import RetrieveSkyminder from '@/components/forms/RetrieveSkyminder.vue'
import {shallowMount} from "@vue/test-utils"

describe('CreateCompanyTest', () => {

    const wrapper = shallowMount(RetrieveSkyminder)

    it('checks field properties', () => {
        expect(wrapper.vm.data).toBeDefined()
        expect(wrapper.vm.schema).toBeDefined()
    })

    it('checks postCompanyData()', async () => {
        expect(wrapper.vm.getSkyminderByName()).toBeDefined()
    })

})
