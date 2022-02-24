import {mount, shallowMount} from '@vue/test-utils'
import HelloWorld from '@/components/HelloWorld.vue'
import APIClient from '@/components/APIClient.vue'
import ESG from '@/components/ESG.vue'
import {DataStore} from "@/service/DataStore";

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
    it('checks if there exist button in the component', () => {
        const wrapper = shallowMount(ESG)
        expect(wrapper.find('button').exists()).toBe(true)
    })
})

describe('APIClient.vue', () => {

    it('checks if APIClient.data() and all its dataStore not undefined', () => {
        expect(typeof APIClient.data()).toBeDefined()
    })

    it('checks code in the script', () => {
        const wrapper = mount(APIClient, {
            data() {
                return {
                    dataStore: new DataStore("http://localhost:8080"),
                    get_id: "dummy_id",
                    get_name: "dummy_name",
                    getResult: null,
                    getResultByID: null,
                    getResultByName: null
                }
            }
        })

        expect(wrapper.vm.get_id).toMatch("dummy_id")
        expect(wrapper.vm.get_name).toMatch("dummy_name")

        wrapper.vm.clearGetOutput()

        expect(wrapper.vm.get_id).toBeNull()
        expect(wrapper.vm.get_name).toBeNull()

    })
})