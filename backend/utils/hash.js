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

module.exports = {
  hashCode,
};
