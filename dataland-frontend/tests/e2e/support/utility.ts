export function doThingsInChunks<T>(dataArray: Array<T>, chunkSize: number, processor: (element: T)=>void): Promise<any> {
    let promise: Promise<any> = Promise.resolve()
    for (let i = 0; i < dataArray.length; i += chunkSize) {
        const chunk = dataArray.slice(i, i + chunkSize);
        promise = promise.then(
            () => Promise.all(chunk.map(element =>
                processor(element)
            ))
        )
    }
    return promise
}