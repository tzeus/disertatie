import React, { Component } from 'react';
import classes from './Home.css';
import Card from './../Cards/Card1';
import Card2 from './../Cards/Card2';
import Account from '../Account/Account';
import profitImg from '../../assets/images/profit.jpg';
import lossImg from '../../assets/images/loss.jpg';
import statsImg from '../../assets/images/stats.jpg';
import transactionsImg from '../../assets/images/transactions.png';
import { connect } from 'react-redux';
import Net from '../Cards/Net';

// import {connect} from 'react-redux';
// import * as actions from '../../store/actions/action';
import Statistics from './../Statistics/Statistics';
import amount from './../Order/Amount/Amount';

class Home extends Component {

    render() {

        let netProfitAmount = 0;
        let netLossAmount = 0;

const netProfit = () => {
    return <Net amount={netProfitAmount} text='NET PROFIT' />
};

const netLoss = () => {
    return <Net amount={netLossAmount} text='NET LOSS' />

};

       
        const statistics = () => {
            let currentOrders = this.props.orders;
            let currentTrades = this.props.trades;
            let cancelledOrders = 0; //DONE
            let filledOrders = 0; //DONE
            let pendingOrders = 0; //DONE
            let openTrades = 0;
            let closedTrades = 0;

            currentOrders.forEach(order => {
                switch (order.state) {
                    case 'FILLED':
                        filledOrders += 1;
                        break;
                    case 'CANCELLED':
                        cancelledOrders += 1;
                        break;
                    case 'PENDING':
                        pendingOrders += 1;
                        break;
                }


            });

            const bestRealizedPL = Math.max(...currentTrades.map(function (trade) { return trade.realizedPL }));
            const bestUnRealizedPL = Math.max(...currentTrades.map(function (trade) { return trade.unrealizedPL }));
            const worstRealizedPL = Math.min(...currentTrades.map(function (trade) { return trade.realizedPL }));
            const worstUnRealizedPL = Math.min(...currentTrades.map(function (trade) { return trade.unrealizedPL }));

            let highestTrade = Math.max(bestRealizedPL, bestUnRealizedPL);
            let lowestTrade = Math.min(worstRealizedPL, worstUnRealizedPL);


            let currencies = [];
            let bestCurrency;
            let worstCurrency;

            currentTrades.map(trade => {
                if (currencies.indexOf(trade.instrument.instrument) === -1) {
                    currencies.push({ currencyPair: trade.instrument.instrument, max: 0, min: 0 });
                }
            });

            currencies.forEach(currency => {
                currentTrades.forEach(trade => {
                    if (trade.instrument.instrument === Object.values(currency)[0]) {
                        currency.max += (parseFloat(trade.realizedPL) > 0 ? parseFloat(trade.realizedPL) : (parseFloat(trade.unrealizedPL) > 0 ? parseFloat(trade.unrealizedPL) : 0));
                        currency.min += (parseFloat(trade.realizedPL) < 0 ? parseFloat(trade.realizedPL) : (parseFloat(trade.unrealizedPL) < 0 ? parseFloat(trade.unrealizedPL) : 0));
                    }
                });
            });

            bestCurrency = Math.max.apply(Math, currencies.map(function (currency) { return Object.values(currency)[1] }));
            let bestPair = currencies.find(function (o) { return o.max === bestCurrency; }).currencyPair.replace('_', ' - ');
            worstCurrency = Math.min.apply(Math, currencies.map(function (currency) { return Object.values(currency)[2] }));
            let worstPair = currencies.find(function (o) { return o.min === worstCurrency; }).currencyPair.replace('_', ' - ');

            currentTrades.forEach(trade => {
                netLossAmount += (parseFloat(trade.realizedPL) < 0 ? parseFloat(trade.realizedPL) : (parseFloat(trade.unrealizedPL) < 0 ? parseFloat(trade.unrealizedPL) : 0))
                netProfitAmount += (parseFloat(trade.realizedPL) > 0 ? parseFloat(trade.realizedPL) : (parseFloat(trade.unrealizedPL) > 0 ? parseFloat(trade.unrealizedPL) : 0))
                if (trade.state === 'OPEN') {
                    openTrades += 1;
                } else {
                    closedTrades += 1;
                }




            });

            

            return <Statistics
                pendingOrders={pendingOrders}
                filledOrders={filledOrders}
                cancelledOrders={cancelledOrders}
                openTrades={openTrades}
                closedTrades={closedTrades}
                highestTrade={highestTrade}
                lowestTrade={lowestTrade}
                bestPair={bestPair}
                worstPair={worstPair}
             />
        }

        return (
            <div className={classes.Wrapper} >
                <Account />
                <Card2 image={statsImg} text={statistics()} />
                <Card image={profitImg} text={netProfit()} />
                <Card image={lossImg} text={netLoss()} />
                {/* <Card image={transactionsImg} /> */}
            </div>
        );
    }
}


const mapStateToProps = (state) => {
    return {
        orders: state.orders,
        trades: state.trades,
        transactions: state.transactions,
        stats: state.stats
    }
}

export default connect(mapStateToProps)(Home);