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

class Order extends Component {
  state = {
    order: null
  }

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
    this.setState({
      order: {
        orderId: this.props.orderId,
        instrument: this.props.instrument,
        units: this.props.units,
        side: this.props.side,
        type: this.props.type,
        takeProfit: this.props.takeProfit,
        stopLoss: this.props.stopLoss,
        price: this.props.price,
        orderState: this.props.state,
        fillingTransactionID: this.props.fillingTransactionID,
        createTime: this.props.createTime
      }
    });
  }

  render() {

    return (
      <div className={classes.Order} style={{ backgroundColor: this.colorOrderByStatus() }} >
        <div className={classes.OrderId}>
          <OrderId orderId={this.props.orderId} />
          <Type type={this.props.type} />
        </div>
        <div className={classes.Instrument}>
          <Instrument instrument={this.props.instrument} />
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
          <Side side={this.props.side} />
        </div>



      </div>
    );
  }

}


export default Order;