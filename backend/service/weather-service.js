const axios = require('axios');

require('dotenv').config();
const User = require('../model/user');
const LOG = require('../utils/logger');
const { timestampToDate } = require('../utils/time-helper');

/**
 * Return the weather of the current and next day of the user's city
 *
 * @param {String} userId
 */
const getWeatherInfo = async userId => {
  let user;
  try {
    user = await User.findById(userId);
  } catch (err) {
    return {
      success: false,
      code: 500,
      message: 'Could not get your information, please try again later',
    };
  }

  const lat = user.lat;
  const lon = user.lng;
  const units = process.env.WEATHER_UNITS;
  const exclude = process.env.WEATHER_EXCLUDE;
  const appid = process.env.WEATHER_API_KEY;

  let response;
  try {
    response = await axios.get(
      'https://api.openweathermap.org/data/2.5/onecall',
      {
        params: {
          lat,
          lon,
          units,
          exclude,
          appid,
        },
      }
    );
  } catch (err) {
    LOG.error(err.message);
    return {
      success: false,
      code: 500,
      message:
        'Could not get weather information in your city, please try again later',
    };
  }

  const { current, daily } = response.data;

  /* Convert the timestamp to a more readable format,
     The timestamp from OpenWeather APi is in seconds, so we need to convert it into milliseconds
  */
  const current_time = timestampToDate(current.dt * 1000);
  const today_time = timestampToDate(daily[0].dt * 1000);
  const tomorrow_time = timestampToDate(daily[1].dt * 1000);

  return {
    success: true,
    current: {
      ...current,
      units: 'degree Celsius',
      time: current_time,
    },
    today: {
      ...daily[0],
      units: 'degree Celsius',
      time: today_time,
    },
    tomorrow: {
      ...daily[1],
      units: 'degree Celsius',
      time: tomorrow_time,
    },
  };
};

/**
 * Return the latitude and longitude of the input place
 *
 * @param {String} place
 */
const getGeoCode = async place => {
  const q = place;
  const key = process.env.GEO_API_KEY;

  let response;
  try {
    response = await axios.get('https://api.opencagedata.com/geocode/v1/json', {
      params: {
        q,
        key,
      },
    });
  } catch (err) {
    LOG.error(err.message);

    return {
      success: false,
      code: 500,
      message: 'Cannot find your city, please check and try again',
    };
  }

  const { results } = response.data;
  const { lat, lng } = results[0].geometry;

  return {
    success: true,
    lat,
    lon: lng,
  };
};

module.exports = {
  getWeatherInfo,
  getGeoCode,
};
