function createChart() {
    // create the chart
    this.priceArr = []; //reinit the array, otherwise it will contain prevdata
    if (Number(this.searchData['price']['d']) < 0) {
        this.chartColor = '#FF0000'
    } else {
        this.chartColor = '#00FF00'
    }
    this.chartDate = this.searchData['history']['t'];
    this.chartPrice = this.searchData['history']['c'];
    for (let i = 0; i < this.chartDate.length; i++) {
        this.priceArr.push([this.chartDate[i] * 1000, this.chartPrice[i]]);
    }
    Highcharts.stockChart('chart_container', {
        plotOptions: {
            series: {
                color: this.chartColor
            }
        },
        // disable zoom
        rangeSelector: {
            buttons: [],
            inputEnabled: false
        },
        // disable sliding window
        navigator: {
            enabled: false
        },
        title: {
            text: this.searchData['company']['ticker'] + ' Hourly Price Variation'
        },
        // use local time
        time: {
            useUTC: false
        },
        series: [{
            type: "line",
            name: this.searchData['company']['ticker'],
            data: this.priceArr
        }]
    })
}

function getData() {
    searchData = JSON.parse(Android.getData());
}
var searchData = {};
getData();
createChart();