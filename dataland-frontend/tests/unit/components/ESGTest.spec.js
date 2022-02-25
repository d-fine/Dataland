import {shallowMount} from '@vue/test-utils'
import ESG from '@/components/ESG.vue'

describe('ESGTest', () => {
    it('checks if there exist button in the component', () => {
        const wrapper = shallowMount(ESG)
        expect(wrapper.find('button').exists()).toBe(true)
    })
})