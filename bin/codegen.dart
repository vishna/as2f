import 'dart:io';
import 'dart:convert';

/// fixed version for predictable builds
const version = "ec87c17d45";
const name = "as2f";

/// fat jar baked on jitpack
const jarPath =
    "https://www.jitpack.io/com/github/vishna/$name/cli/$version/cli-$version-all.jar";

const cacheDir = ".jarCache";
const savePath = "$cacheDir/$name-$version.jar";

const MISSING_JDK_INFO = """

OpenJDK required by this code generator:

*-----------------------------------*
|     https://adoptopenjdk.net/     |
*-----------------------------------*

MacOSX + Homebrew Install Steps:

*-----------------------------------*
|  brew tap AdoptOpenJDK/openjdk    |
|  brew cask install adoptopenjdk8  |
*-----------------------------------*

""";

void main(List<String> arguments) async {
  if (!(await hasJDK())) {
    print(MISSING_JDK_INFO);
    return;
  }

  await Directory(cacheDir).create(recursive: true);
  if (FileSystemEntity.typeSync(savePath) == FileSystemEntityType.notFound) {
    print("Trying to download $name-$version.jar ...");

    try {
      await downloadFile(jarPath, savePath);
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

Future<bool> hasJDK() async {
  try {
    final result = await Process.run('java', ['-version']);
    return result.exitCode == 0;
  } catch (_) {
    return false;
  }
}

Future<void> downloadFile(String url, String filename) async {
  final _client = HttpClient();

  final request = await _client.getUrl(Uri.parse(url));

  final response = await request.close();

  await response.pipe(File(filename).openWrite());

  _client.close(force: true);

  return;
}
