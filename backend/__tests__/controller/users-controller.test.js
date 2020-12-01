const { signup, login, getUserProfile, updateUserProfile } = require('../../controller/users-controllers')

const { validationResult } = require('express-validator');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

jest.mock('../../model/user');
const User = require('../../model/user');

const { getGeoCode } = require('../../service/weather-service');
jest.mock('../../service/weather-service');

describe('User controller signup with mocking', () => {
  it('signup expect bcrypt exception', () => {
    jest.spyOn(bcrypt, 'hash').mockImplementationOnce(() =>
      Promise.reject(new Error('ERROR!'))
    );

    const req = {
      body: {
        name: "NAME",
        email: "EMAIL",
        password: "PASSWORD"
      }
    }

    const result = signup(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('signup expect database exception', () => {
    jest.spyOn(bcrypt, 'hash').mockImplementationOnce(() =>
      Promise.resolve()
    );

    jest.spyOn(User.prototype, 'save').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const req = {
      body: {
        name: "NAME",
        email: "EMAIL",
        password: "PASSWORD"
      }
    }

    const result = signup(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('signup expect jwt exception', () => {
    jest.spyOn(bcrypt, 'hash').mockImplementationOnce(() =>
      Promise.resolve()
    );

    jest.spyOn(User.prototype, 'save').mockImplementationOnce(() => {
      Promise.resolve()
    });

    jest.spyOn(jwt, 'sign').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const req = {
      body: {
        name: "NAME",
        email: "EMAIL",
        password: "PASSWORD"
      }
    }

    const result = signup(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });
});

describe('User controller login with mocking', () => {
  it('login expect database exception', () => {
    jest.spyOn(User, 'findOne').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const req = {
      body: {
        email: "EMAIL",
        password: "PASSWORD"
      }
    }

    const result = login(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('login expect database return null', () => {
    jest.spyOn(User, 'findOne').mockImplementationOnce(() => {
      Promise.resolve(null);
    });

    const req = {
      body: {
        email: "EMAIL",
        password: "PASSWORD"
      }
    }

    const result = login(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('login expect bcrypt exception', () => {
    jest.spyOn(User, 'findOne').mockImplementationOnce(() => {
      return "USER";
    });

    jest.spyOn(bcrypt, 'compare').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const req = {
      body: {
        email: "EMAIL",
        password: "PASSWORD"
      }
    }

    const result = login(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('login expect jwt exception', () => {
    jest.spyOn(User, 'findOne').mockImplementationOnce(() => {
      return "USER";
    });

    jest.spyOn(bcrypt, 'compare').mockImplementationOnce(() => {
      return "VALID_PASSWORD"
    });

    jest.spyOn(jwt, 'sign').mockImplementationOnce(() => {
      throw 'Error!!!'
    });

    const req = {
      body: {
        email: "EMAIL",
        password: "PASSWORD"
      }
    }

    const result = login(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });
});

describe('User controller user profile with mocking', () => {
  it('getUserProfile expect database exception', () => {
    jest.spyOn(User, 'findById').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const req = {
      userData: {
        userId: "USERID"
      }
    }

    const result = getUserProfile(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('updateUserProfile expect getGeoCode exception', () => {
    getGeoCode.mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const req = {
      userData: {
        userId: "USERID"
      },
      body: {
        name: "NAME",
        email: "EMAIL",
        city: "CITY"
      }
    }

    const result = updateUserProfile(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('updateUserProfile expect getGeoCode failed', () => {
    getGeoCode.mockImplementationOnce(() => {
      return {
        success: false,
        code: "CODE",
        message: "MESSAGE"
      }
    });

    const req = {
      userData: {
        userId: "USERID"
      },
      body: {
        name: "NAME",
        email: "EMAIL",
        city: "CITY"
      }
    }

    const result = updateUserProfile(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('updateUserProfile expect database exception', () => {
    getGeoCode.mockImplementationOnce(() => {
      return {
        success: true,
        lat: "LAT",
        lon: "LON"
      }
    });

    jest.spyOn(User, 'findOneAndUpdate').mockImplementationOnce(() => {
      throw 'Error!!!';
    });

    const req = {
      userData: {
        userId: "USERID"
      },
      body: {
        name: "NAME",
        email: "EMAIL",
        city: "CITY"
      }
    }

    const result = updateUserProfile(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });
});
