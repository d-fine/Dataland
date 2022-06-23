import RetrieveSkyminder from '@/components/forms/RetrieveSkyminder.vue'
import {shallowMount} from "@vue/test-utils"
import { expect } from '@jest/globals';

describe('RetrieveSkyminderTest', () => {

    const wrapper = shallowMount(RetrieveSkyminder)

    it('checks field properties', () => {
        expect(wrapper.vm.model).toBeDefined()
    })

    it('checks postCompanyData()', async () => {
        expect(wrapper.vm.getSkyminderByName()).toBeDefined()
    })

})
