const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');

const fs = require('fs');

describe('Image controller tests', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  let token, userId;
  it('get token', async () => {
    let email = 'image@image.com';
    let password = 'TESTING';
    await api.post('/api/users/signup').send({
      name: 'TESTING',
      email,
      password,
    });

    const res = await api.post('/api/users/login').send({
      email,
      password,
    });

    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toBeTruthy();
    expect(res.body.email).toEqual(email);
    expect(res.body.token).toBeTruthy();

    token = res.body.token;
    userId = res.body.userId;
  });

  it('401 post image invalid parameter', async () => {
    const res = await api
      .post(`/api/images/${null}/${null}`)
      .set('Authorization', `Bear ${token}`)
      .attach(
        'ClothingImage',
        "../backend/static/And you don't seem to understand.png"
      );

    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Token missing or invalid');
  });

  let clothingId = 'TEST_CLOTHING_ID';
  it('201 success post image', async () => {
    const res = await api
      .post(`/api/images/${userId}/${clothingId}`)
      .set('Authorization', `Bear ${token}`)
      .attach(
        'ClothingImage',
        "../backend/static/And you don't seem to understand.png"
      );

    expect(res.statusCode).toEqual(201);
    expect(res.body.message).toEqual('Uploaded image!');
  });

  it('500 post image with invalid file extension', async () => {
    const res = await api
      .post(`/api/images/${userId}/${clothingId}`)
      .set('Authorization', `Bear ${token}`)
      .attach(
        'ClothingImage',
        "../backend/README.md"
      );

    expect(res.statusCode).toEqual(403);
    expect(res.body.message).toEqual('Extension not allowed');
  });

  it('200 get image ok after uploaded', async () => {
    const res = await api.get(
      `/UserClothingImages/${userId}/${clothingId}.png`
    );

    expect(res.statusCode).toEqual(200);
  });

  it('401 delete image invalid parameter', async () => {
    const res = await api
      .delete(`/api/images/${null}/${null}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Token missing or invalid');
  });

  it('200 deleted image', async () => {
    const res = await api
      .delete(`/api/images/${userId}/${clothingId}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Deleted image');
  });

  it('500 image not found', async () => {
    const res = await api
      .delete(`/api/images/${userId}/${clothingId}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual('Image does not exist');
  });

  it('404 image not found after deleted', async () => {
    const res = await api.get(
      `/UserClothingImages/${userId}/${clothingId}.png`
    );

    expect(res.statusCode).toEqual(404);
    expect(res.body.message).toEqual('Could not find this route');
  });

  afterAll(async done => {
    fs.rmdirSync('../backend/static/UserClothingImages/', { recursive: true });

    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
