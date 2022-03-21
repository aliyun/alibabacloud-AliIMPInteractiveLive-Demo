if ! [ $1 ]; then
  ./gradlew clean assembleDebug -PwithGradlePropertyAppId=true
fi

adb install -d -r app/build/outputs/apk/debug/app-debug.apk