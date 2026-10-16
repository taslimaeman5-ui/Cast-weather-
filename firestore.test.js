const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.doc(`users/${ALICE_UID}`).get());
});

test("Unauthenticated user: cannot write favorite cities", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.doc(`users/${ALICE_UID}/favorite_cities/city_1`).set({
      id: "city_1",
      userId: ALICE_UID,
      name: "Tokyo",
      country: "Japan",
      latitude: 35.6762,
      longitude: 139.6503,
      addedAt: new Date(),
    })
  );
});

test("Cross-user isolation: Alice cannot read Bob's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.doc(`users/${BOB_UID}`).get());
});

test("Cross-user isolation: Alice cannot create favorite cities for Bob", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.doc(`users/${BOB_UID}/favorite_cities/city_1`).set({
      id: "city_1",
      userId: BOB_UID,
      name: "Paris",
      country: "France",
      latitude: 48.8566,
      longitude: 2.3522,
      addedAt: new Date(),
    })
  );
});

test("Authenticated owner: Alice can create and read her own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}`).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      temperatureUnit: "CELSIUS",
      themeMode: "DARK",
      preferGpsLocation: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.doc(`users/${ALICE_UID}`).get());
});

test("Authenticated owner: Alice can add and list her favorite cities", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/favorite_cities/city_tokyo`).set({
      id: "city_tokyo",
      userId: ALICE_UID,
      name: "Tokyo",
      country: "Japan",
      latitude: 35.6762,
      longitude: 139.6503,
      addedAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.collection(`users/${ALICE_UID}/favorite_cities`).get());
});
