const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');
const { generateOutfit } = require('../../service/outfits-service');

jest.mock('../../service/outfits-service');

jest.mock('../../model/outfit');

const Outfit = require('../../model/outfit');

describe('Outfit controller tests', () => {
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

  it('500 internal generateOutfit failed', async () => {
    let res;
    generateOutfit.mockImplementation(() => {
      throw 'Error!!!';
    });

    res = await api
      .get('/api/outfits/one')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Failed to generate an outfit, please try again later'
    );

    res = await api
      .get('/api/outfits/multiple')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Failed to generate an outfit, please try again later'
    );
  });

  it('400 one outfit generateOutfit warning', async () => {
    generateOutfit.mockImplementation(() => {
      return {
        success: false,
        message: 'MESSAGE',
        warning: 'WARNING',
      };
    });

    const res = await api
      .get('/api/outfits/one')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('MESSAGE');
    expect(res.body.warning).toEqual('WARNING');
  });

  it('200 one outfit generateOutfit success', async () => {
    generateOutfit.mockImplementation(() => {
      return {
        success: true,
        message: 'MESSAGE',
      };
    });

    const res = await api
      .get('/api/outfits/one')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('MESSAGE');
  });

  it('200 multiple outfit generateOutfit success true', async () => {
    generateOutfit.mockImplementation(() => {
      return {
        success: true,
        outfit: {
          _id: 'OUTFIT_ID',
        },
      };
    });

    const res = await api
      .get('/api/outfits/multiple')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.messages).toEqual([]);
    expect(res.body.warnings).toEqual([]);
    expect(res.body.outfits).toEqual([
      {
        _id: 'OUTFIT_ID',
      },
    ]);
  });

  it('200 multiple outfit generateOutfit success false', async () => {
    generateOutfit.mockImplementation(() => {
      return {
        success: false,
        message: 'MESSAGE',
        warning: 'WARNING',
        outfit: 'OUTFIT',
      };
    });

    const res = await api
      .get('/api/outfits/multiple')
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.messages).toEqual(['MESSAGE']);
    expect(res.body.warnings).toEqual(['WARNING']);
    expect(res.body.outfits).toEqual([]);
  });

  const OutfitId = 'TEST_OUTFIT_ID';
  it('422 update user opinion missing parameter', async () => {
    const res = await api
      .put(`/api/outfits/${OutfitId}`)
      .set('Authorization', `Bear ${token}`);

    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data'
    );
  });

  it('500 update user opinion database exception', async () => {
    jest.spyOn(Outfit, 'findOneAndUpdate').mockImplementationOnce(() =>
      Promise.reject({
        opinion: 'OPINION',
      })
    );

    const res = await api
      .put(`/api/outfits/${OutfitId}`)
      .set('Authorization', `Bear ${token}`)
      .send({
        opinion: 'OPINION',
      });

    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Failed to change user opinion of the outfit, please try again later'
    );
  });

  it('200 update user opinion', async () => {
    jest.spyOn(Outfit, 'findOneAndUpdate').mockImplementationOnce(() =>
      Promise.resolve({
        opinion: 'OPINION',
      })
    );

    const res = await api
      .put(`/api/outfits/${OutfitId}`)
      .set('Authorization', `Bear ${token}`)
      .send({
        opinion: 'OPINION',
      });

    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Updated user opinion successfully!');
    expect(res.body.updatedOutfit).toEqual({ opinion: 'OPINION' });
  });

  it('200 delete all outfits', async () => {
    const res = await api
      .delete(`/api/outfits/all`)
      .set('Authorization', `Bearer ${token}`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Outfits deleted successfully!');
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
