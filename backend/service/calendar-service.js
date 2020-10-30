const fs = require('fs');
const { google } = require('googleapis');

require('dotenv').config();
const { getDaysInMonth } = require('../utils/time-helper');

let dateMin;
let dateMax;
let givenCode;

// The file token.json stores the user's access and refresh tokens, and is
// created automatically when the authorization flow completes for the first
// time.
const TOKEN_PATH = 'token.json';

const getCalendarEvents = async (calendar_date, code) => {
  // Verify input date
  const time = calendar_date.split('-');
  if (time.length < 2) {
    return {
      success: false,
      reason: process.env.CALENDAR_DATE_ERROR,
    };
  }

  const timeString = calendar_date.replace('-', ' ');
  const days = getDaysInMonth(time[0], time[1]);

  if (time.length > 3 || days === -1) {
    return {
      success: false,
      reason: process.env.CALENDAR_DATE_ERROR,
    };
  } else if (time.length == 2) {
    dateMin = `${timeString} ${process.env.START_DATE_IN_MONTH}`;
    dateMax = `${timeString} ${days}`;
  } else if (time.length == 3) {
    dateMin = timeString;
    dateMax = timeString;
  }

  // Save the given code for later use
  givenCode = code;

  // Load client secrets from a local file.
  let content;
  try {
    content = fs.readFileSync('credentials.json');
  } catch (err) {
    console.log('Error loading client secret file:', err);
    return {
      success: false,
      reason: process.env.CALENDAR_FILE_ERROR,
    };
  }

  // Google Authentication
  const response = await authorize(JSON.parse(content));
  if (!response.success) {
    return response;
  }

  const { events } = response;
  return {
    success: true,
    events,
  };
};

/**
 * Create an OAuth2 client with the given credentials, and then execute the
 * given callback function.
 * @param {Object} credentials The authorization client credentials.
 * @param {function} callback The callback to call with the authorized client.
 */
const authorize = async credentials => {
  const { client_secret, client_id, redirect_uris } = credentials.installed;
  const oAuth2Client = new google.auth.OAuth2(
    client_id,
    client_secret,
    redirect_uris[0]
  );

  // Check if we have previously stored a token.
  let token;
  try {
    token = fs.readFileSync(TOKEN_PATH);
  } catch (err) {
    console.log(err);
    return getAccessToken(oAuth2Client);
  }

  oAuth2Client.setCredentials(JSON.parse(token));
  return listEvents(oAuth2Client);
};

/**
 * Get and store new token after prompting for user authorization, and then
 * execute the given callback with the authorized OAuth2 client.
 * @param {google.auth.OAuth2} oAuth2Client The OAuth2 client to get token for.
 * @param {getEventsCallback} callback The callback for the authorized client.
 */
const getAccessToken = async oAuth2Client => {
  let response;
  try {
    response = await oAuth2Client.getToken(givenCode);
  } catch (err) {
    console.error('Error retrieving access token', err);
    return {
      success: false,
      reason: process.env.CALENDAR_CODE_ERROR,
    };
  }

  const { tokens } = response;

  oAuth2Client.setCredentials(tokens);

  try {
    fs.writeFileSync(TOKEN_PATH, JSON.stringify(tokens));
  } catch (err) {
    console.log(err);
    return {
      success: false,
      reason: process.env.CALENDAR_FILE_ERROR,
    };
  }

  return listEvents(oAuth2Client);
};

/**
 * Lists the next 10 events on the user's primary calendar.
 * @param {google.auth.OAuth2} auth An authorized OAuth2 client.
 */
const listEvents = async auth => {
  const calendar = google.calendar({ version: 'v3', auth });

  let response;
  try {
    response = await calendar.events.list({
      calendarId: 'primary',
      timeMin: new Date(`${dateMin} 00:00`).toISOString(),
      timeMax: new Date(`${dateMax} 23:59`).toISOString(),
      singleEvents: true,
      orderBy: 'startTime',
    });
  } catch (err) {
    console.log('Error loading calendar events:', err);
    return {
      success: false,
      reason: process.env.CALENDAR_EVENTS_ERROR,
    };
  }

  const { items } = response.data;

  return {
    success: true,
    events: items,
  };
};

module.exports = { getCalendarEvents };
