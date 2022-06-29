import {shallowMount} from '@vue/test-utils'
import WelcomeDataland from '@/components/pages/WelcomeDataland.vue'
import { expect } from '@jest/globals';

describe('WelcomeDatalandTest', () => {
    it('checks if it is defined', () => {
        const wrapper = shallowMount(WelcomeDataland)
        expect(wrapper.text()).toBeDefined()
    })
})