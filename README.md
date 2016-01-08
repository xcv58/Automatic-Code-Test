[![Circle CI](https://circleci.com/gh/xcv58/Automatic-Code-Test/tree/master.svg?style=svg)](https://circleci.com/gh/xcv58/Automatic-Code-Test/tree/master)

# Connected Test
## Setup
To run `connectedAndroidTest`, you **MUST** set properties: `TOKEN` and `INVALID_TOKEN`.
There're several ways to set them:

a. Edit/add file `gradle.properties` in the `./` or `./app/` directory. And add the following lines:
```
TOKEN=e5cdd2a2f2c52ac2ff9825f53ac566f45c513991
INVALID_TOKEN=e5cdd2a2f2c52ac2ff9825f53ac566f45c513991
```

b. Use environment variables:
```
export ORG_GRADLE_PROJECT_TOKEN=e5cdd2a2f2c52ac2ff9825f53ac566f45c513991
export ORG_GRADLE_PROJECT_INVALID_TOKEN=e5cdd2a2f2c52ac2ff9825f53ac566f45c513991
```

For all methods to setup `TOKEN`, you **MUST** replace token content to your correct token.

## Run
Then use below command to run test:
```
./gradlew cAT
```
or
```
./gradlew connectedAndroidTest
```
