import React, { Component } from 'react';
import Order from '../Order/Order';
import allOrders from '../../assets/orders.json';
import classes from './Orders.css';

class Orders extends Component {
  constructor() {
    super();
    this.state = {
      orders: []
    }
  }

  componentDidMount() {

    let myOrders = allOrders.allOrders.map((order) => {
      return (
        <Order
          key={order.orderId}
          instrument={order.instrument}
          units={order.units}
          side={order.side}
          type={order.type}
          takeProfit={order.takeProfit}
          stopLoss={order.stopLoss}
          orderId={order.orderId}
          price={order.price}
          orderState={order.state}
          fillingTransactionID={order.fillingTransactionID}
          createTime={order.createTime}
        />
      )
    })
    this.setState({orders: myOrders});
    };
  


  render() {
 
    return (
      <div className={classes.Orders}>
        {this.state.orders}
      </div>
    );
  }
}


export default Orders;

