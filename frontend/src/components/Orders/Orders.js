import React, { Component } from 'react';
import allOrders from '../../assets/orders.json';
import classes from './Orders.css';
import Auxiliary from '../../hoc/Auxiliary';
import Order from './../Order/Order';

class Orders extends Component {
  constructor() {
    super();
    this.state = {
      orders: []
    }
  }

  componentDidMount() {
    let orders = allOrders.allOrders.map((order) => {
      return (
        <Order
          key={order.orderId}
          orderId={order.orderId}
          instrument={order.instrument}
          units={order.units}
          side={order.side}
          type={order.type}
          takeProfit={order.takeProfit}
          stopLoss={order.stopLoss}
          price={order.price}
          orderState={order.state}
          fillingTransactionID={order.fillingTransactionID}
          createTime={order.createTime}
        />
      )
    });
    this.setState({ orders: orders });

  }

  colorOrderByStatus = (orderState) => {
    switch (orderState) {

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


  render() {

    let myOrders = allOrders.allOrders.map((order) => {
      return (

        <li key={order.orderId} style={{ backgroundColor: order.state === 'FILLED' ? '#5C9210' : '#944317' }}>
          <a href="#">Order ID: {order.orderId}</a>
        </li>
      )
    });

    return (
      <Auxiliary>
        <div className={classes.Wrapper}>
          <div className={classes.NavigationItems}>
            <ul>
              <li>Recent orders</li>
              {myOrders}
            </ul>
          </div>
          <div className={classes.Orders}>
            {this.state.orders}
          </div>
        </div>
      </Auxiliary>
    );
  }
}


export default Orders;

