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

const dayOfWeek = ['Sun', 'Mon', 'Tue', 'Wen', 'Thu', 'Fri', 'Sat'];

/**
 * Input a Unix Timestamp and return the following values
 *    - Year
 *    - Month values (includes month description, month index, month number)
 *    - Date
 *    - Day of week (includes day description, day index, day number)
 *
 * @param {String | Number} timestamp
 */
const timestampToDate = timestamp => {
  const time = new Date(timestamp * 1000);

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

module.exports = {
  timestampToDate,
};
