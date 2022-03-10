import APIClient from '@/components/APIClient.vue'
import {mount} from "@vue/test-utils"

jest.setTimeout(10000) /* new timeout in milliseconds due to long tests */

describe('APIClientTest', () => {

    const wrapper = mount(APIClient)

    it('checks field properties', () => {
        expect(wrapper.vm.response).toBeDefined()
        expect(wrapper.vm.loading).toBeDefined()
    })

    it('checks getSkyminderByName()', async () => {
        expect(wrapper.vm.loading).toBe(false)
        wrapper.vm.companyName="BMW"
        wrapper.vm.countryCode="DEU"
        expect(wrapper.vm.getSkyminderByName()).toBeDefined()
        expect(wrapper.vm.loading).toBe(true)
        await new Promise(timerHandler => setTimeout(timerHandler, 9000)) /* sleep for 9s*/
        expect(wrapper.vm.loading).toBe(false)
    })

    it('checks clearGetOutput()', () => {
        expect(wrapper.vm.clearGetOutput()).toBeDefined()
    })

})
