export function nFormatter(num:number , digits:number) {
    const lookup = [
        { value: 1, symbol: "" },
        { value: 1e3, symbol: "k" },
        { value: 1e6, symbol: "m" },
        { value: 1e9, symbol: "b" },
        { value: 1e12, symbol: "t" },
        { value: 1e15, symbol: "qa" },
        { value: 1e18, symbol: "qi" }
    ];
    const rx = /\.0+$|(\.[0-9]*[1-9])0+$/;
    const item = lookup.slice().reverse().find(function(item) {
        return num >= item.value;
    });
    return item ? (num / item.value).toFixed(digits).replace(rx, "$1") + " " + item.symbol : "0";
}