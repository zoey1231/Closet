require('dotenv').config();
const { getCalendarEvents } = require('./calendar-service');
const { getWeatherInfo } = require('./weather-service');
const { timestampToDate } = require('../utils/time-helper');
const LOG = require('../utils/logger');

/*  Get today's weather
      Sample response
        {
          success: true,
          temperature: {
            day: 8.9,
            min: 5.75,
            max: 10.11,
            night: 7.35,
            eve: 6.64,
            morn: 6.15
          },
          weather: 'scattered clouds'
        }
 */
const getTodayWeather = async () => {
  // Hard code place for now (using vancouver)

  let response;
  try {
    response = await getWeatherInfo('vancouver');
  } catch (err) {
    LOG.error(err);
    return {
      success: false,
    };
  }

  const { today } = response;
  const { temp, weather } = today;

  return {
    success: true,
    temperature: temp,
    weather: weather[0].description,
  };
};

/*  Get today's events
      Sample response
        { 
          success: true, 
          events: [ 'Testing meeting' ] 
        }
 */
const getTodayEvents = async () => {
  const time = timestampToDate(Date.now());
  const date = `${time.month.monthDesc}-${time.date}-${time.year}`;

  let response;
  try {
    response = await getCalendarEvents(date);
  } catch (err) {
    LOG.error(err);
    return {
      success: false,
    };
  }

  const { events } = response;
  const eventsSummary = [];
  events.forEach(e => eventsSummary.push(e.summary));

  return {
    success: true,
    events: eventsSummary,
  };
};

module.exports = {
  getTodayWeather,
  getTodayEvents,
};
