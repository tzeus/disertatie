import React, { Component } from 'react';
import Account from "./components/Account/Account";
import './App.css';
import {Route, Switch} from 'react-router-dom';
import Layout from './components/Layout/Layout';
import Orders from './components/Orders/Orders';


class App extends Component {
  render() {
    return (
      <div className="App">
        <Layout>
          <Switch>
          <Route path="/orders" component={Orders} />
          <Route path="/" component={Account} />
          </Switch>
        </Layout>
      </div>
    );
  }
}

export default App;
