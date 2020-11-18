const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');
const { admin } = require('../../config/firebase.config');

jest.mock('../../config/firebase.config');

describe('Notification controller tests', () => {
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

  it('400 sendNotification missing parameter', async () => {
    const res = await api
      .post('/api/notifications')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual(
      'Missing parameters sendNotification: registrationToken or message'
    );
  });

  it('500 sendNotification invalid token', async () => {
    admin.messaging = jest.fn(() => null);

    const res = await api
      .post('/api/notifications')
      .set('Authorization', `Bear ${token}`)
      .send({
        registrationToken: 'INVALID',
        message: 'INVALID',
      });

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Could not send notification user, please try again'
    );

    admin.messaging.mockRestore();
  });

  it('200 sendNotification', async () => {
    const sendToDevice = jest.fn();
    admin.messaging = jest.fn(() => ({
      sendToDevice,
    }));

    const res = await api
      .post('/api/notifications')
      .set('Authorization', `Bear ${token}`)
      .send({
        registrationToken: 'VALID',
        message: 'VALID',
      });

    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Notification sent successfully!');
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
