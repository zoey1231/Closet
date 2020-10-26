require('dotenv').config();
const HttpError = require('../model/http-error');
const { getWeatherInfo } = require('../service/weather-service');
const LOG = require('../utils/logger');

const getWeather = async (req, res, next) => {
  const { place } = req.params;

  let response;
  try {
    response = await getWeatherInfo(place);
  } catch (err) {
    LOG.error(err);
    return next(
      new HttpError(
        'Could not get weather information, please try again later',
        500
      )
    );
  }

  if (!response.success) {
    return next(
      new HttpError(
        'Could not get weather information, please try again later',
        500
      )
    );
  }

  const { current, today, tomorrow } = response;
  res.status(200).json({ current, today, tomorrow });
};

exports.getWeather = getWeather;
