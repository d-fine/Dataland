import {shallowMount} from '@vue/test-utils'
import HelloWorld from '@/components/HelloWorld.vue'
import APIClient from '@/components/APIClient.vue'
import ESG from '@/components/ESG.vue'

describe('HelloWorld.vue', () => {
  const components = [HelloWorld, APIClient, ESG]
  for (let idx = 0; idx < components.length; idx++) {
    it('renders props.msg when passed', () => {
      const msg = 'new message'
      const wrapper = shallowMount(components[idx], {
        props: {msg}
      })
      expect(wrapper.text()).toMatch(msg)
    })
  }
})
