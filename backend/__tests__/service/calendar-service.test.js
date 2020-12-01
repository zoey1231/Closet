const fs = require('fs');
const { google } = require('googleapis');

const { getDaysInMonth } = require('../../utils/time-helper');

const { getCalendarEvents } = require('../../service/calendar-service');

jest.mock('../../utils/time-helper');

describe('Calendar service with mocking', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('file system exception: readFileSync', async () => {
    jest.spyOn(fs, 'readFileSync').mockImplementationOnce(() => {
      throw "Error!!!";
    });

    getDaysInMonth.mockImplementationOnce(() => {
      return 0;
    });

    const result = await getCalendarEvents("1-2", "CODE");
    expect(result.success).toEqual(false);
    expect(result.reason).toEqual("CALENDAR_FILE_ERROR");
  });

  it('authorize exception: event error', async () => {
    jest.spyOn(fs, 'readFileSync').mockImplementationOnce(() => {
      return JSON.stringify({
        installed: {
          client_secret: "1",
          client_id: "2",
          redirect_uris: "3"
        }
      });
    });

    getDaysInMonth.mockImplementationOnce(() => {
      return 0;
    });

    const result = await getCalendarEvents("1-2", "CODE");
    expect(result.success).toEqual(false);
    expect(result.reason).toEqual("CALENDAR_EVENTS_ERROR");
  });

  it('authorize exception: code error', async () => {
    jest.spyOn(fs, 'readFileSync').mockImplementationOnce(() => {
      return JSON.stringify({
        installed: {
          client_secret: "1",
          client_id: "2",
          redirect_uris: "3"
        }
      });
    });

    getDaysInMonth.mockImplementationOnce(() => {
      return 0;
    });

    jest.spyOn(fs, 'readFileSync').mockImplementationOnce(() => {
      throw "Error!!!"
    });

    const result = await getCalendarEvents("1-2", "CODE");
    expect(result.success).toEqual(false);
    expect(result.reason).toEqual("CALENDAR_CODE_ERROR");
  });

  it('authorize exception: file error', async () => {
    jest.spyOn(google.auth, 'OAuth2').mockImplementationOnce(() => {
      return {
        getToken: jest.fn().mockImplementation(() => { return { tokens: "TOKENS" } }),
        setCredentials: jest.fn()
      }
    })

    jest.spyOn(fs, 'readFileSync').mockImplementationOnce(() => {
      return JSON.stringify({
        installed: {
          client_secret: "1",
          client_id: "2",
          redirect_uris: "3"
        }
      });
    });

    getDaysInMonth.mockImplementationOnce(() => {
      return 0;
    });

    jest.spyOn(fs, 'readFileSync').mockImplementationOnce(() => {
      throw "Error!!!"
    });

    jest.spyOn(fs, 'writeFileSync').mockImplementationOnce(() => {
      throw "Error!!!"
    });

    const result = await getCalendarEvents("1-2", "CODE");
    expect(result.success).toEqual(false);
    expect(result.reason).toEqual("CALENDAR_FILE_ERROR");
  });

  it('authorize exception: event error', async () => {
    jest.spyOn(google.auth, 'OAuth2').mockImplementationOnce(() => {
      return {
        getToken: jest.fn().mockImplementation(() => { return { tokens: "TOKENS" } }),
        setCredentials: jest.fn()
      }
    })

    jest.spyOn(fs, 'readFileSync').mockImplementationOnce(() => {
      return JSON.stringify({
        installed: {
          client_secret: "1",
          client_id: "2",
          redirect_uris: "3"
        }
      });
    });

    getDaysInMonth.mockImplementationOnce(() => {
      return 0;
    });

    jest.spyOn(fs, 'readFileSync').mockImplementationOnce(() => {
      throw "Error!!!"
    });

    jest.spyOn(fs, 'writeFileSync').mockImplementationOnce(() => {
      return 0;
    });

    const result = await getCalendarEvents("1-2", "CODE");
    expect(result.success).toEqual(false);
    expect(result.reason).toEqual("CALENDAR_EVENTS_ERROR");
  });

})