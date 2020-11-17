const jwt = require('jsonwebtoken');
const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');

describe('Authentication Tests', () => {
  const testToken = 'testToken';

  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  it('422 should fail to sign up if inputs are invalid', async () => {
    const newUser = {
      name: 'TESTING',
      email: 'invalid_email',
      password: 'TESTING',
    };

    const res = await api.post('/api/users/signup').send(newUser);

    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data.'
    );
  });

  let userId, token;
  it('201 should succeed to sign up if every input is correct', async () => {
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

    userId = res.body.userId;
    token = res.body.token;
  });

  it('422 should fail to sign up if the user has existed', async () => {
    const existingUser = {
      name: 'TESTING',
      email: 'testing@testing.com',
      password: 'TESTING',
    };

    const res = await api.post('/api/users/signup').send(existingUser);

    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'User exists already, please login instead'
    );
  });

  it('200 should succeed to login for an existing user', async () => {
    const loginInfo = {
      email: 'testing@testing.com',
      password: 'TESTING',
    };

    const res = await api.post('/api/users/login').send(loginInfo);

    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toBeTruthy();
    expect(res.body.email).toEqual(loginInfo.email);
    expect(res.body.token).toBeTruthy();

    token = res.body.token;
  });

  it('401 should fail to login if the user does not exist', async () => {
    const loginInfo = {
      email: 'NEW@NEW.com',
      password: 'NEW_PASSWORD',
    };

    const res = await api.post('/api/users/login').send(loginInfo);

    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );
  });

  it('401 missing login info', async () => {
    const loginInfo = {
      email: '',
      password: '',
    };

    const res = await api.post('/api/users/login').send(loginInfo);

    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );
  });

  it('401 should fail to login if the password is invalid', async () => {
    const loginInfo = {
      email: 'testing@testing.com',
      password: 'WRONG_PASSWORD',
    };

    const res = await api.post('/api/users/login').send(loginInfo);

    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );
  });

  it('200 get user profile', async () => {
    const res = await api
      .get('/api/users/me')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.id).toEqual(userId);
    expect(res.body.email).toEqual('testing@testing.com');
    expect(res.body.city).toEqual('vancouver');
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
