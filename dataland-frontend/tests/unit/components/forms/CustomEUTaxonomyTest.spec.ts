import CustomEUTaxonomy from '@/components/forms/CustomEUTaxonomy.vue'
import {shallowMount} from "@vue/test-utils"

describe('CreateCompanyTest', () => {

    const wrapper = shallowMount(CustomEUTaxonomy)

    it('checks field properties', () => {
        expect(wrapper.vm.data).toBeDefined()
    })

    it('checks postCompanyData()', async () => {
        expect(wrapper.vm.postEUData()).toBeDefined()
    })

})
