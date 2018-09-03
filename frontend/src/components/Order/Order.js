import React, { Component } from 'react';
import classes from './order.css';
import Instrument from './Instrument/Instrument';
import Side from './Side/Side';
import Amount from './Amount/Amount';
import Type from './Type/Type';
import OrderId from './OrderID/OrderID';
import CreateTime from './CreateTime/CreateTime';
import OrderState from './OrderState/OrderState';
import Tpsl from './TPSL/TPSL';
import { connect } from 'react-redux';
import * as actions from '../../store/actions/action';


class Order extends Component {


  colorOrderByStatus = () => {
    switch (this.props.orderState) {

      case 'FILLED':
        return '#5C9210';
      case 'PENDING':
        return 'gray';
      case 'CANCELLED':
        return '#944317';
      default:
        break;
    }
  }

  componentWillMount() {
   
  }

  render() {

    return (
      <div id={this.props.orderId} className={classes.Order} style={{ backgroundColor: this.colorOrderByStatus() }} >
        <div className={classes.OrderId}>
          <OrderId orderId={this.props.orderId} />
          <Type type={this.props.type} />
        </div>
        <div className={classes.Instrument}>
          <Instrument instrument={this.props.instrument}  width={'50%'} height={'50%'}/>
        </div>
        <div className={classes.Amount}>
          <Amount amount={this.props.units} />
        </div>
        {/* <div className={classes.Type}>
          
        </div> */}
        <div className={classes.Tpsl}>
          <Tpsl takeProfit={this.props.takeProfit} stopLoss={this.props.stopLoss} />
        </div>
        <div className={classes.OrderState}>
          <OrderState state={this.props.orderState} fillingTransactionID={this.props.fillingTransactionID} />
        </div>
        <div className={classes.CreateTime}>
          <CreateTime createTime={this.props.createTime} />
        </div>
        <div className={classes.Side}>
          <Side side={this.props.side}  width={'100%'} height={'100%'}/>
        </div>



      </div>
    );
  }

}

const mapStateToProps = (state) => {
  return {
    order: state.order
  }
}

const mapDispatchToProps = dispatch => {
  return {
    onLoadTrade: (trade) => dispatch(actions.loadTrade(trade))
  };
}

export default connect(mapStateToProps,mapDispatchToProps)(Order);