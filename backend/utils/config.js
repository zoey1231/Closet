require("dotenv").config();

console.log(`
 ::::::::  :::         ::::::::   ::::::::  :::::::::: ::::::::::: 
:+:    :+: :+:        :+:    :+: :+:    :+: :+:            :+:     
+:+        +:+        +:+    +:+ +:+        +:+            +:+     
+#+        +#+        +#+    +:+ +#++:++#++ +#++:++#       +#+     
+#+        +#+        +#+    +#+        +#+ +#+            +#+     
#+#    #+# #+#        #+#    #+# #+#    #+# #+#            #+#     
 ########  ##########  ########   ########  ##########     ###     `)

let VERSION = process.env.npm_package_version;
console.log(`===== environment:${process.env.NODE_ENV} version:${VERSION} =====\n`);

let PORT = process.env.PORT;
let MONGODB_URI = process.env.MONGODB_URI;
let REDIS_URI = process.env.REDIS_URI;

if (process.env.NODE_ENV === "test" || process.env.NODE_ENV === "development") {
  MONGODB_URI = process.env.TEST_MONGODB_URI;
  REDIS_URI = process.env.TEST_REDIS_URI;
}

if (process.env.NODE_ENV === "docker") {
  MONGODB_URI = process.env.DOCKER_MONGODB_URI;
  REDIS_URI = process.env.DOCKER_REDIS_URI;
}

console.log("#️⃣PORT:", PORT);
console.log("#️⃣MONGODB_URI:", MONGODB_URI);
console.log("#️⃣REDIS_URI:", REDIS_URI);


module.exports = {
  PORT,
  MONGODB_URI,
  REDIS_URI,
  VERSION
};
