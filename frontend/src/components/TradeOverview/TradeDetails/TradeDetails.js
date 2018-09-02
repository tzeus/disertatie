import React, { Component } from 'react';
import classes from './TradeDetails.css'
import Auxiliary from './../../../hoc/Auxiliary';
import ExecutionPrice from './ExecutionPrice/ExecutionPrice';
import Financing from './Financing/Financing';
import MarginRequired from './Margin/MarginRequired/MarginRequired';
import MarginUsed from './Margin/MarginUsed/MarginUsed';
import RealizedUnrealizedPL from './RealizedUnrealizedPL/RealizedUnrealizedPL';
import TradeId from './TradeID/TradeID';
import TradeState from './TradeState/TradeState';
import Instrument from './../../Order/Instrument/Instrument';
import Side from './../../Order/Side/Side';
import Tpsl from './../../Order/TPSL/TPSL';
import Amount from './../../Order/Amount/Amount';
import CreateTime from './../../Order/CreateTime/CreateTime';

class TradeDetails extends Component {

  state = {
    trade: {
      instrument: 'GBP_USD',
      side: 'SHORT',
      executionPrice: '1.30311',
      realizedPL: '0.0000',
      unrealizedPL: '2.0396',
      marginRequired: '13.2308',
      marginUsed: '13.2761',
      financing: '0.0154',
      tradeId: '48',
      tradeState: 'OPEN',
      tradeDate: '2018-08-29 23:38:14.00',
      amount: '-357',
      takeProfit: '0.0',
      stopLoss: '0.0'
    }
  }

  colorOrderByStatus = () => {
    const tradeProfit = parseFloat(this.state.trade.unrealizedPL);
    if (tradeProfit > 0) {
      return '#5C9210';
    } else if (tradeProfit === 0) {
      return 'gray';
    } else if (tradeProfit < 0) {
      return '#944317';
    }
  }


  componentDidMount() {
    // if (this.props != null) {
    //   this.setState({
    //     trade: {
    //       instrument: this.props.instrument,
    //       side: this.props.side,
    //       executionPrice: this.props.executionPrice,
    //       realizedPL: this.props.realizedPL,
    //       unrealizedPL: this.props.unrealizedPL,
    //       marginRequired: this.props.marginRequired,
    //       marginUsed: this.props.marginUsed,
    //       financing: this.props.financing,
    //       tradeId: this.props.tradeId,
    //       tradeState: this.props.state,
    //       tradeDate: this.props.tradeDate,
    //       amount: this.props.units,
    //       takeProfit: this.props.takeProfit,
    //       stopLoss: this.props.stopLoss
    //     }
    //   });
    // }

  }

  render() {
    return (
      <Auxiliary>
        <div className={classes.Wrapper} style={{ backgroundColor: this.colorOrderByStatus() }}>
          <div className={classes.Instrument}>
            <Instrument instrument={this.state.trade.instrument} width={'50%'} height={'50%'}/>
          </div>
          <div className={classes.Side}>
            <Side side={this.state.trade.side} width={'100%'} height={'100%'} />
          </div>
          <div className={classes.ExecutionPrice}>
            <ExecutionPrice executionPrice={this.state.trade.executionPrice} />
          </div>
          <div className={classes.Tpsl}>
            <Tpsl tpsl={this.state.trade.tpsl} takeProfit={this.state.trade.takeProfit} stopLoss={this.state.trade.stopLoss}/>
          </div>
          <div className={classes.RealizedUnrealizedPL}>
            <RealizedUnrealizedPL realizedPL={this.state.trade.realizedPL} unrealizedPL={this.state.trade.unrealizedPL} />
          </div>
          <div className={classes.MarginRequired}>
            <MarginRequired marginRequired={this.state.trade.marginRequired} />
            <MarginUsed marginUsed={this.state.trade.marginUsed} />
          </div>
          <div className={classes.Financing}>
            <Financing financing={this.state.trade.financing} />
          </div>
          <div className={classes.Financing}>
          </div>
          <div className={classes.TradeId}>
            <TradeId tradeId={this.state.trade.tradeId} />
          </div>
          <div className={classes.TradeState}>
            <TradeState state={this.state.trade.tradeState} />
          </div>
          <div className={classes.Amount}>
            <Amount amount={this.state.trade.amount} />
          </div>
          <div className={classes.CreateTime}>
          <CreateTime createTime={this.state.trade.tradeDate}/>
          </div>

        </div>
      </Auxiliary>
    );
  }
}

export default TradeDetails;
