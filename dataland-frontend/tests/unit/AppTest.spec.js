import {mount} from '@vue/test-utils'
import App from '@/App.vue'

describe('Mounted App', () => {
    const wrapper = mount(App);

    it('does a wrapper exist', () => {
        expect(wrapper.exists()).toBe(true)
    })
})