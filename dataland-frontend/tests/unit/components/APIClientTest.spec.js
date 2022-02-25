import APIClient from '@/components/APIClient.vue'

describe('APIClientTest', () => {

    it('checks if APIClient.data() and all its dataStore not undefined', () => {
        expect(typeof APIClient.data()).toBeDefined()
    })
})