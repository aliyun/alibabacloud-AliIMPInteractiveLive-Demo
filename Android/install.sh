if ! [ $1 ]; then
  echo 'start build..'
  ./gradlew app:clean
  ./gradlew app:assembleDebug --stacktrace --info;
  
fi
echo 'start install..'
adb install -r app/build/outputs/apk/debug/app-debug.apk
