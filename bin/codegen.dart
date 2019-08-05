import 'dart:io';
import 'dart:convert';

/// fixed version for predictable builds
const version = "737c0da010";
const name = "as2f";

/// fat jar baked on jitpack
const jarPath =
    "https://jitpack.io/com/github/vishna/$name/cli/$version/cli-$version-all.jar";

const cacheDir = ".jarCache";
const savePath = "$cacheDir/$name-$version.jar";

void main(List<String> arguments) async {
  await Directory(cacheDir).create(recursive: true);
  if (FileSystemEntity.typeSync(savePath) == FileSystemEntityType.notFound) {
    print("Trying to download $name-$version.jar ...");

    try {
      await downloadJar(jarPath, savePath);
      print("Downloaded $name-$version.jar");
    } catch (_) {
      // wget gets the job done while dart based http file download errors 500 ¯\_(ツ)_/¯
      stderr.writeln(
          "Failed to download $jarPath \nPlease download the jar manually and save it to $savePath.\nOnce this is complete just rerun pub command.");
      exit(1);
    }
  }

  final process = await Process.start('java', ['-jar', savePath] + arguments);
  process.stdout.transform(utf8.decoder).listen((data) {
    print("$data".trim());
  });
}

downloadJar(String url, String target) async {
  await Process.run('wget', [url, '-O', target]);
}
