const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');
const fs = require('fs');
const { hashCode } = require('../../utils/hash');

describe('Closet integration tests', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
    jest.setTimeout(60000);
  });

  let res, userId, token, clothesId, outfitId;

  const testUser = {
    name: 'TESTING',
    email: 'testing@testing.com',
    password: 'TESTING',
  };
  const invalidUser = {
    name: 'TESTING',
    email: 'invalid_email',
    password: 'TESTING',
  };
  const existingUser = {
    name: 'TESTING',
    email: 'testing@testing.com',
    password: 'TESTING',
  };

  it('should have correct responses for POST /api/users/signup', async () => {
    res = await api.post('/api/users/signup').send(testUser);
    expect(res.statusCode).toEqual(201);
    expect(res.body.email).toEqual(testUser.email);
    expect(res.body.userId).toBeTruthy();
    expect(res.body.token).toBeTruthy();
    userId = res.body.userId;

    res = await api.post('/api/users/signup').send(invalidUser);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data.'
    );

    res = await api.post('/api/users/signup').send(existingUser);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'User exists already, please login instead'
    );
  });

  const invalidLoginInfo = {
    email: '',
    password: '',
  };
  const wrongPasswordLoginInfo = {
    email: 'testing@testing.com',
    password: 'WRONG_PASSWORD',
  };
  const notExistLoginInfo = {
    email: 'NEW@NEW.com',
    password: 'NEW_PASSWORD',
  };
  const correctLoginInfo = {
    email: 'testing@testing.com',
    password: 'TESTING',
  };

  it('should have correct responses for POST /api/users/login', async () => {
    res = await api.post('/api/users/login').send(invalidLoginInfo);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );

    res = await api.post('/api/users/login').send(wrongPasswordLoginInfo);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );

    res = await api.post('/api/users/login').send(notExistLoginInfo);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual(
      'Invalid credentials, could not log you in.'
    );

    res = await api.post('/api/users/login').send(correctLoginInfo);
    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toBeTruthy();
    expect(res.body.email).toEqual(correctLoginInfo.email);
    expect(res.body.token).toBeTruthy();

    userId = res.body.userId;
    token = res.body.token;
  });

  it('should have correct response for GET /api/users/me', async () => {
    // Fail without authentication
    res = await api.get('/api/users/me');
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Authentication failed!');

    res = await api
      .get('/api/users/me')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.id).toEqual(userId);
    expect(res.body.email).toEqual(testUser.email);
    expect(res.body.name).toEqual(testUser.name);
    expect(res.body.city).toEqual('vancouver');
  });

  const updatedTestUser = {
    name: 'NEW TESTING',
    email: 'newtesting@newtesting.com',
    city: 'burnaby',
  };
  const originalTestUser = {
    name: 'TESTING',
    email: 'testing@testing.com',
    city: 'vancouver',
  };
  const invalidProfile = {
    name: 'NEW TESTING',
    email: 'newtesting@newtesting.com',
  };
  const otherUser = {
    name: 'TESTING',
    email: 'othertesting@othertesting.com',
    password: 'TESTING',
  };
  const existingUserProfile = {
    name: 'TESTING',
    email: 'othertesting@othertesting.com',
    city: 'vancouver',
  };

  it('should have correct response for PUT /api/users/me', async () => {
    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bearer ${token}`)
      .send(invalidProfile);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data.'
    );

    res = await api.post('/api/users/signup').send(otherUser);
    expect(res.statusCode).toEqual(201);
    expect(res.body.email).toEqual(otherUser.email.toLowerCase());
    expect(res.body.userId).toBeTruthy();
    expect(res.body.token).toBeTruthy();

    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bearer ${token}`)
      .send(existingUserProfile);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'The email has been registered, please change to another one'
    );

    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bearer ${token}`)
      .send(updatedTestUser);
    expect(res.statusCode).toEqual(200);
    expect(res.body.updatedUser.id).toEqual(userId);
    expect(res.body.updatedUser.email).toEqual(updatedTestUser.email);
    expect(res.body.updatedUser.city).toEqual(updatedTestUser.city);

    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bearer ${token}`)
      .send(originalTestUser);
    expect(res.statusCode).toEqual(200);
    expect(res.body.updatedUser.id).toEqual(userId);
    expect(res.body.updatedUser.email).toEqual(originalTestUser.email);
    expect(res.body.updatedUser.city).toEqual(originalTestUser.city);
  });

  it('should have correct response for GET /api/weather/', async () => {
    res = await api.get('/api/weather').set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.current).toBeDefined();
    expect(res.body.today).toBeDefined();
    expect(res.body.tomorrow).toBeDefined();
  });

  const validDateMonth = 'Nov-2020';
  const validDateDay = 'Nov-2020-25';
  const emptyDate = null;
  const invalidDate = 'Nov-2020-25-extra';
  const validCode =
    '4/1AfDhmrh-4Hp4mrkHhTVdZlYSqwwvwZd3U0oK87Uz-mIfvl9LSx1zUST2CWM';
  const emptyCode = null;
  const expectedNumEventsMonth = 9;
  const expectedNumEventsDay = 1;

  it('should have correct response for POST /api/calendar/:date', async () => {
    res = await api
      .post(`/api/calendar/${emptyDate}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing parameters');

    res = await api
      .post(`/api/calendar/${validDateMonth}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ code: emptyCode });
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing parameters');

    res = await api
      .post(`/api/calendar/${invalidDate}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ code: validCode });
    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Failed to fetch calendar events, please check the date format'
    );

    res = await api
      .post(`/api/calendar/${validDateMonth}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ code: validDateMonth });
    expect(res.statusCode).toEqual(200);
    expect(res.body.length).toEqual(expectedNumEventsMonth);

    res = await api
      .post(`/api/calendar/${validDateDay}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ code: validDateMonth });
    expect(res.statusCode).toEqual(200);
    expect(res.body.length).toEqual(expectedNumEventsDay);
  });

  const registrationToken = 'sampleToken';
  const incorrectMessageFormat = {
    message: 'I am a message',
  };

  it('should have correct response for POST /api/notifications/', async () => {
    res = await api
      .post('/api/notifications')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual(
      'Missing parameters sendNotification: registrationToken or message'
    );

    res = await api
      .post('/api/notifications')
      .set('Authorization', `Bearer ${token}`)
      .send({
        registrationToken,
        message: incorrectMessageFormat,
      });
    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Could not send notification user, please try again'
    );
  });

  const testClothes = {
    category: 'outerwear',
    color: 'black',
    seasons: ['All'],
    occasions: ['formal'],
  };
  const emptyClothes = {};
  const emptyValueClothes = {
    category: '',
    color: '',
    seasons: '',
    occasions: '',
  };
  const invalidOccasions = {
    category: 't-shirt',
    color: 'blue',
    seasons: 'Seasons',
    occasions: 'NOT_AN_ARRAY',
  };
  const invalidSeasons = {
    category: 't-shirt',
    color: 'blue',
    seasons: ['NOT_AN_SEASON'],
    occasions: ['CLOTH'],
  };

  it('should have correct response for POST /api/clothes/:userId', async () => {
    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', 'Bearer INVALID')
      .send(testClothes);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Authentication failed!');

    res = await api
      .get(`/api/clothes/${null}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Token missing or invalid');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(emptyClothes);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing parameters');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(emptyValueClothes);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing clothes values');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(invalidOccasions);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Invalid occasions; should be an array');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(invalidSeasons);
    const seasonList = ['Spring', 'Summer', 'Fall', 'Winter', 'All'];
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual(
      `Invalid seasons; can only include ${seasonList}`
    );

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(testClothes);
    expect(res.statusCode).toEqual(201);
    expect(res.body.seasons).toEqual(testClothes.seasons);
    expect(res.body.occasions).toEqual(testClothes.occasions);
    expect(res.body.color).toEqual(testClothes.color);
    expect(res.body.category).toEqual(testClothes.category);
    expect(res.body.user).toBeTruthy();
    expect(res.body.id).toBeTruthy();

    clothesId = res.body.id;
  });

  it('should have correct response for GET /api/clothes/:userId', async () => {
    res = await api
      .get(`/api/clothes/${userId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    let resClothes = res.body.clothes;
    expect(Array.isArray(res.body.clothes));
    expect(resClothes.length).toEqual(1);
    expect(resClothes[0].seasons).toEqual(testClothes.seasons);
    expect(resClothes[0].occasions).toEqual(testClothes.occasions);
    expect(resClothes[0].color).toEqual(testClothes.color);
    expect(resClothes[0].category).toEqual(testClothes.category);
  });

  it('should have correct response for GET /api/clothes/:userId/:clothesId', async () => {
    const res = await api
      .get(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.seasons).toEqual(testClothes.seasons);
    expect(res.body.occasions).toEqual(testClothes.occasions);
    expect(res.body.color).toEqual(testClothes.color);
    expect(res.body.category).toEqual(testClothes.category);
    expect(res.body.user).toBeTruthy();
    expect(res.body.id).toBeTruthy();
  });

  it('should have correct response for GET /api/clothes/:userId?category=category', async () => {
    res = await api
      .get(`/api/clothes/${userId}?category=${testClothes.category}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    const resClothes = res.body.clothes;
    expect(Array.isArray(resClothes));
    expect(resClothes.length).toEqual(1);
    expect(resClothes[0].seasons).toEqual(testClothes.seasons);
    expect(resClothes[0].occasions).toEqual(testClothes.occasions);
    expect(resClothes[0].color).toEqual(testClothes.color);
    expect(resClothes[0].category).toEqual(testClothes.category);
  });

  it('should have correct response for PUT /api/clothes/:userId/:clothesId', async () => {
    res = await api
      .put(`/api/clothes/${null}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(emptyClothes);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Token missing or invalid');

    res = await api
      .put(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(emptyClothes);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing parameters');

    res = await api
      .put(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(emptyValueClothes);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing clothes values');

    res = await api
      .put(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(invalidOccasions);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Invalid occasions; should be an array');

    res = await api
      .put(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(invalidSeasons);
    const seasonList = ['Spring', 'Summer', 'Fall', 'Winter', 'All'];
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual(
      `Invalid seasons; can only include ${seasonList}`
    );

    res = await api
      .put(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(testClothes);
    expect(res.statusCode).toEqual(200);
    expect(res.body.seasons).toEqual(testClothes.seasons);
    expect(res.body.occasions).toEqual(testClothes.occasions);
    expect(res.body.color).toEqual(testClothes.color);
    expect(res.body.category).toEqual(testClothes.category);
    expect(res.body.user).toBeTruthy();
    expect(res.body.id).toBeTruthy();
  });

  it('should have correct response for DELETE /api/clothes/:userId/:clothesId', async () => {
    res = await api
      .delete(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Deleted clothing');

    res = await api
      .delete(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(404);
    expect(res.body.message).toEqual('Not found or already deleted');

    res = await api
      .get(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(404);
    expect(res.body.message).toEqual('Not found');

    res = await api
      .get(`/api/clothes/${userId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(404);
    expect(res.statusCode).toEqual(404);
    expect(res.body.message).toEqual('Not found');
  });

  it('should have correct response for POST /api/images/:userId/:clothesId', async () => {
    res = await api
      .post(`/api/images/${null}/${null}`)
      .set('Authorization', `Bearer ${token}`)
      .attach(
        'ClothingImage',
        "../backend/static/And you don't seem to understand.png"
      );
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Token missing or invalid');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bearer ${token}`)
      .send(testClothes);
    expect(res.statusCode).toEqual(201);
    expect(res.body.seasons).toEqual(testClothes.seasons);
    expect(res.body.occasions).toEqual(testClothes.occasions);
    expect(res.body.color).toEqual(testClothes.color);
    expect(res.body.category).toEqual(testClothes.category);
    expect(res.body.user).toBeTruthy();
    expect(res.body.id).toBeTruthy();
    clothesId = res.body.id;

    res = await api
      .post(`/api/images/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`)
      .attach(
        'ClothingImage',
        "../backend/static/And you don't seem to understand.png"
      );
    expect(res.statusCode).toEqual(201);
    expect(res.body.message).toEqual('Uploaded image!');
  });

  it('should have correct response for GET /UserClothingImages/:userId/imageName.imageExt', async () => {
    const res = await api.get(`/UserClothingImages/${userId}/${clothesId}.png`);
    expect(res.statusCode).toEqual(200);
  });

  it('should have correct response for DELETE /api/images/:userId/:clothesId', async () => {
    res = await api
      .delete(`/api/images/${null}/${null}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Token missing or invalid');

    res = await api
      .delete(`/api/images/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Deleted image');

    res = await api.get(`/UserClothingImages/${userId}/${clothesId}.png`);
    expect(res.statusCode).toEqual(404);
    expect(res.body.message).toEqual('Could not find this route');

    // Clear clothes
    res = await api
      .delete(`/api/clothes/${userId}/${clothesId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Deleted clothing');
  });

  const testClothesArray = [
    {
      category: 'outerwear',
      color: 'black',
      seasons: ['All'],
      occasions: ['formal'],
    },
    {
      category: 'outerwear',
      color: 'blue',
      seasons: ['All'],
      occasions: ['formal'],
    },
    {
      category: 'shirt',
      color: 'white',
      seasons: ['All'],
      occasions: ['formal'],
    },
    {
      category: 'shirt',
      color: 'blue',
      seasons: ['All'],
      occasions: ['formal'],
    },
    {
      category: 'trousers',
      color: 'black',
      seasons: ['All'],
      occasions: ['formal'],
    },
    {
      category: 'trousers',
      color: 'blue',
      seasons: ['All'],
      occasions: ['formal'],
    },
    {
      category: 'shoes',
      color: 'black',
      seasons: ['All'],
      occasions: ['formal'],
    },
    {
      category: 'shoes',
      color: 'white',
      seasons: ['All'],
      occasions: ['formal'],
    },
    {
      category: 'outerwear',
      color: 'yellow',
      seasons: ['Summer'],
      occasions: ['causal'],
    },
    {
      category: 'outerwear',
      color: 'red',
      seasons: ['Fall'],
      occasions: ['causal'],
    },
    {
      category: 'outerwear',
      color: 'white',
      seasons: ['Spring', 'Winter'],
      occasions: ['causal'],
    },
    {
      category: 'shirt',
      color: 'blue',
      seasons: ['Spring', 'Fall'],
      occasions: ['causal'],
    },
    {
      category: 'shirt',
      color: 'pink',
      seasons: ['Summer'],
      occasions: ['causal'],
    },
    {
      category: 'shirt',
      color: 'purple',
      seasons: ['Summer'],
      occasions: ['causal'],
    },
    {
      category: 'trousers',
      color: 'black',
      seasons: ['Summer'],
      occasions: ['causal'],
    },
    {
      category: 'trousers',
      color: 'grey',
      seasons: ['Fall'],
      occasions: ['causal'],
    },
    {
      category: 'trousers',
      color: 'green',
      seasons: ['All'],
      occasions: ['causal'],
    },
    {
      category: 'shoes',
      color: 'pink',
      seasons: ['Summer'],
      occasions: ['causal'],
    },
    {
      category: 'shoes',
      color: 'black',
      seasons: ['Winter'],
      occasions: ['causal'],
    },
    {
      category: 'shoes',
      color: 'white',
      seasons: ['All'],
      occasions: ['causal'],
    },
  ];
  let testClothesIds = [];

  it('should have correct response for GET /api/outfits/one', async () => {
    // Post all necessary clothes for testing
    for (const clothes of testClothesArray) {
      res = await api
        .post(`/api/clothes/${userId}`)
        .set('Authorization', `Bearer ${token}`)
        .send(clothes);
      expect(res.statusCode).toEqual(201);
      testClothesIds.push(res.body.id);
    }

    // Get a formal outfit
    res = await api
      .get('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.success).toBeTruthy();
    expect(res.body.message).toEqual('New outfit generated successfully!');
    expect(res.body.outfit.occasions).toEqual(['formal']);
    expect(res.body.outfit.seasons).toEqual(['All']);
    expect(res.body.outfit.opinion).toEqual('unknown');
    expect(res.body.outfit.user).toEqual(userId);
    expect(res.body.outfit._id).toEqual(
      hashCode(
        res.body.outfit.chosenUpperClothes.id +
          res.body.outfit.chosenTrousers.id +
          res.body.outfit.chosenShoes.id
      )
    );

    // Get two normal outfits
    res = await api
      .get('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.success).toBeTruthy();
    expect(res.body.message).toEqual('New outfit generated successfully!');
    expect(res.body.outfit.occasions).toEqual(['normal']);
    expect(res.body.outfit.opinion).toEqual('unknown');
    expect(res.body.outfit.user).toEqual(userId);
    expect(res.body.outfit._id).toEqual(
      hashCode(
        res.body.outfit.chosenUpperClothes.id +
          res.body.outfit.chosenTrousers.id +
          res.body.outfit.chosenShoes.id
      )
    );

    res = await api
      .get('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.success).toBeTruthy();
    expect(res.body.message).toEqual('New outfit generated successfully!');
    expect(res.body.outfit.occasions).toEqual(['normal']);
    expect(res.body.outfit.opinion).toEqual('unknown');
    expect(res.body.outfit.user).toEqual(userId);
    expect(res.body.outfit._id).toEqual(
      hashCode(
        res.body.outfit.chosenUpperClothes.id +
          res.body.outfit.chosenTrousers.id +
          res.body.outfit.chosenShoes.id
      )
    );

    // Get error message for duplication and manual should be true
    res = await api
      .get('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(400);
    expect(res.body.success).toBeFalsy();
    expect(res.body.manual).toBeTruthy();
    expect(res.body.message).toEqual(
      'We have generated all possible outfits. Do you want to create one manually?'
    );

    // Clear all clothes
    for (const id of testClothesIds) {
      res = await api
        .delete(`/api/clothes/${userId}/${id}`)
        .set('Authorization', `Bearer ${token}`);
      expect(res.statusCode).toEqual(200);
      expect(res.body.message).toEqual('Deleted clothing');
    }
    testClothesIds = [];
  });

  it('should have correct response for GET /api/outfits/multiple', async () => {
    // Post all necessary clothes for testing
    for (const clothes of testClothesArray) {
      res = await api
        .post(`/api/clothes/${userId}`)
        .set('Authorization', `Bearer ${token}`)
        .send(clothes);
      expect(res.statusCode).toEqual(201);
      testClothesIds.push(res.body.id);
    }

    // Get multiple outfits
    res = await api
      .get('/api/outfits/multiple')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(
      res.body.messages.includes('New outfit generated successfully!')
    ).toBeTruthy();
    expect(res.body.warnings.length).toEqual(0);
    expect(res.body.outfits.length).toBeGreaterThan(0);
    for (const outfit of res.body.outfits) {
      expect(outfit.opinion).toEqual('unknown');
      expect(outfit.user).toEqual(userId);
      expect(outfit._id).toEqual(
        hashCode(
          outfit.chosenUpperClothes.id +
            outfit.chosenTrousers.id +
            outfit.chosenShoes.id
        )
      );
    }

    // Clear all clothes
    for (const id of testClothesIds) {
      res = await api
        .delete(`/api/clothes/${userId}/${id}`)
        .set('Authorization', `Bearer ${token}`);
      expect(res.statusCode).toEqual(200);
      expect(res.body.message).toEqual('Deleted clothing');
    }
    testClothesIds = [];
  });

  const emptyOpinion = null;
  const likeOpinion = 'like';
  const dislikeOpinion = 'dislike';
  const invalidOutfitId = '123';

  it('should have correct response for PUT /api/outfits/:outfitId', async () => {
    // Post all necessary clothes for testing
    for (const clothes of testClothesArray) {
      res = await api
        .post(`/api/clothes/${userId}`)
        .set('Authorization', `Bearer ${token}`)
        .send(clothes);
      expect(res.statusCode).toEqual(201);
      testClothesIds.push(res.body.id);
    }

    // Get an outfit
    res = await api
      .get('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.success).toBeTruthy();
    expect(res.body.message).toEqual('New outfit generated successfully!');
    expect(res.body.outfit.opinion).toEqual('unknown');
    expect(res.body.outfit.user).toEqual(userId);
    expect(res.body.outfit._id).toEqual(
      hashCode(
        res.body.outfit.chosenUpperClothes.id +
          res.body.outfit.chosenTrousers.id +
          res.body.outfit.chosenShoes.id
      )
    );
    outfitId = res.body.outfit._id;

    // Update opinion
    res = await api
      .put(`/api/outfits/${outfitId}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ opinion: emptyOpinion });
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data'
    );

    res = await api
      .put(`/api/outfits/${invalidOutfitId}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ opinion: likeOpinion });
    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Failed to change user opinion of the outfit, please try again later'
    );

    res = await api
      .put(`/api/outfits/${outfitId}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ opinion: likeOpinion });
    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Updated user opinion successfully!');
    expect(res.body.updatedOutfit.opinion).toEqual(likeOpinion);

    res = await api
      .put(`/api/outfits/${outfitId}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ opinion: dislikeOpinion });
    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Updated user opinion successfully!');
    expect(res.body.updatedOutfit.opinion).toEqual(dislikeOpinion);

    // Clear all clothes
    for (const id of testClothesIds) {
      res = await api
        .delete(`/api/clothes/${userId}/${id}`)
        .set('Authorization', `Bearer ${token}`);
      expect(res.statusCode).toEqual(200);
      expect(res.body.message).toEqual('Deleted clothing');
    }
    testClothesIds = [];
  });

  it('should have correct response for POST /api/outfits/one', async () => {
    // Post all necessary clothes for testing
    for (const clothes of testClothesArray) {
      res = await api
        .post(`/api/clothes/${userId}`)
        .set('Authorization', `Bearer ${token}`)
        .send(clothes);
      expect(res.statusCode).toEqual(201);
      testClothesIds.push(res.body.id);
    }

    const emptyOutfit = {};
    const invalidOutfitOccasions = {
      clothes: [testClothesIds[0], testClothesIds[4], testClothesIds[6]],
      occasions: 'NOT_AN_ARRAY',
      seasons: ['All'],
    };
    const invalidOutfitClothes = {
      clothes: [testClothesIds[0]],
      occasions: ['normal'],
      seasons: ['All'],
    };
    const invalidOutfitSeasons = {
      clothes: [testClothesIds[0], testClothesIds[4], testClothesIds[6]],
      occasions: ['normal'],
      seasons: ['NOT_AN_SEASON'],
    };
    const newOutfit = {
      clothes: [testClothesIds[0], testClothesIds[4], testClothesIds[6]],
      occasions: ['formal'],
      seasons: ['All'],
    };
    const validSeasons = ['Spring', 'Summer', 'Fall', 'Winter', 'All'];
    const defaultManualOpinion = 'like';

    // Failure cases
    res = await api
      .post('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`)
      .send(emptyOutfit);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data'
    );

    res = await api
      .post('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`)
      .send(invalidOutfitOccasions);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data'
    );

    res = await api
      .post('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`)
      .send(invalidOutfitClothes);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'An outfit should consist of three clothes, please check your selections'
    );

    res = await api
      .post('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`)
      .send(invalidOutfitSeasons);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      `Invalid seasons, should be one of ${validSeasons}, please check your input data`
    );

    res = await api
      .post('/api/outfits/one')
      .set('Authorization', `Bearer ${token}`)
      .send(newOutfit);
    expect(res.statusCode).toEqual(201);
    expect(res.body.success).toBeTruthy();
    expect(res.body.message).toEqual('Outfit created successfully!');
    expect(res.body.outfit.clothes).toEqual(newOutfit.clothes);
    expect(res.body.outfit.occasions).toEqual(newOutfit.occasions);
    expect(res.body.outfit.seasons).toEqual(newOutfit.seasons);
    expect(res.body.outfit.opinion).toEqual(defaultManualOpinion);
    expect(res.body.outfit.user).toEqual(userId);
    expect(res.body.outfit._id).toEqual(
      hashCode(newOutfit.clothes[0], newOutfit.clothes[1], newOutfit.clothes[2])
    );
    expect(res.body.outfit.chosenUpperClothes.id).toEqual(newOutfit.clothes[0]);
    expect(res.body.outfit.chosenTrousers.id).toEqual(newOutfit.clothes[1]);
    expect(res.body.outfit.chosenShoes.id).toEqual(newOutfit.clothes[2]);

    // Clear all clothes
    for (const id of testClothesIds) {
      res = await api
        .delete(`/api/clothes/${userId}/${id}`)
        .set('Authorization', `Bearer ${token}`);
      expect(res.statusCode).toEqual(200);
      expect(res.body.message).toEqual('Deleted clothing');
    }
    testClothesIds = [];
  });

  afterAll(async done => {
    fs.rmdirSync('../backend/static/UserClothingImages/', { recursive: true });
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
