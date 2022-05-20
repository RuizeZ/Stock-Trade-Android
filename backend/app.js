const express = require('express');
const finnhub = require('finnhub');
const path = require('path');
const axios = require('axios');
const api_key = finnhub.ApiClient.instance.authentications['api_key'];
api_key.apiKey = "c823s8qad3i9d12p6hqg";
const finnhubClient = new finnhub.DefaultApi();
const app = express();
const port = process.env.PORT || 8080;

var total_data = {};

function getCompany(companyName) {
  return new Promise((resolve, reject) => {
    console.log('start');
    finnhubClient.companyProfile2({
      'symbol': companyName
    }, (error, data, response) => {
      total_data['company'] = data;
      resolve();
      console.log('finish');
    });
  })
}
// Company’s Description 
function getInfo(companyName, mode) {
  // Company’s Historical Data
  // get start date and end date
  console.log("getInfo for ", companyName)
  var date = new Date();
  var endDate = Math.round(date.getTime() / 1000);
  date.setHours(date.getHours() - 6);
  var startDate = Math.round(date.getTime() / 1000);
  Promise2 = new Promise((resolve, reject) => {
    finnhubClient.stockCandles(companyName, 5, startDate, endDate, (error, data, response) => {
      total_data['history'] = data;
      resolve();
    });
  })
  // Company's Latest Price of Stock
  Promise3 = new Promise((resolve, reject) => {
    var url = 'https://finnhub.io/api/v1/quote?symbol=' + companyName + '&token=' + api_key.apiKey;
    axios.get(url).then(function (res) {
      total_data['price'] = res.data;
      resolve();
    })
  })
  if (mode === 'getNew') {
    console.log('getting new data');
    Promise1 = new Promise((resolve, reject) => {
      finnhubClient.companyPeers(companyName, (error, data, response) => {
        total_data['peers'] = data;
        resolve();
      });
    })
    // Company’s Historical Data in Charts tab
    // get start date and end date
    var date = new Date();
    var endDate = Math.round(date.getTime() / 1000);
    date.setFullYear(date.getFullYear() - 2);
    var startDate = Math.round(date.getTime() / 1000);
    Promise5 = new Promise((resolve, reject) => {
      finnhubClient.stockCandles(companyName, 'D', startDate, endDate, (error, data, response) => {
        total_data['history-charts'] = data;
        resolve();
      });
    })
    // Company’s News
    var newsDate = new Date();
    var newsStartDate = String(newsDate.getFullYear()) + '-' + String(newsDate.getMonth() + 1).padStart(2, '0') + '-' + String(newsDate.getDate() - 10).padStart(2, '0');
    var newsEndDate = String(newsDate.getFullYear()) + '-' + String(newsDate.getMonth() + 1).padStart(2, '0') + '-' + String(newsDate.getDate()).padStart(2, '0');
    Promise4 = new Promise((resolve, reject) => {
      finnhubClient.companyNews(companyName, newsStartDate, newsEndDate, (error, data, response) => {
        total_data['news'] = data;
        resolve();
      });
    })

    // Company’s Social Sentiment
    Promise6 = new Promise((resolve, reject) => {
      var url = 'https://finnhub.io/api/v1/stock/social-sentiment?symbol=' + companyName + '&from=2022-01-01&token=' + api_key.apiKey;
      axios.get(url).then(function (res) {
        total_data['sentiment'] = res.data;
        resolve();
      })
    })

    // company's Earnings
    Promise7 = new Promise((resolve, reject) => {
      finnhubClient.companyEarnings(companyName, {
        'limit': ''
      }, (error, data, response) => {
        total_data['earnings'] = data;
        resolve();
      });
    })

    // company's Recommendation Trends
    Promise8 = new Promise((resolve, reject) => {
      finnhubClient.recommendationTrends(companyName, (error, data, response) => {
        total_data['recommendation'] = data;
        resolve();
      });
    })
    return Promise.all([Promise1, Promise2, Promise3, Promise4, Promise5, Promise6, Promise7, Promise8]);
  }
  return Promise.all([Promise2, Promise3]);
}

function getNewHistory(companyName) {
  // Company’s Historical Data
  // get start date and end date
  var endDate = total_data['price']['t'];
  var date = new Date(endDate * 1000);
  date.setHours(date.getHours() - 6);
  var startDate = Math.round(date.getTime() / 1000);
  return new Promise((resolve, reject) => {
    finnhubClient.stockCandles(companyName, 5, startDate, endDate, (error, data, response) => {
      total_data['history'] = data;
      resolve();
    });
  })
}

// Company’s Description 
function getUpdateInfo(companyName, dataJSON) {
  // Company’s Historical Data
  // get start date and end date
  console.log("getUpdateInfo for ", companyName)
  var date = new Date();
  var endDate = Math.round(date.getTime() / 1000);
  date.setHours(date.getHours() - 6);
  var startDate = Math.round(date.getTime() / 1000);
  Promise2 = new Promise((resolve, reject) => {
    finnhubClient.stockCandles(companyName, 5, startDate, endDate, (error, data, response) => {
      dataJSON['history'] = data;
      resolve();
    });
  })
  // Company's Latest Price of Stock
  Promise3 = new Promise((resolve, reject) => {
    var url = 'https://finnhub.io/api/v1/quote?symbol=' + companyName + '&token=' + api_key.apiKey;
    axios.get(url).then(function (res) {
      dataJSON['price'] = res.data;
      resolve();
    })
  })
  return Promise.all([Promise2, Promise3]);
}

app.get('/search/auto/:name', (req, res) => {
  console.log(req.params['name']);
  companyName = req.params['name'].toUpperCase();
  finnhubClient.symbolSearch(companyName, (error, data, response) => {
    console.log("auto success");
    res.json(data);
  });
})

app.get('/search/company/:name', async (req, res) => {
  total_data = {};
  companyName = req.params['name'].toUpperCase();
  console.log("Getting company data: ", companyName);
  await getCompany(companyName);
  // change total_data['company'] to string and compare to '{}'
  if (JSON.stringify(total_data['company']) === '{}') {
    console.log("companyName Error");
    res.json(total_data);
  } else {
    console.log("Getting other info");
    await getInfo(companyName, 'getNew');
    // check current time to see if market is closed
    var date = new Date();
    while (true) {
      try {
        if ((date.getUTCHours() === 13 && date.getUTCMinutes() < 30) || date.getUTCHours() < 13 || date.getUTCHours() >= 20 || JSON.stringify(total_data['history']['s']) === '"no_data"') {
          console.log("Getting new news");
          await getNewHistory(companyName);
        }
        console.log("success");
        break;
      } catch {
        console.log("error");
        console.log("Getting new news again");
        await getNewHistory(companyName);
      }
    }
    res.json(total_data);
  }
})

app.get('/search/update/:name', async (req, res) => {
  companyName = req.params['name'].toUpperCase();
  console.log("updating company data: ", companyName);
  var dataJSON = {};
  // dataJSON = await getUpdateInfo(companyName,dataJSON);
  await getUpdateInfo(companyName,dataJSON)
  console.log("success");
  res.json(dataJSON);
})

app.use(express.static(path.join(__dirname + '/dist/paper-trade')));
app.listen(port, () => console.log(
  `Express started on http://localhost:${port}; ` +
  `press Ctrl-C to terminate.`))
