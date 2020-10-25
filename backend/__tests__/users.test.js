const config = require('../utils/config');
const mongoose = require('mongoose');
// const server = require('../index');
const supertest = require('supertest');
const http = require('http');

const app = require('../app');

describe('signup', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  it('sign up 201 for new user', async () => {
    const newUser = {
      name: 'TESTING',
      email: 'TESTING@TESTING.com',
      password: 'TESTING',
    };

    const res = await api.post('/api/users/signup').send(newUser);

    expect(res.statusCode).toEqual(201);
    expect(res.body.email).toEqual(newUser.email.toLowerCase());
    expect(res.body.id);
    expect(res.body.token);
  });

  it('sign up 422 for existing user', async () => {
    const existUser = {
      name: 'TESTING',
      email: 'TESTING@TESTING.com',
      password: 'TESTING',
    };

    const res = await api.post('/api/users/signup').send(existUser);

    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'User exists already, please login instead'
    );
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
