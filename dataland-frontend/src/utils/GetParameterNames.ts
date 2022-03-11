export function getParams(func: any): any {
    const getAllParams = require('get-parameter-names')
    const params = getAllParams(func)
    for (const index in params) {
        const value = params[index];
        if ("options" == value) {
            params.splice(index, 1)
        }
    }
    return params
}