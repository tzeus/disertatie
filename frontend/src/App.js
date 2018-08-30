import React, { Component } from 'react';
import Account from "./components/Account/Account";
import './App.css';
import {Route, Switch} from 'react-router-dom';
import Layout from './components/Layout/Layout'


class App extends Component {
  render() {
    return (
      <div className="App">
        <Layout>
          <Switch>
          <Route path="/" component={Account} />
          </Switch>
        </Layout>
      </div>
    );
  }
}

export default App;
