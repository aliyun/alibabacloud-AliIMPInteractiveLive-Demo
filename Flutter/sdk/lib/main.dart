import 'dart:convert';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';

import 'package:alicloud_impinteraction_sdk/event.dart';
import 'package:alicloud_impinteraction_sdk/room_engine.dart';
import 'package:alicloud_impinteraction_sdk/room_channel.dart';
import 'package:alicloud_impinteraction_sdk/chat.dart';
import 'package:alicloud_impinteraction_sdk/utils.dart';
import 'package:alicloud_impinteraction_sdk/live_pusher.dart';
import 'package:alicloud_impinteraction_sdk/live_player.dart';
import 'package:http/http.dart' as http;

import 'Utils.dart';
import 'dart:convert';

typedef Listener = void Function(dynamic msg);
typedef CancelListening = void Function();

Future<M> makePostRequest(String serverHost, String userId, String appId,
    String appKey, String deviceId) async {
  debugPrint('makePostRequest');
  final uri = Uri.parse(
      '$serverHost/api/login/getToken?userId=$userId&deviceId=$deviceId&appId=$appId&appKey=$appKey');

  var response = await http.post(
    uri,
    encoding: Encoding.getByName('utf-8'),
  );

  int statusCode = response.statusCode;
  String responseBody = response.body;
  debugPrint('statusCode = $statusCode, responseBody=$responseBody');

  var tokenJson = await json.decode(responseBody);
  String accessToken = tokenJson['result']['accessToken'];
  String refreshToken = tokenJson['result']['refreshToken'];
  debugPrint('accessToken = $accessToken, refreshToken=$refreshToken');
  return {
    'accessToken': accessToken,
    'refreshToken': refreshToken,
  };
}

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _initResult = 'Unknown';
  String _apiResult = 'Unknown';
  final sceneIdController = TextEditingController();

  var appSettings;
  var demoParam;

  static const ROLE_ANCHOR = 'anchor';
  static const ROLE_AUDIENCE = 'audience';
  var roleChoosen = '';

  var eventChannelCancel;

  void regEvent(String serverHost, String userId, String appId, String appKey,
      String deviceId) async {
    debugPrint('AAA flutter regEvent startListening');
    eventChannelCancel = IMPSdkEvent.startListening((jsonMsg) async {
      debugPrint('AAA flutter IMPSdkEvent = $jsonMsg');
      var msg = await json.decode(jsonMsg);
      String event = msg['event'] ?? '';
      String params = msg['params'] ?? '';
      switch (event) {
        case 'onRequestToken':
          M token = await makePostRequest(
              serverHost, userId, appId, appKey, deviceId);
          var setLoginToken = await IMPSdkRoomEngine.setLoginToken(token);
          debugPrint('setLoginToken=$setLoginToken');
          break;
      }
    });
  }

  @override
  void initState() {
    super.initState();
  }

  Future<void> initPlugin(anchor) async {
    M res = {'result': 'unknown'};
    try {
      String config =
      anchor ? 'app_settings_anchor.json' : 'app_settings_audience.json';
      final String settingJson = await rootBundle.loadString('assets/$config');
      appSettings = await json.decode(settingJson);
      String userId = (appSettings['userId'] as String).isEmpty
          ? Utils.randomName()
          : appSettings['userId'];

      final String paramJson =
      await rootBundle.loadString('assets/demo_param.json');
      demoParam = await json.decode(paramJson);

      String deviceId = await Utils.getId() ?? '';

      var param = {
        'userId': userId,
        'appId': appSettings['appId'],
        'appKey4Android': appSettings['appKey4Android'],
        'appKey4iOS': appSettings['appKey4iOS'],
        'serverHost': appSettings['serverHost'],
        'serverSecret': appSettings['serverSecret'],
        'deviceId': deviceId,
      };

      var appKey = defaultTargetPlatform == TargetPlatform.android
          ? appSettings['appKey4Android']
          : appSettings['appKey4iOS'];
      regEvent(appSettings['tokenServerHost'], userId, appSettings['appId'],
          appKey, deviceId);
      res = await IMPSdkRoomEngine.init(param);
      res = await IMPSdkRoomEngine.login();
      res = await IMPSdkRoomChannel.setRoomId({
        'roomId': demoParam['roomId'],
      });
      res = await IMPSdkRoomChannel.enterRoom({
        'nick': 'nickOf' + userId,
      });

      roleChoosen = appSettings['role'];
      if (roleChoosen == ROLE_ANCHOR) {
        res = await IMPSdkLivePusher.startPreview();
        res = await IMPSdkLivePusher.startLive();
      } else {
        var param = {
          'showMode': 'aspect_fill',
          'lowDelay': 'true',
        };
        res = await IMPSdkLivePlayer.start(param);
      }
    } on PlatformException catch (e) {
      res!['result'] =
      'api e: code=${e.code}, msg=${e.message}, details=${e.details}';
      debugPrint('startPreview result:${res['result']}');
    }

    if (!mounted) return;

    debugPrint('init result:${res!['result']}');
    setState(() {
      _initResult = res!['result'];
    });
  }

  Future<void> toggleBeautyPanel() async {
    M res = {'result': 'unknown'};
    try {
      if (roleChoosen == ROLE_ANCHOR) {
        res = await IMPSdkLivePusher.toggleBeautyPanel();
      } else {
        res['result'] = 'error:only valid for anchor';
      }
    } on PlatformException catch (e) {
      res!['result'] =
      'api e: code=${e.code}, msg=${e.message}, details=${e.details}';
      debugPrint('toggleBeautyPanel result:${res['result']}');
    }

    if (!mounted) return;

    debugPrint('toggleBeautyPanel:${res!['result']}');
    setState(() {
      _initResult = res!['result'];
    });
  }

  Future<void> listComment() async {
    M res = {'result': 'unknown'};
    try {
      var param = {
        'key': '',
      };
      res = await IMPSdkChat.listComment(param);
    } on PlatformException catch (e) {
      res!['result'] =
      'listComment e: code=${e.code}, msg=${e.message}, details=${e.details}';
    }

    debugPrint('listComment result:${res!['result']}');
    setState(() {
      _apiResult = res!['result'];
    });
  }

  Future<void> login() async {
    M res = {'result': 'unknown'};
    try {
      var param = {
        'key': '',
      };
      res = await IMPSdkRoomEngine.login(param);
    } on PlatformException catch (e) {
      res!['result'] =
      'login e: code=${e.code}, msg=${e.message}, details=${e.details}';
    }

    debugPrint('login result:${res!['result']}');
    setState(() {
      _apiResult = res!['result'];
    });
  }

  void setUpAsAnchor() {
    listComment();
    login();
  }

  void setUpAsAudience() {
    String inputId = sceneIdController.text;
    String? sceneId = inputId.isEmpty ? demoParam['liveId'] : inputId;
    if (sceneId?.isEmpty ?? true) {
      debugPrint('empty input');
    } else {
      listComment();
      login();
    }
  }

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = '<platform-view-type>';
    // Pass parameters to the platform side.
    const Map<String, dynamic> creationParams = <String, dynamic>{};
    debugPrint('build:$viewType');

    Widget widget;
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
      // return widget on Android.
        widget = PlatformViewLink(
          viewType: viewType,
          surfaceFactory:
              (BuildContext context, PlatformViewController controller) {
            return AndroidViewSurface(
              controller: controller as AndroidViewController,
              gestureRecognizers: const <
                  Factory<OneSequenceGestureRecognizer>>{},
              hitTestBehavior: PlatformViewHitTestBehavior.opaque,
            );
          },
          onCreatePlatformView: (PlatformViewCreationParams params) {
            return PlatformViewsService.initSurfaceAndroidView(
              id: params.id,
              viewType: viewType,
              layoutDirection: TextDirection.ltr,
              creationParams: creationParams,
              creationParamsCodec: const StandardMessageCodec(),
              onFocus: () {
                params.onFocusChanged(true);
                debugPrint('onFocus:$params');
              },
            )
              ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
              ..create();
          },
        );
        break;
      case TargetPlatform.iOS:
      default:
        widget = const UiKitView(
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: StandardMessageCodec(),
        );
        // return widget on iOS.
        break;
    }

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('AliCloud vPaaS SDK demo app'),
        ),
        body: Stack(
          children: <Widget>[
            widget,
            Positioned.fill(
              child: ButtonBar(
                alignment: MainAxisAlignment.center,
                children: <Widget>[
                  Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      if (roleChoosen.isEmpty) Row(
                        children: [
                          ElevatedButton(
                            onPressed: () {
                              setState(() {
                                roleChoosen = ROLE_ANCHOR;
                              });
                              initPlugin(true);
                            },
                            child: const Icon(Icons.radar),
                            // color: Colors.green,
                          ),
                          const SizedBox(width: 10),
                          const Text(
                            "主播",
                            style: TextStyle(color: Colors.blue),
                          ),
                        ],
                      ),
                      if (roleChoosen.isEmpty) Row(
                        children: [
                          ElevatedButton(
                            onPressed: () {
                              setState(() {
                                roleChoosen = ROLE_AUDIENCE;
                              });
                              initPlugin(false);
                            },
                            child: const Icon(Icons.connected_tv),
                            // color: Colors.red,
                          ),
                          const SizedBox(width: 10),
                          const Text(
                            "观众",
                            style: TextStyle(color: Colors.blue),
                          ),
                        ],
                      ),
                      if (roleChoosen == ROLE_ANCHOR) Row(
                        children: [
                          ElevatedButton(
                            onPressed: () {
                              toggleBeautyPanel();
                            },
                            child: const Icon(Icons.face),
                            // color: Colors.red,
                          ),
                          const SizedBox(width: 10),
                          const Text(
                            "美颜",
                            style: TextStyle(color: Colors.blue),
                          ),
                        ],
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
        // floatingActionButton:
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    if (eventChannelCancel) {
      eventChannelCancel();
    }
  }
}
