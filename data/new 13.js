{
	"orderCreateTransaction": {
		"type": "MARKET_ORDER",
		"instrument": "GBP_USD",
		"units": "357",
		"timeInForce": "FOK",
		"positionFill": "REDUCE_ONLY",
		"reason": "TRADE_CLOSE",
		"tradeClose": {
			"units": "ALL",
			"tradeID": "48"
		},
		"id": "63",
		"accountID": "101-004-9126938-001",
		"userID": 9126938,
		"batchID": "63",
		"requestID": "78499870091108123",
		"time": "2018-09-03T07:05:02.254475099Z"
	},
	"orderFillTransaction": {
		"type": "ORDER_FILL",
		"orderID": "63",
		"instrument": "GBP_USD",
		"units": "357",
		"requestedUnits": "357",
		"price": "1.29171",
		"pl": "3.5075",
		"financing": "0.0039",
		"commission": "0.0000",
		"accountBalance": "100003.4314",
		"gainQuoteHomeConversionFactor": "0.861846074291",
		"lossQuoteHomeConversionFactor": "0.861935216949",
		"guaranteedExecutionFee": "0.0000",
		"halfSpreadCost": "0.0231",
		"fullVWAP": "1.29171",
		"reason": "MARKET_ORDER_TRADE_CLOSE",
		"tradesClosed": [{
				"tradeID": "48",
				"units": "357",
				"realizedPL": "3.5075",
				"financing": "0.0039",
				"price": "1.29171",
				"guaranteedExecutionFee": "0.0000",
				"halfSpreadCost": "0.0231"
			}
		],
		"fullPrice": {
			"closeoutBid": "1.29131",
			"closeoutAsk": "1.29196",
			"timestamp": "2018-09-03T07:05:02.145587301Z",
			"bids": [{
					"price": "1.29156",
					"liquidity": "10000000"
				}
			],
			"asks": [{
					"price": "1.29171",
					"liquidity": "10000000"
				}
			]
		},
		"id": "64",
		"accountID": "101-004-9126938-001",
		"userID": 9126938,
		"batchID": "63",
		"requestID": "78499870091108123",
		"time": "2018-09-03T07:05:02.254475099Z"
	},
	"relatedTransactionIDs": ["63", "64"],
	"lastTransactionID": "64"
}
