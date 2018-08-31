import React, { Component } from 'react';
import classes from './order.css';

class Order extends Component {
  state = {
    order:null
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

  componentWillMount(){
    this.setState({order: {
      orderId:this.props.orderId,
      instrument:this.props.instrument,
      units:this.props.units,
      side:this.props.side,
      type:this.props.type,
      takeProfit:this.props.takeProfit,
      stopLoss:this.props.stopLoss,
      price:this.props.price,
      orderState:this.props.state,
      fillingTransactionID:this.props.fillingTransactionID,
      createTime:this.props.createTime
    }});
  }

    render() {
    console.log('Entered render method');
    
    return (
      <div className={classes.Order} style={{backgroundColor: this.colorOrderByStatus()}} >
        OrderId: <strong>{this.props.orderId}</strong>
        Instrument: <strong>{this.props.instrument}</strong>
        Units: <strong>{this.props.units}</strong> 
        Side: <strong>{this.props.side}</strong> 
        Order type: <strong>{this.props.type}</strong> 
        Take profit: <strong>{this.props.takeProfit}</strong> 
        Stop loss: <strong>{this.props.stopLoss}</strong>
        State: <strong>{this.props.orderState}</strong>
        Filling transaction id: <strong>{this.props.fillingTransactionID}</strong> 
        Created at: <strong>{this.props.createTime}</strong>
      </div>
    );
  }

}


export default Order;