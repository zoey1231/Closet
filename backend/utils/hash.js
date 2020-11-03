/**
 * Java's String.hashCode()
 * https://werxltd.com/wp/2010/05/13/javascript-implementation-of-javas-string-hashcode-method/
 * @param {String} source
 */
const hashCode = source => {
  let hash = 0;
  for (let i = 0; i < source.length; i++) {
    let char = source.charCodeAt(i);
    hash = (hash << 5) - hash + char;
    hash = hash & hash;
  }

  return hash;
};

/**
 * Generate a random number between 0 (inclusive) and max (exclusive)
 * if the max is less than or equal to 0, return -1
 *
 * @param { Number} max  the upper limit of range
 */
const randomInt = max => {
  if (max <= 0) {
    return -1;
  }
  return Math.floor(Math.random() * Math.floor(max));
};

module.exports = {
  hashCode,
  randomInt,
};
