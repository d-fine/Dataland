/**
 * Class to call api functions with axios input option
 */
export class ApiWrapper {
    private readonly axiosFunction: any

    constructor(axiosFunction: any) {
        this.axiosFunction = axiosFunction
    }

    /**
     * call the function with axios option
     *
     * @param  {any} args      the input arguments of the api function
     */
    perform(...args: any): any {
        return this.axiosFunction(...args, {baseURL: process.env.VUE_APP_BASE_API_URL})
    }
}
