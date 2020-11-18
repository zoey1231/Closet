const { getCalendarEvents } = require('../../service/calendar-service');

describe('Calendar service tests', () => {
  const code = '4/1AfDhmrh-4Hp4mrkHhTVdZlYSqwwvwZd3U0oK87Uz-mIfvl9LSx1zUST2CWM';
  let date;

  it('should return calendar events for a month', async () => {
    date = 'Oct-2020';
    const response = await getCalendarEvents(date, code);
    const { success, events } = response;

    expect(success).toBeTruthy();
    expect(events.length).toEqual(2);
  });

  it('should return calendar events for a day', async () => {
    date = 'Oct-2020-29';
    const response = await getCalendarEvents(date, code);
    const { success, events } = response;

    expect(success).toBeTruthy();
    expect(events.length).toEqual(1);
  });

  it('should return false response if input date is insufficient', async () => {
    date = 'Oct';
    const response = await getCalendarEvents(date, code);
    const { success } = response;

    expect(success).toBeFalsy();
  });

  it('should return false response if input date is invalid', async () => {
    date = 'Oct-2020-29-invalid';
    const response = await getCalendarEvents(date, code);
    const { success } = response;

    expect(success).toBeFalsy();
  });
});
