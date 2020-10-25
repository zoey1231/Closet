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

/**
 * Input a Unix Timestamp and return the time in 'YYYY-MMM-DD' format
 *
 * @param {String | Number} timestamp
 */
const timestampToDate = timestamp => {
  const time = new Date(timestamp * 1000);
  const year = time.getFullYear();
  const month = months[time.getMonth()];
  const day = time.getDate();
  const date = `${year}-${month}-${day}`;
  return date;
};

module.exports = {
  timestampToDate,
};
