import React, { Component } from 'react';
import Home from './components/Home/Home';
import './App.css';
import {Route, Switch} from 'react-router-dom';
import Layout from './components/Layout/Layout';
import Orders from './components/Orders/Orders';
import TradeOverview from './components/TradeOverview/TradeOverview';


class App extends Component {
  render() {
    return (
      <div className="App">
        <Layout>
          <Switch>
          <Route path="/orders" component={Orders} />
          <Route path="/trades" component={TradeOverview} />
          <Route path="/" component={Home} />
          </Switch>
        </Layout>
      </div>
    );
  }
}

export default App;
