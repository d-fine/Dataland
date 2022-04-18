export class ApiWrapper {
    private readonly axiosFunction: any

    constructor(axiosFunction: any) {
        this.axiosFunction = axiosFunction
    }

    perform(...args: any): any {
        return this.axiosFunction(...args, {baseURL: process.env.VUE_APP_BASE_API_URL})
    }
}
