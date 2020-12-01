const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');

const {
  getClothes,
  getClothing,
  postClothing,
  deleteClothing,
  updateClothing,
} = require('../../controller/clothes-controllers.js');

jest.mock('../../model/clothes');

const Clothes = require('../../model/clothes');

describe('Clothes controller tests with database mocking', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

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

  const clothingOne = {
    category: 't-shirt',
    color: 'blue',
    seasons: ['Summer', 'Fall'],
    occasions: ['home'],
  };

  it("post clothes: expecting database error", async () => {
    jest.spyOn(Clothes.prototype, 'save').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bear ${token}`)
      .send(clothingOne);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual('Failed adding clothing');
  });

  it("get all clothes: expecting database error", async () => {
    jest.spyOn(Clothes, 'find').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const res = await api
      .get(`/api/clothes/${userId}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual('Failed getting clothes');
  });

  it("get one clothes: expecting database error", async () => {
    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const res = await api
      .get(`/api/clothes/${userId}/${"CLOTHING_ID"}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual('Failed getting clothing');
  });

  it("delete one clothes: expecting database error", async () => {
    jest.spyOn(Clothes, 'findOneAndDelete').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const res = await api
      .delete(`/api/clothes/${userId}/${"CLOTHING_ID"}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual('Failed deleting clothing');
  });

  it('update one clothing: expecting database error', async () => {
    jest.spyOn(Clothes, 'findOneAndUpdate').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const res = await api
      .put(`/api/clothes/${userId}/${'CLOTHING_ID'}`)
      .set('Authorization', `Bear ${token}`)
      .send(clothingOne);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual('Failed updating clothes');
  });

  it('update one clothing: expecting database assertion error', async () => {
    jest.spyOn(Clothes, 'findOneAndUpdate').mockImplementationOnce(() => {
      return {
        category: 'NO_CATEGORY',
        color: 'blue',
        seasons: ['Summer', 'Fall'],
        occasions: ['home'],
      };
    });

    const res = await api
      .put(`/api/clothes/${userId}/${'CLOTHING_ID'}`)
      .set('Authorization', `Bear ${token}`)
      .send(clothingOne);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual('Error updating clothes');
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
