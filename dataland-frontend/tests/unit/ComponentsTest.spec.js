import {shallowMount} from '@vue/test-utils'
import HelloWorld from '@/components/HelloWorld.vue'
import APIClient from '@/components/APIClient.vue'
import ESG from '@/components/ESG.vue'

describe('HelloWorld.vue', () => {
    it('renders props.msg when passed', () => {
        const msg = 'new message'
        const wrapper = shallowMount(HelloWorld, {
            props: {msg}
        })
        expect(wrapper.text()).toMatch(msg)
    })
})
describe('ESG.vue', () => {
    it('renders props.msg when passed', () => {
        const wrapper = shallowMount(ESG)
        expect(wrapper.find('button').exists()).toBe(true)
    })
})