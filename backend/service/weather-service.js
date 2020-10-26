const axios = require('axios');

require('dotenv').config();
const LOG = require('../utils/logger');
const { timestampToDate } = require('../utils/time-helper');

/**
 * Return the weather of the current and next day of the given place
 *
 * @param {String} place
 */
const getWeatherInfo = async place => {
  // Get latitude and longitude of the given place
  const geoCode = await getGeoCode(place);

  if (!geoCode.success) {
    return {
      success: false,
      message: 'Could not get weather information, please try again later',
    };
  }

  const { lat, lon } = geoCode;
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
    LOG.error(err);
    return {
      success: false,
      message: 'Could not get weather information, please try again later',
    };
  }

  if (response.status !== 200) {
    return {
      success: false,
      message: 'Could not get weather information, please try again later',
    };
  }

  const { current, daily } = response.data;

  // Convert the timestamp to a more readable format
  const current_time = timestampToDate(current.dt);
  const today_time = timestampToDate(daily[0].dt);
  const tomorrow_time = timestampToDate(daily[1].dt);

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
    LOG.error(err);

    return {
      success: false,
      code: 500,
      message: 'Internal Geo-location called failed',
    };
  }

  const { status, results } = response.data;

  if (status.code !== 200) {
    return {
      success: false,
      ...status,
    };
  } else {
    const { lat, lng } = results[0].geometry;

    return {
      success: true,
      lat,
      lon: lng,
    };
  }
};

module.exports = {
  getWeatherInfo,
};
