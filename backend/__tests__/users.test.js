const config = require('../utils/config');
const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../app');

describe('signup and login', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  it('sign up 201 for new user', async () => {
    const newUser = {
      name: 'TESTING',
      email: 'testing@testing.com',
      password: 'TESTING',
    };

    const res = await api.post('/api/users/signup').send(newUser);

    expect(res.statusCode).toEqual(201);
    expect(res.body.email).toEqual(newUser.email.toLowerCase());
    expect(res.body.userId).toBeTruthy();
    expect(res.body.token).toBeTruthy();
  });

  it('sign up 422 for existing user', async () => {
    const existUser = {
      name: 'TESTING',
      email: 'testing@testing.com',
      password: 'TESTING',
    };

    const res = await api.post('/api/users/signup').send(existUser);

    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'User exists already, please login instead'
    );
  });

  it('login 200 for existing user', async () => {
    const loginInfo = {
      email: 'testing@testing.com',
      password: 'TESTING',
    };

    const res = await api.post('/api/users/login').send(loginInfo);

    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toBeTruthy();
    expect(res.body.email).toEqual(loginInfo.email);
    expect(res.body.token).toBeTruthy();
  });

  it('login 401 invalid user password or no user', async () => {
    const loginInfo = {
      email: 'TESTING@TESTING.com',
      password: 'WRONG_PASSWORD',
    };

    const res = await api.post('/api/users/login').send(loginInfo);

    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
