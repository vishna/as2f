# AS2F - Android Strings To Flutter

## Overview

Kotlin based generator for converting android xml string resources into one i18n dart class that can be used in flutter projects.

Inspired by [flutter_i18n](https://github.com/long1eu/flutter_i18n) project by long1eu except no need to use IntelliJ or arb files.

## Usage in Dart/Flutter Project

```yaml
dev_dependencies:
 as2f: ^0.0.2
```

Then run the following command.

```
flutter packages pub run as2f:codegen
```

__NOTE__: You can add `--run-once` and `--dry-run` parameters to the above command.

__NOTE__: You can running `pub run as2f:codegen` in the example folder to play with the `carrots_sample` 🥕🥕

This fetches the jar from [jitpack](https://jitpack.io/#vishna/as2f) containing the code generator and executes it in a watch mode. It will bootstrap `as2f.yaml` file for you. This file describes where your Android string files are and where the generated class should go.

```
- name: android_strings # base name
  source: someplace/main/res # lookup place for all values-lang/strings.xml files
  target: lib/gen/i18n.dart # place where your i18n class should go
```

The structure of the generated class is much like the one by [flutter_i18n](https://github.com/long1eu/flutter_i18n#usage) meaning you can just follow their setup intructions:

```dart
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      onGenerateTitle: (BuildContext context) => S.of(context).app_name,
      localizationsDelegates: const <LocalizationsDelegate<WidgetsLocalizations>>[
            S.delegate,
            // You need to add them if you are using the material library.
            // The material components usses this delegates to provide default 
            // localization      
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,               
      ],
      supportedLocales: S.delegate.supportedLocales,
      title: 'Flutter Demo',
      theme: new ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: new MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}
```

## Supported Features

- Normal Strings
- Strings with arguments
- Plurals (only integer part)

## Code Organization

- `cli` - Command Line Tool based on [patrol](https://github.com/vishna/patrol), uses as2f as a module
- `as2f` - All the code generation logic & unit tests, contributions welcome.
- `bin` - dart script wrapper around jar file allowing you to run `pub run as2f:codegen` right from your project