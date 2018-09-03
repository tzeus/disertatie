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
import { connect } from 'react-redux';


class TradeDetails extends Component {



  colorOrderByStatus = () => {
    const tradeProfit = parseFloat(this.props.trade.unrealizedPL);
    if (tradeProfit > 0) {
      return '#5C9210';
    } else if (tradeProfit === 0) {
      return 'gray';
    } else if (tradeProfit < 0) {
      return '#944317';
    }
  }


  componentDidMount() {
    if (this.props != null) {
      // this.setState({
      //   trade: {
      //     instrument: this.props.instrument,
      //     side: this.props.side,
      //     executionPrice: this.props.executionPrice,
      //     realizedPL: this.props.realizedPL,
      //     unrealizedPL: this.props.unrealizedPL,
      //     marginRequired: this.props.marginRequired,
      //     marginUsed: this.props.marginUsed,
      //     financing: this.props.financing,
      //     tradeId: this.props.tradeId,
      //     tradeState: this.props.state,
      //     tradeDate: this.props.tradeDate,
      //     amount: this.props.units,
      //     takeProfit: this.props.takeProfit,
      //     stopLoss: this.props.stopLoss
      //   }
      // });
    }

  }

  render() {
    return (
      <Auxiliary>
        <div className={classes.Wrapper} style={{ backgroundColor: this.colorOrderByStatus() }}>
          <div className={classes.Instrument}>
            <Instrument instrument={this.props.trade.instrument} width={'50%'} height={'50%'}/>
          </div>
          <div className={classes.Side}>
            <Side side={this.props.trade.side} width={'100%'} height={'100%'} />
          </div>
          <div className={classes.ExecutionPrice}>
            <ExecutionPrice executionPrice={this.props.trade.executionPrice} />
          </div>
          <div className={classes.Tpsl}>
            <Tpsl tpsl={this.props.trade.tpsl} takeProfitPrice={this.props.trade.takeProfitPrice} stopLoss={this.props.trade.stopLoss}/>
          </div>
          <div className={classes.RealizedUnrealizedPL}>
            <RealizedUnrealizedPL realizedPL={this.props.trade.realizedPL} unrealizedPL={this.props.trade.unrealizedPL} />
          </div>
          <div className={classes.MarginRequired}>
            <MarginRequired marginRequired={this.props.trade.initialMarginRequired} />
            <MarginUsed marginUsed={this.props.trade.marginUsed} />
          </div>
          <div className={classes.Financing}>
            <Financing financing={this.props.trade.financing} />
          </div>
          <div className={classes.Financing}>
          </div>
          <div className={classes.TradeId}>
            <TradeId tradeId={this.props.trade.tradeId} />
          </div>
          <div className={classes.TradeState}>
            <TradeState state={this.props.trade.state} />
          </div>
          <div className={classes.Amount}>
            <Amount amount={this.props.trade.units} />
          </div>
          <div className={classes.CreateTime}>
          <CreateTime createTime={this.props.trade.tradeDate}/>
          </div>

        </div>
      </Auxiliary>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    trade: state.trade
  };
}

export default connect(mapStateToProps)(TradeDetails);
