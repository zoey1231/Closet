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

  it('should be able to authenticate a user', async () => {});
  it('should be able to add/get/update/delete clothes and clothes images', async () => {});
  it('should be able to get weather information and calendar event', async () => {});
  it('should be able to get/update/create outfits', async () => {});

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
