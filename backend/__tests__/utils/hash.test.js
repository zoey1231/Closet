const { hashCode, randomInt } = require('../../utils/hash');

describe('Hash Test', () => {
  it('should return a hash code', () => {
    const hash = hashCode('123123');
    expect(hash).toBeDefined();
  });
});

describe('Random Int Test', () => {
  let seed;

  it('should return a random int if the input is a positive integer', () => {
    seed = 3;
    const r = randomInt(seed);
    expect(r).toBeLessThan(seed);
    expect(r).toBeGreaterThanOrEqual(0);
  });

  it('should return a random int if the input is zero', () => {
    seed = 0;
    const r = randomInt(seed);
    expect(r).toEqual(-1);
  });

  it('should return a random int if the input is a negative integer', () => {
    seed = -10;
    const r = randomInt(seed);
    expect(r).toEqual(-1);
  });
});
