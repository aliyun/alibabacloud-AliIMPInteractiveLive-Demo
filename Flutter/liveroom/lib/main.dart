import 'package:flutter/material.dart';
import 'dart:convert';
import 'dart:async';

import 'package:flutter/services.dart';

import 'package:alicloud_impinteraction_liveroom/alicloud_impinteraction_liveroom.dart';

import 'Utils.dart';

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
  String _setUpResult = 'Unknown';
  final sceneIdController = TextEditingController();

  var appSettings;
  var demoParam;

  @override
  void initState() {
    super.initState();
    initPlugin();
  }

  Future<void> initPlugin() async {
    String initResult = 'unknown';
    try {
      final String settingJson =
      await rootBundle.loadString('assets/app_settings.json');
      appSettings = await json.decode(settingJson);
      String userId = (appSettings['userId'] as String).isEmpty ? Utils.randomName() : appSettings['userId'];

      final String paramJson =
      await rootBundle.loadString('assets/demo_param.json');
      demoParam = await json.decode(paramJson);

      var param = {
        'userId':  userId,
        'appId': appSettings['appId'],
        'appKey4Android': appSettings['appKey4Android'],
        'appKey4iOS': appSettings['appKey4iOS'],
        'serverHost': appSettings['serverHost'],
        'serverSecret': appSettings['serverSecret'],
      };

      initResult = await AlicloudImpinteractionLiveroom.init(param) ??
          'Unknown init result';
    } on Exception {
      initResult = 'Failed to init.';
    }

    if (!mounted) return;

    setState(() {
      _initResult = initResult;
    });
  }

  Future<void> setUp(String? sceneId, String? role) async {
    String value = 'unknown';
    try {
      var param = {
        'liveId': sceneId ?? '',
        'role': role ?? '',
      };
      value = await AlicloudImpinteractionLiveroom.setUp(param) ??
          'Unknown setUp result';
    } on Exception {
      value = 'Failed to setUp';
    }
    setState(() {
      _setUpResult = value;
    });
  }

  void setUpAsAnchor() {
    setUp(null, null);
  }

  void setUpAsAudience() {
    String inputId = sceneIdController.text;
    String? sceneId = inputId.isEmpty ? demoParam['liveId'] : inputId;
    if (sceneId?.isEmpty ?? true) {
      Utils.showToast('empty input');
    } else {
      setUp(sceneId, 'audience');
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('AliCloud vPaaS LiveRoom demo app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              ElevatedButton(
                child: const Text('Start Live as Anchor'),
                onPressed: setUpAsAnchor,
              ),
              TextField(
                onSubmitted: (value) {
                  setUpAsAudience();
                },
                controller: sceneIdController,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  hintText: 'Enter a valid liveId',
                ),
              ),
              ElevatedButton(
                child: const Text('Enter Live as Audience'),
                onPressed: setUpAsAudience,
              ),
              Text('init result: $_initResult\n'),
              Text('setUp result: $_setUpResult\n'),
            ],
          ),
        ),
      ),
    );
  }
}
