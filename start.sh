kill $(lsof -t -i :18081)
./gradlew clean build -Pprofile=dev -Djava.net.preferIPv4Stack=true --exclude-task test
nohup java -jar build/libs/stream-auth-0.0.1-SNAPSHOT.jar &
