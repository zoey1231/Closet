const months = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
];

const dayOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

const daysInMonth = [
  { month: 'Jan', days: 31 },
  { month: 'Feb', days: { leap: 29, normal: 28 } },
  { month: 'Mar', days: 31 },
  { month: 'Apr', days: 30 },
  { month: 'May', days: 31 },
  { month: 'Jun', days: 30 },
  { month: 'Jul', days: 31 },
  { month: 'Aug', days: 31 },
  { month: 'Sep', days: 30 },
  { month: 'Oct', days: 31 },
  { month: 'Nov', days: 30 },
  { month: 'Dec', days: 31 },
];

/**
 * Input a Unix Timestamp (in milliseconds) and return the following values
 *    - Year
 *    - Month values (includes month description, month index, month number)
 *    - Date
 *    - Day of week (includes day description, day index, day number)
 *
 * @param {String | Number} timestamp
 */
const timestampToDate = timestamp => {
  const time = new Date(timestamp);

  const year = time.getFullYear();
  const monthDesc = months[time.getMonth()];
  const monthIndex = time.getMonth();
  const monthNumber = time.getMonth() + 1;
  const date = time.getDate();
  const dayDesc = dayOfWeek[time.getDay()];
  const dayIndex = time.getDay();
  const dayNumber = time.getDay() + 1;

  return {
    year,
    month: {
      monthDesc,
      monthIndex,
      monthNumber,
    },
    date,
    day: {
      dayDesc,
      dayIndex,
      dayNumber,
    },
  };
};

/**
 * Return the number of days in a month by the given year and month
 *
 * @param {String} month
 * @param {String | Number} year
 */
const getDaysInMonth = (month, year) => {
  const target = daysInMonth.find(item => item.month === month);

  if (!target) {
    return -1;
  }

  const y = parseInt(year);
  if (month === 'Feb') {
    const isLeapYear = y % 400 === 0 || (y % 4 === 0 && y % 100 !== 0);
    if (isLeapYear) {
      return target.days.leap;
    } else {
      return target.days.normal;
    }
  } else {
    return target.days;
  }
};

module.exports = {
  timestampToDate,
  getDaysInMonth,
};
