if ! [ $1 ]; then
  ./gradlew clean assembleDebug
fi

adb install -d -r app/build/outputs/apk/debug/app-debug.apk

sleep 1s
adb shell am start com.aliyun.liveroom.demo/.MainActivity