const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');

describe('Weather controller tests', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  let token, userId;
  it('get token', async () => {
    await api.post('/api/users/signup').send({
      name: 'TESTING',
      email: 'testing@testing.com',
      password: 'TESTING',
    });

    const res = await api.post('/api/users/login').send({
      email: 'testing@testing.com',
      password: 'TESTING',
    });

    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toBeTruthy();
    expect(res.body.email).toEqual('testing@testing.com');
    expect(res.body.token).toBeTruthy();

    token = res.body.token;
    userId = res.body.userId;
  });

  it('200 get weather - integration', async () => {
    const res = await api
      .get('/api/weather')
      .set('Authorization', `Bearer ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.current).toBeDefined();
    expect(res.body.today).toBeDefined();
    expect(res.body.tomorrow).toBeDefined();
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
