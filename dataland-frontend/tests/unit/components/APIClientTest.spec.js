import APIClient from '@/components/APIClient.vue'
import {mount} from "@vue/test-utils";

describe('APIClientTest', () => {

    const wrapper = mount(APIClient)

    it('checks field properties', () => {
        expect(wrapper.vm.data).toBeDefined()
        expect(wrapper.vm.dataStore).toBeDefined()
        expect(wrapper.vm.loading).toBeDefined()
    })

    it('checks getSkyminderByName()', async () => {
        expect(wrapper.vm.loading).toBe(false)
        expect(wrapper.vm.getSkyminderByName()).toBeDefined()
        expect(wrapper.vm.loading).toBe(true)
        await new Promise(timerHandler => setTimeout(timerHandler, 4000)); /* sleep for 4s*/
        expect(wrapper.vm.loading).toBe(false)
    })

    it('checks clearGetOutput()', () => {
        expect(wrapper.vm.clearGetOutput()).toBeDefined()
    })

})
