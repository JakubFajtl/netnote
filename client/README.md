
Assuming that you have [Maven](https://maven.apache.org/install.html) installed, you can run the project out-of-the-box from your terminal via

	mvn javafx:run

If you receive errors about the `commons` artifact not being available, make sure to run `mvn clean install` before.

Running the template project from within your IDE (Eclipse/IntelliJ) requires setting up OpenJFX.

First download (and unzip!) an [OpenJFX SDK](https://openjfx.io).
Make sure that the download *matches your Java JDK version*.

Then create a *run configuration* and add the following *VM* commands:

	--module-path="/path/to/javafx-sdk/lib"
	--add-modules=javafx.controls,javafx.fxml,javafx.web

Adjust the module path to *your* local download location and make sure you adapt the path
to the `lib`(!) directory (not just the directory that you unzipped)...

*Tip:* Windows uses `\` to separate path elements.

*Tip:* Make sure not to forget the `/lib` at the end of the path

*Tip:* Double-check that the path is correct. If you receive abstract error messages, like `Module javafx.web not found`
or a segmentation fault, you are likely not pointing to the right folder

Our application uses a config.json file, however the same file is used for all clients by default
if you run multiple clients in the same directory. To avoid it you can provide command line arguments to the clients
to have different config file names.
For example, if you use two clients, you could add:
- "1" in a *program arguments* of a *run configuration* of the first client
- "2" in a *program arguments* of a *run configuration* of the second client

Now the config file of the first client will be named config_1.json,
while the config file of the second client will be named config_2.json
Thus the clients will use different config files, this way you can test the behaviour of collections more easily.
