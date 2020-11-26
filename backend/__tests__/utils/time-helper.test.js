const {
  timestampToDate,
  getDaysInMonth,
  getTodayDateInTimezone,
} = require('../../utils/time-helper');

describe('Time stamp to date tests', () => {
  let timestamp;

  it('should return the correct date information', () => {
    timestamp = 1605470892000;
    const date = timestampToDate(timestamp);
    const expectedDate = {
      year: 2020,
      month: {
        monthDesc: 'Nov',
        monthIndex: 10,
        monthNumber: 11,
      },
      date: 15,
      day: {
        dayDesc: 'Sun',
        dayIndex: 0,
        dayNumber: 1,
      },
    };
    expect(date).toMatchObject(expectedDate);
  });
});

describe('Get days in months tests', () => {
  let month;
  let year;

  it('should return -1 if the input is invalid', () => {
    month = 'invalid';
    year = 2020;
    const days = getDaysInMonth(month, year);
    expect(days).toEqual(-1);
  });

  it('should return the correct number if the input is valid', () => {
    month = 'Jan';
    year = 2019;
    const days = getDaysInMonth(month, year);
    expect(days).toEqual(31);
  });

  it('should return the correct number for Feb if the year is a leap year', () => {
    month = 'Feb';
    year = 2020;
    const days = getDaysInMonth(month, year);
    expect(days).toEqual(29);
  });

  it('should return the correct number for Feb if the year is not a leap year', () => {
    month = 'Feb';
    year = 500;
    const days = getDaysInMonth(month, year);
    expect(days).toEqual(28);
  });
});

describe('Get today date in timezone test', () => {
  it('should return the correct date', () => {
    const dateString = getTodayDateInTimezone();

    const date = dateString.split('-');
    expect(date[0]).toEqual(new Date().getFullYear().toString());
    expect(date[1]).toEqual((new Date().getMonth() + 1).toString());
    expect(date[2]).toEqual(new Date().getDate().toString());
  });
});
