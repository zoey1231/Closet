const mongoose = require('mongoose');
const supertest = require('supertest');
const http = require('http');

const app = require('../../app');
const { send } = require('process');

describe('Closet integration tests', () => {
  let server, api;
  beforeAll(done => {
    server = http.createServer(app);
    server.listen(done);
    api = supertest(server);
  });

  let res, userId, token, clothesId;

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

  it.skip('should have correct response for GET /api/users/me', async () => {
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
  const invalidCity = {
    name: 'NEW TESTING',
    email: 'newtesting@newtesting.com',
    city: 'a',
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

  it.skip('should have correct response for PUT /api/users/me', async () => {
    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bearer ${token}`)
      .send(invalidProfile);
    expect(res.statusCode).toEqual(422);
    expect(res.body.message).toEqual(
      'Invalid inputs passed, please check your data.'
    );

    res = await api
      .put('/api/users/me')
      .set('Authorization', `Bearer ${token}`)
      .send(invalidCity);
    expect(res.statusCode).toEqual(500);
    expect(res.body.message).toEqual(
      'Cannot find your city, please check and try again'
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

  it.skip('should have correct response for GET /api/weather/', async () => {
    res = await api.get('/api/weather').set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toEqual(200);
    expect(res.body.current).toBeDefined();
    expect(res.body.today).toBeDefined();
    expect(res.body.tomorrow).toBeDefined();
  });

  const validDateMonth = 'Nov-2020';
  const validDateDay = 'Nov-2020-20';
  const emptyDate = null;
  const invalidDate = 'Nov-2020-20-extra';
  const validCode =
    '4/1AfDhmrh-4Hp4mrkHhTVdZlYSqwwvwZd3U0oK87Uz-mIfvl9LSx1zUST2CWM';
  const emptyCode = null;

  it.skip('should have correct response for POST /api/calendar/:date', async () => {
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
    expect(res.body.length).toEqual(31);

    res = await api
      .post(`/api/calendar/${validDateDay}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ code: validDateMonth });
    expect(res.statusCode).toEqual(200);
    expect(res.body.length).toEqual(1);
  });

  const registrationToken = 'sampleToken';
  const correctMessageFormat = {
    notification: {
      title: 'test',
      body: 'Welcome to Closet!',
    },
  };
  const incorrectMessageFormat = {
    message: 'I am a message',
  };

  it.skip('should have correct response for POST /api/notifications/', async () => {
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

    res = await api
      .post('/api/notifications')
      .set('Authorization', `Bearer ${token}`)
      .send({
        registrationToken: registrationToken,
        message: correctMessageFormat,
      });
    expect(res.statusCode).toEqual(200);
    expect(res.body.message).toEqual('Notification sent successfully!');
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
      .set('Authorization', 'Bear INVALID')
      .send(testClothes);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Authentication failed!');

    res = await api
      .get(`/api/clothes/${null}`)
      .set('Authorization', `Bear ${token}`);
    expect(res.statusCode).toEqual(401);
    expect(res.body.message).toEqual('Token missing or invalid');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bear ${token}`)
      .send(emptyClothes);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing parameters');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bear ${token}`)
      .send(emptyValueClothes);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Missing clothes values');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bear ${token}`)
      .send(invalidOccasions);
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual('Invalid occasions; should be an array');

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bear ${token}`)
      .send(invalidSeasons);
    const seasonList = ['Spring', 'Summer', 'Fall', 'Winter', 'All'];
    expect(res.statusCode).toEqual(400);
    expect(res.body.message).toEqual(
      `Invalid seasons; can only include ${seasonList}`
    );

    res = await api
      .post(`/api/clothes/${userId}`)
      .set('Authorization', `Bear ${token}`)
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

  // it('should have correct response for ', async () => {});
  // it('should have correct response for ', async () => {});

  afterAll(async done => {
    await mongoose.connection.db.dropDatabase();
    await mongoose.connection.close();
    server.close(done);
  });
});
