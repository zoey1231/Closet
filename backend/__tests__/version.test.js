const config = require('../utils/config');
const mongoose = require('mongoose');
// const server = require('../index');
const supertest = require('supertest');
const http = require('http');

const app = require('../app');

describe('get version', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  it('should get a version number', async () => {
    const res = await api.get('/version');
    expect(res.statusCode).toEqual(200);
    expect(res.body).toEqual({ message: config.VERSION });
  });

  afterAll(done => {
    mongoose.connection.close();
    server.close(done);
  });
});
