# DW CLI

**DW CLI** allows to execute queries and generate data directly from the command line.


    

**DW CLI is** compiled with [Graal AOT](https://www.graalvm.org/docs/reference-manual/aot-compilation/) for fast bootstrap so download the one for your target OS.

## What does it contains?

- It includes all the modules that are included in the weave runtime. [wlang](https://github.com/mulesoft/data-weave/tree/master/wlang)
- It includes file-module, http-module and raml-module from [data-weave-io]((https://github.com/mulesoft-labs/data-weave-io)
- It includes dependency-management-module. This module allows to declare dependencies using annotations.
- Data Formats are:

| Format  |      MimeType      |  Extension |
|----------|:-------------:|------:|
| Xml | application/xml | .xml |
| Json | application/json | .json |
| CSV | application/csv | .csv |
| Properties | text/x-java-properties | .properties|
| TextPlain | text/plain | .txt|
| Yaml | application/yaml | .yaml|
| Binary | application/octet-stream| .bin|
| Multipart | multipart/form-data| .multipart|
| UrlEncoded | application/x-www-form-urlencoded|.urlencoded|
| NDjson| application/x-ndjson|.ndjson|

## How to install it? 

### Using Homebrew:
```
brew tap mulesoft-labs/data-weave
brew install dw
```

### Manually
1. Download the zip [Mac](https://github.com/mulesoft-labs/data-weave-native/releases/download/v1.0.9/dw-1.0.9-macOS.zip) | [Linux](https://github.com/mulesoft-labs/data-weave-native/releases/download/v1.0.9/dw-1.0.9-Linux.zip) | [Windows](https://github.com/mulesoft-labs/data-weave-native/releases/download/v1.0.9/dw-1.0.9-Windows.zip) 
2. Unzip the file on your `<home_directory>/.dw`
3. Add `<home_directory>/.dw/bin` to your PATH


### From code

Execute the gradle task nativeImage

```
./gradlew native-cli:nativeImage
```

It takes several minutes so good time to take and refeel your mate.

Once it finishes you will find the `dw` binary in `native-cli/build/graal/dw`


## How to use it

If the directory containing the `dw` executable is in your PATH, you can run `dw` from anywhere. If it is not, go to the `bin` directory referenced in the installation instructions and run `dw` from there.
 

### Show documentation

`dw`
 
```bash

.........................................................................
.%%%%%....%%%%...%%%%%%...%%%%...%%...%%..%%%%%%...%%%%...%%..%%..%%%%%%.
.%%..%%..%%..%%....%%....%%..%%..%%...%%..%%......%%..%%..%%..%%..%%.....
.%%..%%..%%%%%%....%%....%%%%%%..%%.%.%%..%%%%....%%%%%%..%%..%%..%%%%...
.%%..%%..%%..%%....%%....%%..%%..%%%%%%%..%%......%%..%%...%%%%...%%.....
.%%%%%...%%..%%....%%....%%..%%...%%.%%...%%%%%%..%%..%%....%%....%%%%%%.
.........................................................................


Usage:

dw [-p <weavePath>]? [-i <name> <path>]* [-v]? [-o <outputPath>]? [[-f <filePath>] | [-m <nameIdentifier>] | <scriptContent>]

Arguments Detail:

 --spell | Runs a spell. Use the <spellName> or <wizard>/<spellName> for spells from a given wizard.
 --update-grimoires | Update all wizard grimoires
 --add-wizard    | Downloads wizard grimoire so that its spell are accessible
 --path or -p    | Path of jars or directories where weave files are being searched.
 --input or -i   | Declares a new input.
 --verbose or -v | Enable Verbose Mode.
 --output or -o  | Specifies output file for the transformation if not standard output will be used.
 --main or -m    | The full qualified name of the mapping to be execute.
 --file or -f     | Path to the file
 --eval          | Evaluates the script instead of writing it
 --version       | The version of the CLI and Runtime
 --clean-cache   | Cleans the cache where all artifacts are being downloaded this force to download all artifacts every time

 Example:

 dw -i payload <fullpathToUser.json> "output application/json --- payload filter (item) -> item.age > 17"

 Documentation reference:

 https://docs.mulesoft.com/mule-runtime/4.3/dataweave
    

```

### Query content from a file

Input file `users.json`

```json
[
  {
    "name": "User1",
    "age": 19
  },
  {
    "name": "User2",
    "age": 18
  },
  {
    "name": "User3",
    "age": 15
  },
  {
    "name": "User4",
    "age": 13
  },
  {
    "name": "User5",
    "age": 16
  }
]
```
 
Let's query users old enough to drink alcohol:

`dw -i payload <fullpathToUsers.json> "output application/json --- payload filter (item) -> item.age > 17"`


### Query content from standard input

`cat <fullpathToUser.json> | dw "output application/json --- payload filter (item) -> item.age > 17"`


### Redirecting the output to a file

`dw "output application/xml --- users: {( 1 to 100 map (item) -> {user: "User" ++ item} )}" >> out.xml` 


### DW Important ENV variables

* *DW_HOME* = The directory where the home will be found if not defined `~/.dw` will be used
* *DW_LIB_PATH* = The directory where libraries are going to be search by default. If not defined `${DW_HOME}/libs` will be used
* *DW_DEFAULT_INPUT_MIMETYPE* = The default mimeType that is going to be used for the standard input. If not defined `application/json` will be used
* *DW_DEFAULT_OUTPUT_MIMETYPE* = The default output mimeType that is going to be if not defined. If not defined `application/json` will be used


### Using external DW Modules with --path

This specifies the path where to search it will also search on the `DW_LIB_PATH` env directory path.

```bash
DIRECTORY=<pick a directory>

cd $DIRECTORY

echo '%dw 2.0
fun addUnderscore(s: String): String = s ++ "_"' > utils.dwl

dw --path $DIRECTORY 'output application/json import * from utils --- {underscored: addUnderscore("hello")}'
```


## CURL + DW => Power API Playground

An interesting use case for the CLI is to combine it with [curl](https://curl.haxx.se/)  


### Example query github commits

We can use the Github API to query commits of a repo.

We can easily get the first commit by doing:

`curl "https://api.github.com/repos/mulesoft/mule/commits?per_page=5" | dw "payload[0]"`

or we can get the message by doing:

`curl "https://api.github.com/repos/mulesoft/mule/commits?per_page=5" | dw "{message: payload[0].commit.message}"` 


### HTTP POST data generated by DW

This example will create a really big csv and it will stream it to the HTTP server on localhost.

`dw "output application/csv --- (1 to 10000000000000000000000) map (item) -> {name: 'User \$(item)'}" | curl -X POST  -T "/dev/stdin" http://localhost:8081/`


## Documentation
For more info about the language see the [docs site](https://docs.mulesoft.com/mule-runtime/latest/dataweave)

## Spells 


Spells are just executables scripts that can be called from the command line using the `Spell` name.
For example 
`dw --eval --spell Playground` It is going to execute the Playground spell that is going to be located in 
`{User_Home}/.dw/grimoires/data-weave-grimoire/Playground/src/Main.dwl`

## Grimoire

A grimoire is a collection of `spells` from a given `wizard`. The default `grimoire` is located in [data-weave-grimoire](https://github.com/mulesoft-labs/data-weave-grimoire)
But it can also be consumed from other trusted `Wizard`. For example `leansh/Tutorial` is going to looks for a github repo called leansh-data-weave-grimoire under leansh user.


