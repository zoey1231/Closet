const { getWeatherInfo } = require('../../service/weather-service');

const axios = require('axios');
const User = require('../../model/user');

jest.mock('../../model/user')

describe('Weather service with mocking', () => {
  it('getWeatherInfo expect axios exception', async () => {
    jest.spyOn(User, 'findById').mockImplementationOnce(() => {
      return {
        lat: "LAT",
        lon: "LON"
      }
    });

    jest.spyOn(axios, 'get').mockImplementationOnce(() => {
      throw "Error!!!"
    });

    const result = await getWeatherInfo("USERID");
    expect(result.success).toEqual(false);
    expect(result.code).toEqual(500);
    expect(result.message).toEqual("Could not get weather information in your city, please try again later");
  })
})
