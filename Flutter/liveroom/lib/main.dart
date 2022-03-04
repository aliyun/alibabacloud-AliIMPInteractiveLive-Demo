import 'package:flutter/material.dart';
import 'dart:convert';
import 'package:flutter/services.dart';

import 'package:alicloud_impinteraction_liveroom/alicloud_impinteraction_liveroom.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'AliCloud vPaaS LiveRoom',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'AliCloud vPaaS LiveRoom Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String _initResult = 'Unknown';
  String _setUpResult = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlugin();
  }

  Future<void> initPlugin() async {
    String initResult = 'unknown';
    try {
      final String settingJson = await rootBundle.loadString('assets/app_settings.json');
      final String paramJson = await rootBundle.loadString('assets/demo_param.json');
      final appSettings = await json.decode(settingJson);
      final demoParam = await json.decode(paramJson);

      var param = {
        'userId': demoParam['userId'],
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

  Future<void> setUp() async {
    String value = 'unknown';
    try {
      final String paramJson = await rootBundle.loadString('assets/demo_param.json');
      final demoParam = await json.decode(paramJson);

      var param = {
        'liveId': demoParam['liveId'],
        'role': 'anchor',
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
                child: const Text('plugin_usage'),
                onPressed: setUp,
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
