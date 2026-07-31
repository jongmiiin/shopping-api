gradle wrapper --gradle-version 8.5 --distribution-type bin
chmod +x gradlew
./gradlew clean build -x test
