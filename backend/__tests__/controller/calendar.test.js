require('dotenv').config();

const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');

const { getCalendarEvents } = require('../../service/calendar-service');

jest.mock('../../service/calendar-service');

describe('Calendar controller tests', () => {
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

  it('400 missing parameters', async () => {
    let res;
    res = await api
      .post(`/api/calendar/${null}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing parameters');

    res = await api
      .post(`/api/calendar/${'DATE'}`)
      .set('Authorization', `Bear ${token}`)
      .send({ code: null });

    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing parameters');
  });

  const date = 'DATE';
  const code = 'CODE';
  const sendRequest = async () => {
    return await api
      .post(`/api/calendar/${date}`)
      .set('Authorization', `Bear ${token}`)
      .send({ code });
  };
  it('500 internal getCalendarEvents failed with exception', async () => {
    getCalendarEvents.mockImplementation(() => {
      throw 'Error!!!';
    });

    const res = await sendRequest();

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'There is an error occurred, please try again later'
    );
  });

  it('400 internal authentication failed', async () => {
    getCalendarEvents.mockImplementation(() => {
      return {
        success: false,
        reason: process.env.CALENDAR_CODE_ERROR,
      };
    });

    const res = await sendRequest();

    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual(
      'Authentication failed, please enter the correct code'
    );
  });

  it('500 internal getCalendarEvents failed with error code', async () => {
    let res;

    getCalendarEvents.mockImplementation(() => {
      return {
        success: false,
        reason: process.env.CALENDAR_DATE_ERROR,
      };
    });

    res = await sendRequest();

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Failed to fetch calendar events, please check the date format'
    );

    getCalendarEvents.mockImplementation(() => {
      return {
        success: false,
        reason: process.env.CALENDAR_FILE_ERROR,
      };
    });

    res = await sendRequest();

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Failed to load credentials, please try again later'
    );

    getCalendarEvents.mockImplementation(() => {
      return {
        success: false,
        reason: process.env.CALENDAR_EVENTS_ERROR,
      };
    });

    res = await sendRequest();

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Failed to fetch calendar events, please try again later'
    );

    getCalendarEvents.mockImplementation(() => {
      return {
        success: false,
        reason: 'DEFAULT ERROR CODE',
      };
    });

    res = await sendRequest();

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'There is an error occurred, please try again later'
    );
  });

  it('200 success getCalendarEvent', async () => {
    getCalendarEvents.mockImplementation(() => {
      return {
        success: true,
        reason: 'SUCCESS',
        events: "EVENTS",
      };
    });

    const res = await sendRequest();

    expect(res.statusCode).toEqual(200);
    expect(res.body).toEqual("EVENTS");
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
