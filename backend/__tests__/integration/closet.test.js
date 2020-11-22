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

  const testUser = {
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
    res = await api.post('/api/users/signup').send(testUser);
    expect(res.statusCode).toEqual(201);
    expect(res.body.email).toEqual(testUser.email);
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

  it('should have correct response for GET /api/users/me', async () => {
    // Fail without authentication
    res = await api.get('/api/users/me');
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Authentication failed!');

    res = await api
      .get('/api/users/me')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.id).toEqual(userId);
    expect(res.body.email).toEqual(testUser.email);
    expect(res.body.name).toEqual(testUser.name);
    expect(res.body.city).toEqual('vancouver');
  });

  const updatedTestUser = {
    name: 'NEW TESTING',
    email: 'newtesting@newtesting.com',
    city: 'burnaby',
  };
  const originalTestUser = {
    name: 'TESTING',
    email: 'testing@testing.com',
    city: 'vancouver',
  };
  const invalidProfile = {
    name: 'NEW TESTING',
    email: 'newtesting@newtesting.com',
  };
  const invalidCity = {
    name: 'NEW TESTING',
    email: 'newtesting@newtesting.com',
    city: 'a',
  };
  const otherUser = {
    name: 'TESTING',
    email: 'othertesting@othertesting.com',
    password: 'TESTING',
  };
  const existingUserProfile = {
    name: 'TESTING',
    email: 'othertesting@othertesting.com',
    city: 'vancouver',
  };

  it('should have correct response for PUT /api/users/me', async () => {
    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bear ${token}`)
      .send(invalidProfile);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data.'
    );

    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bear ${token}`)
      .send(invalidCity);
    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Cannot find your city, please check and try again'
    );

    res = await api.post('/api/users/signup').send(otherUser);

    expect(res.statusCode).toEqual(201);
    expect(res.body.email).toEqual(otherUser.email.toLowerCase());
    expect(res.body.userId).toBeTruthy();
    expect(res.body.token).toBeTruthy();

    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bear ${token}`)
      .send(existingUserProfile);

    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'The email has been registered, please change to another one'
    );

    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bear ${token}`)
      .send(updatedTestUser);
    expect(res.statusCode).toEqual(200);
    expect(res.body.updatedUser.id).toEqual(userId);
    expect(res.body.updatedUser.email).toEqual(updatedTestUser.email);
    expect(res.body.updatedUser.city).toEqual(updatedTestUser.city);

    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bear ${token}`)
      .send(originalTestUser);
    expect(res.statusCode).toEqual(200);
    expect(res.body.updatedUser.id).toEqual(userId);
    expect(res.body.updatedUser.email).toEqual(originalTestUser.email);
    expect(res.body.updatedUser.city).toEqual(originalTestUser.city);
  });

  it('should have correct response for ', async () => {});

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
