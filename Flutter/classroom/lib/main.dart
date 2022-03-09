import 'package:flutter/material.dart';
import 'dart:convert';
import 'dart:async';

import 'package:flutter/services.dart';

import 'Utils.dart';

import 'package:alicloud_impinteraction_classroom/alicloud_impinteraction_classroom.dart';

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
    String initResult;
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

      initResult = await AlicloudImpinteractionClassroom.init(param) ??
          'Unknown init result';
    } on Exception {
      initResult = 'Failed to init.';
    }

    if (!mounted) return;

    setState(() {
      _initResult = initResult;
    });
  }

  Future<void> setUp() async {
    String value;

    String inputId = sceneIdController.text;
    String? sceneId = inputId.isEmpty ? demoParam['classId'] : inputId;
    if (sceneId?.isEmpty ?? true) {
      Utils.showToast('empty input');
      return;
    }

    try {
      var param = {
        'classId': sceneId ?? '',
      };
      value = await AlicloudImpinteractionClassroom.setUp(param) ??
          'Unknown setUp result';
    } on Exception {
      value = 'Failed to setUp';
    }
    setState(() {
      _setUpResult = value;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('AliCloud vPaaS Classroom demo app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              TextField(
                onSubmitted: (value) {
                  setUp();
                },
                controller: sceneIdController,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  hintText: 'Enter a valid classId',
                ),
              ),
              ElevatedButton(
                child: const Text('Enter Class'),
                onPressed: setUp,
              ),
              Text('Init result: $_initResult\n'),
              Text('SetUp result: $_setUpResult\n'),
            ],
          ),
        ),
      ),
    );
  }
}
