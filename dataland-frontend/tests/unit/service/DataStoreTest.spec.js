import axios from "axios"
import MockAdapter from "axios-mock-adapter"

import {DataStore} from "@/service/DataStore.js"

const BASE_URL = "https://dummyhost:dummyport"

describe("DataStoreTest", () => {
  let mock
  const data = [
    {id: 0, name: "John"},
    {id: 1, name: "Andrew"},
  ]
  const code = "DEU"
  const name = "BMW"

  beforeAll(() => {
    mock = new MockAdapter(axios)
  })

  afterEach(() => {
    mock.reset()
  })

  it("should return data if everything is fine", async () => {

    mock.onGet(`${BASE_URL}/data/skyminder/${code}/${name}`).reply(200, data)

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getByName(code, name)

    expect(receivedData).toEqual(data)
  })

  it("should return null when no data is available", async () => {
    mock.onGet(`${BASE_URL}/data/skyminder/${code}/${name}`).reply(200, [])

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getByName(code, name)

    expect(receivedData).toBeNull()
  })

  it("should return null in case of a network error", async () => {
    mock.onGet(`${BASE_URL}/data/skyminder/${code}/${name}`).networkErrorOnce()

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getByName(code, name)

    expect(receivedData).toBeNull()
  })

  it("should return null in case of a wrong address", async () => {
    mock.onGet("https://newdummyhost:newdummyport/data")

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getByName(code, name)

    expect(receivedData).toBeNull()
  })

  it("should post successfully if everything is fine", async () => {
    mock.onPost(`${BASE_URL}/path/`).reply(200)

    const dataStore = new DataStore(BASE_URL)
    const success = await dataStore.postJson(data)

    expect(success).toBeTruthy()
  })

  it("should not be able to post successfully in case of a network error", async () => {
    mock.onPost(`${BASE_URL}/path/`).networkErrorOnce()

    const dataStore = new DataStore(BASE_URL)
    const success = await dataStore.postJson(data)

    expect(success).toBeFalsy()
  })
})
