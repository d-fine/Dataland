import axios from "axios"

export class DataStore {
    constructor(baseUrl) {
        this.axios = axios.create({
            baseURL: baseUrl,
            timeout: 2000, /* timeout in milliseconds*/
            headers: {
                "Content-type": "application/json"
            }
        })
    }


    async getByName(code, name) {
        try {
            const results = await this.axios.get(`/data/skyminder/${code}/${name}`)
            const data = results.data
            if (data.length){
                return data
            }
        } catch (err) {
            console.error(err)
        }
        return null
    }


}

