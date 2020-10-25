const config = require('../utils/config');
const mongoose = require('mongoose');
const server = require('../index');
const supertest = require('supertest');

const app = require('../app');

const api = supertest(app);

describe('sample test', () => {
  it('should test that true === true', () => {
    expect(true).toBe(true);
  });
});

describe('get version', () => {
  it('should get a version number', async () => {
    const res = await api.get('/version');
    expect(res.statusCode).toEqual(200);
    expect(res.body).toEqual({ message: config.VERSION });
  });
});

afterAll(() => {
  mongoose.connection.close();
  server.close();
});
