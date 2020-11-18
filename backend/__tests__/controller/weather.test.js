const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');
const { getWeatherInfo } = require('../../service/weather-service');

jest.mock('../../service/weather-service');

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

  it('200 weather', async () => {
    getWeatherInfo.mockImplementation(() => {
      return {
        success: true,
        current: 'CURRENT',
        today: 'TODAY',
        tomorrow: 'TOMORROW',
      };
    });

    const res = await api
      .get('/api/weather')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.current).toEqual('CURRENT');
    expect(res.body.today).toEqual('TODAY');
    expect(res.body.tomorrow).toEqual('TOMORROW');
  });

  it('500 failed with no error code and message', async () => {
    getWeatherInfo.mockImplementation(() => {
      throw 'Error!!!';
    });

    const res = await api
      .get('/api/weather')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(500);
  });

  it('500 failed with error code and message', async () => {
    getWeatherInfo.mockImplementation(() => {
      return {
        success: false,
        code: 418,
        message: "I'm a teapot",
      };
    });

    const res = await api
      .get('/api/weather')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(418);
    expect(res.body.message).toEqual("I'm a teapot");
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
