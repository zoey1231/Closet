const config = require('../utils/config');
const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../app');

describe('clothes', () => {
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

  const oneClothing = {
    category: 't-shirt',
    color: 'blue',
    seasons: ['Summer', 'Fall'],
    occasions: ['home'],
  };

  let oneClothingId;
  it('201 post one clothing', async () => {
    const res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bear ${token}`)
      .send(oneClothing);

    expect(res.statusCode).toEqual(201);

    expect(res.body.seasons).toEqual(oneClothing.seasons);
    expect(res.body.occasions).toEqual(oneClothing.occasions);
    expect(res.body.color).toEqual(oneClothing.color);
    expect(res.body.category).toEqual(oneClothing.category);

    expect(res.body.user).toBeTruthy();
    expect(res.body.id).toBeTruthy();

    oneClothingId = res.body.id;
  });

  it('200 get one clothing', async () => {
    const res = await api
      .get(`/api/clothes/${userId}/${oneClothingId}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(200);

    expect(res.body.seasons).toEqual(oneClothing.seasons);
    expect(res.body.occasions).toEqual(oneClothing.occasions);
    expect(res.body.color).toEqual(oneClothing.color);
    expect(res.body.category).toEqual(oneClothing.category);

    expect(res.body.user).toBeTruthy();
    expect(res.body.id).toBeTruthy();
  });

  it('200 delete one clothing', async () => {
    const res = await api
      .delete(`/api/clothes/${userId}/${oneClothingId}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Deleted clothing');
  });

  it('404 deleted clothing should not be get', async () => {
    const res = await api
      .get(`/api/clothes/${userId}/${oneClothingId}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(404);
    expect(res.body.message).toEqual('Not found');
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
