const axios = require('axios');

require('dotenv').config();
const LOG = require('../utils/logger');
const { timestampToDate } = require('../utils/weather-helper');

const getWeatherInfo = async place => {
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

  const today_date = timestampToDate(current.dt);
  const tomorrow_date = timestampToDate(daily[1].dt);

  return {
    success: true,
    today: { ...current, dt: today_date, units: 'degree Celsius' },
    tomorrow: {
      ...daily[1],
      dt: tomorrow_date,
      units: 'degree Celsius',
    },
  };
};

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
