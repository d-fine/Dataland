import {shallowMount} from '@vue/test-utils'
import WelcomeDataland from '@/components/WelcomeDataland.vue'

describe('WelcomeDatalandTest', () => {
    it('renders props.msg when passed', () => {
        const msg = 'new message'
        const wrapper = shallowMount(WelcomeDataland, {
            props: {msg}
        })
        expect(wrapper.text()).toMatch(msg)
    })
})