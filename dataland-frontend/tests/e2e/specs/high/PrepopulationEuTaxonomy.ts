describe('TPopulate with EU Taxonomy data', () => {
    let companyIdList:Array<string>
    let eutaxonomiesData:any

    async function uploadData(dataArray:Array<object>, endpoint:string){
        const start = Date.now()
        const chunkSize = 8;
        for (let i = 0; i < dataArray.length; i += chunkSize) {
            const chunk = dataArray.slice(i, i + chunkSize);
            await Promise.all(chunk.map(async (element:object) => {
                    await fetch(`${Cypress.env("API")}/${endpoint}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(element)
                    }).then(response => {
                        console.log(response.json())
                        assert(response.status.toString() === "200" )
                    })
                })
            )
        }
        const millis = Date.now() - start
        console.log(`seconds elapsed = ${Math.floor(millis / 1000)}`)
    }

    before(function(){
        cy.fixture('CompanyAssociatedEuTaxonomyData').then(function(eutaxonomies){
            eutaxonomiesData=eutaxonomies
            cy.request('GET', `${Cypress.env("API")}/companies`).then((response) => {
                console.log("response.body",response.body)
                companyIdList = response.body.map((e: any, index:number) => {
                    eutaxonomiesData[index].companyId = e.companyId
                    console.log(e.companyId)
                    return e.companyId
                })
            })
        });
    });

    it.only('Populate EU Taxonomy Data',  async() => {
        console.log("Company ID list",companyIdList)
        console.log("EU TaxoData", eutaxonomiesData)

        await uploadData(eutaxonomiesData, "data/eutaxonomies")
    });
})