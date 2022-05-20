function createChart() {
    // create the chart
    this.volumeArr = [];
    this.priceArr = [];
    this.candleStickArr = [];
    this.chartDate = this.searchData['history-charts']['t'];
    this.chartPrice = this.searchData['history-charts']['c'];
    for (let i = 0; i < this.chartDate.length; i++) {
        this.candleStickArr.push([this.chartDate[i] * 1000, this.searchData['history-charts']['o'][i],
            this.searchData['history-charts']['h'][i],
            this.searchData['history-charts']['l'][i],
            this.chartPrice[i]
        ]);
        this.volumeArr.push([this.chartDate[i] * 1000, this.searchData['history-charts']['v'][i]])
        this.priceArr.push([this.chartDate[i] * 1000, this.chartPrice[i]]);
    }
    Highcharts.stockChart('chart_container', {
        rangeSelector: {
            selected: 2
        },

        title: {
            text: this.searchData['company']['ticker'] + ' Historical'
        },

        subtitle: {
            text: 'With SMA and Volume by Price technical indicators'
        },

        yAxis: [{
            startOnTick: false,
            endOnTick: false,
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'OHLC'
            },
            height: '60%',
            lineWidth: 2,
            resize: {
                enabled: true
            }
        }, {
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'Volume'
            },
            top: '65%',
            height: '35%',
            offset: 0,
            lineWidth: 2
        }],

        tooltip: {
            split: true
        },

        series: [{
            type: 'candlestick',
            name: this.searchData['company']['ticker'],
            id: 'candlestick',
            zIndex: 2,
            data: this.candleStickArr
        }, {
            type: 'column',
            name: 'Volume',
            id: 'volume',
            data: this.volumeArr,
            yAxis: 1
        }, {
            type: 'vbp',
            linkedTo: 'candlestick',
            params: {
                volumeSeriesID: 'volume'
            },
            dataLabels: {
                enabled: false
            },
            zoneLines: {
                enabled: false
            }
        }, {
            type: 'sma',
            linkedTo: 'candlestick',
            zIndex: 1,
            marker: {
                enabled: false
            }
        }]
    })
}

function getData() {
    searchData = JSON.parse(Android.getData());
}
var searchData = {};
getData();
createChart();