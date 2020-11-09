const config = require('../utils/config');
const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../app');

const outfitService = require('../service/outfits-service');

describe('outfit-services', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  let token, userId;
  it('get token', async () => {
    let email = 'outfit@outfit.com';
    let password = 'TESTING';
    await api.post('/api/users/signup').send({
      name: 'TESTING',
      email,
      password,
    });

    const res = await api.post('/api/users/login').send({
      email: email,
      password: password,
    });

    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toBeTruthy();
    expect(res.body.email).toEqual(email);
    expect(res.body.token).toBeTruthy();

    token = res.body.token;
    userId = res.body.userId;
  });

  const formalOutwear = {
    category: 'outwear',
    color: 'blue',
    seasons: ['Summer', 'Fall'],
    occasions: ['formal'],
  };

  const formalShirt = {
    category: 'shirt',
    color: 'blue',
    seasons: ['Summer', 'Fall'],
    occasions: ['formal'],
  };

  const formalTrousers = {
    category: 'trousers',
    color: 'blue',
    seasons: ['Summer', 'Fall'],
    occasions: ['formal'],
  };

  const formalShoes = {
    category: 'shoes',
    color: 'blue',
    seasons: ['Summer', 'Fall'],
    occasions: ['formal'],
  };

  const clothesToPost = [
    formalOutwear,
    formalShirt,
    formalTrousers,
    formalShoes,
  ];

  // it('post four formal clothes and check', async () => {
  //   for (c of clothesToPost) {
  //     let res = await api
  //       .post(`/api/clothes/${userId}`)
  //       .set('Authorization', `Bear ${token}`)
  //       .send(c);

  //     expect(res.statusCode).toEqual(201);
  //   }

  //   const res = await api
  //     .get(`/api/clothes/${userId}`)
  //     .set('Authorization', `Bear ${token}`);

  //   expect(res.statusCode).toEqual(200);

  //   let resClothes = res.body.clothes;
  //   expect(Array.isArray(resClothes));
  //   expect(resClothes.length).toEqual(4);

  //   clothesToPost.map((x, i) => {
  //     expect(x.seasons).toEqual(resClothes[i].seasons);
  //     expect(x.occasions).toEqual(resClothes[i].occasions);
  //     expect(x.color).toEqual(resClothes[i].color);
  //     expect(x.category).toEqual(resClothes[i].category);
  //   });
  // });

  it('generate formal outfit', async () => {
    const res = await api
      .get(`/api/outfits/one`)
      .set('Authorization', `Bear ${token}`);
  });

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
