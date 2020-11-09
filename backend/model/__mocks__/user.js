const userDB = [];

class User {
  constructor(user) {
    this.id = userDB.length + 1;
    this.name = user.name;
    this.email = user.email;
    this.password = user.password;
    this.clothes = user.clothes;
  }

  save() {
    const index = userDB.findIndex(user => user.email === this.email);
    if (index !== -1) {
      return Promise.reject('Reject!');
    }

    userDB.push({
      id: this.id,
      name: this.name,
      email: this.email,
      password: this.password,
      clothes: this.clothes,
    });
    return Promise.resolve('Saved!');
  }

  static findOne(input) {
    const user = userDB.find(user => user.email === input.email);
    return Promise.resolve(user);
  }
}

module.exports = User;
