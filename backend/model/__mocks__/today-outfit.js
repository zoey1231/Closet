let outfits = [];

class TodayOutfit {
  constructor(outfit) {
    outfits.push(outfit);
  }

  save() {
    return Promise.resolve('saved');
  }

  static find() {
    return Promise.resolve(outfits);
  }

  static deleteMany() {
    outfits = [];
  }
}

module.exports = TodayOutfit;
