import React, { Component } from 'react';
import classes from './Orders.css';
import Auxiliary from '../../hoc/Auxiliary';
import Order from './../Order/Order';
import { connect } from 'react-redux';
import * as actions from '../../store/actions/action';
import Axios from 'axios';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';

class Orders extends Component {
  
  componentDidMount() {
    this.props.onInitOrders();
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

    let orders = this.props.orders.map((order) => {
      return (
        <Order
          id={order.orderId}
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

    let myOrders = this.props.orders.map((order) => {
      return (

        <li key={order.orderId} style={{ backgroundColor: order.state === 'FILLED' ? '#5C9210' : '#944317' }}>
          <a href={'#'+order.orderId}>Order ID: {order.orderId}</a>
        </li>
      )
    });

    return (
      <Auxiliary>
        <Button component={Link} to="/trades">Go to TRADES</Button>
        <div className={classes.Wrapper}>
          <div className={classes.NavigationItems}>
            <ul>
              <li>Recent orders</li>
              {myOrders}
            </ul>
          </div>
          <div className={classes.Orders}>
            {orders}
          </div>
        </div>
      </Auxiliary>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    orders: state.orders,
    trade: state.trade
  };
}

const mapDispatchToProps = dispatch => {
  return {
  onInitOrders: () => {
    dispatch(actions.initOrders())
  }
};
}



export default connect(mapStateToProps,mapDispatchToProps)(Orders, Axios);

