import router from '@/router/index'
import {shallowMount} from "@vue/test-utils"

describe('routerTest', () => {

    const wrapper = shallowMount(router)

    it('checks if the router is mounted', () => {
        expect(wrapper.text()).toBeDefined()
    })

})
