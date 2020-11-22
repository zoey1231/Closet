const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');

describe('Closet integration tests', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  let res, userId, token;

  const newUser = {
    name: 'TESTING',
    email: 'testing@testing.com',
    password: 'TESTING',
  };
  const invalidUser = {
    name: 'TESTING',
    email: 'invalid_email',
    password: 'TESTING',
  };
  const existingUser = {
    name: 'TESTING',
    email: 'testing@testing.com',
    password: 'TESTING',
  };

  it('should have correct responses for POST /api/users/signup', async () => {
    res = await api.post('/api/users/signup').send(newUser);
    expect(res.statusCode).toEqual(201);
    expect(res.body.email).toEqual(newUser.email.toLowerCase());
    expect(res.body.userId).toBeTruthy();
    expect(res.body.token).toBeTruthy();
    userId = res.body.userId;

    res = await api.post('/api/users/signup').send(invalidUser);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data.'
    );

    res = await api.post('/api/users/signup').send(existingUser);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'User exists already, please login instead'
    );
  });

  const invalidLoginInfo = {
    email: '',
    password: '',
  };
  const wrongPasswordLoginInfo = {
    email: 'testing@testing.com',
    password: 'WRONG_PASSWORD',
  };
  const notExistLoginInfo = {
    email: 'NEW@NEW.com',
    password: 'NEW_PASSWORD',
  };
  const correctLoginInfo = {
    email: 'testing@testing.com',
    password: 'TESTING',
  };

  it('should have correct responses for POST /api/users/login', async () => {
    res = await api.post('/api/users/login').send(invalidLoginInfo);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );

    res = await api.post('/api/users/login').send(wrongPasswordLoginInfo);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );

    res = await api.post('/api/users/login').send(notExistLoginInfo);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );

    res = await api.post('/api/users/login').send(correctLoginInfo);
    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toBeTruthy();
    expect(res.body.email).toEqual(correctLoginInfo.email);
    expect(res.body.token).toBeTruthy();

    userId = res.body.userId;
    token = res.body.token;
  });

  it('should have correct response for ', async () => {
    console.log(userId);
    console.log(token);
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
