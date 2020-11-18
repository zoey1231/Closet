const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');
const { getWeatherInfo, getGeoCode } = require('../../service/weather-service');

describe('Get weather information tests', () => {
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

  it('should return correct weather information', async () => {
    const response = await getWeatherInfo(userId);
    const { success, current, today, tomorrow } = response;

    expect(success).toBeTruthy();
    expect(current).toBeDefined();
    expect(today).toBeDefined();
    expect(tomorrow).toBeDefined();
  });

  it('should return error message if input user id is invalid', async () => {
    const response = await getWeatherInfo(123);
    const { success, code, message } = response;

    expect(success).toBeFalsy();
    expect(code).toEqual(500);
    expect(message).toEqual(
      'Could not get your information, please try again later'
    );
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});

describe('Get geo code tests', () => {
  let place;

  it('should return correct geo-location if input is valid', async () => {
    place = 'vancouver';

    const response = await getGeoCode(place);

    const { success, lat, lon } = response;
    expect(success).toBeTruthy();
    expect(lat).toEqual(49.2608724);
    expect(lon).toEqual(-123.1139529);
  });

  it('should return error message if the input is not valid', async () => {
    place = 'a';
    const response = await getGeoCode(place);

    const { success, code, message } = response;
    expect(success).toBeFalsy();
    expect(code).toEqual(500);
    expect(message).toEqual(
      'Cannot find your city, please check and try again'
    );
  });
});
