import axios from "axios"

/**
 * Class for data store
 */
export class DataStore {
    /**
     * Creates an axios instance
     * @param baseUrl identifies the base url
     */
    constructor(baseUrl) {
        this.axios = axios.create({
            baseURL: baseUrl,
            headers: {
                "Content-type": "application/json"
            }
        })
    }

    /**
     * Gets data contact information from the server
     * @param countryCode identifies the three-letter country code
     * @param companyName identifies the company name
     */
    async getByName(countryCode, companyName) {
        try {
            const results = await this.axios.get(
                `/data/skyminder/${countryCode}/${companyName}`
            )
            const data = results.data
            if (data.length) {
                return data
            }
        } catch (err) {
            console.error(err)
        }
        return null
    }

}

