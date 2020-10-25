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
