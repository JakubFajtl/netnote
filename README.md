# NETNOTE

## Starting the app

To run the project from the command line, you either need to have [Maven](https://maven.apache.org/install.html) installed on your local system (`mvn`) or you need to use the Maven wrapper (`mvnw`). You can then execute

	mvn clean install

to package and install the artifacts for the three subprojects. Afterwards, you can run ...

	cd server
	mvn spring-boot:run

to start the server or ...

	cd client
	mvn javafx:run

to run the client.

More instructions in client README.md

## Implemented Features
### Collections
- You can change the collection of a note by clicking the dropdownmenu at topright
- You can select collections by:
    - Clicking in the collection list to select one
    - Ctrl+click to add more to selection
- You can set a default collection
- When creating a new note, the user is prompted to pick a collection
- When multiple collections are selected it picks the default collection, unless specified otherwise.
- When only one collection is selected a new note is added to that collection, unless specified otherwise.
- When in the Collections menu the user is presented with the status of the collection, it can be either created by giving it a unique Title and key in the Collection field or renamed if the key already belongs to another collection
- ***Additions***: 
    - You can select multiple collections
    - Notes are grouped by collection
    - Allows for multiple/different servers
        - Collectionkeys still need to be unique across servers

### Embedded Files
- When a file is embedded to a note you can right click on the file
- Send a request to the server to download the file onto the system
- Delete the file
- Rename a file 
- Encoding is used for special characters 
- We use regex to shorten the reference URL to just the name of the file

- The files are available at ``` http://{server}/api/files/{colectionKey}/{noteId}/{fileName} ```
- ***Additions***: 
    - "Insert reference" option in context menu to add an image in markdown so that user doesn't have to type it manually
    - Reference is automatically inserted into content when file is added to note
    - When a file is renamed all references in the note content are changed accordingly

### Live Language Switch
- You can click on the flag icon to change the language
- ***Additions***: 
    - We added extra languages: Frysian and Cat
    - We translated all error messages


## Tasks & Planning

- We have created a "removed" tag for abandoned issues
- We use the issue boards to keep track of the state of issues, closed issues are implemented unless they have the "removed" tag
- We use a description template for creating issues and MRs with checkboxes so that our descriptions stay consistent
- We use tags to set priority, type and location in codebase to different issues
- We use milestones to set weekly goals
- We put time tracking into both the Issue and the MR because the it doesn't sync

## Technology (Formative)

- We use controllers in client to interact with user input
- We inject services for functionality
- We use utils for APIs to communicate with the server

## Usability/Accessibility

### Color Contrast
- App is black and white (high contrast) to consider the colorblind
### Shortcuts
#### NotesHome scene
- Alt+A -> Open the actions on a note shortcuts menu: 
    - Alt+N -> Create a new note
    - Alt+T -> Title of selected note
    - Alt+E -> Edit the content of the selected note
    - Alt+D -> Delete the selected note
- Alt+F -> open the filtering/searching shortcuts menu
    - Esc -> Select searchbar
    - Enter -> Perform search 
- Alt+X -> Open the extra actions shortcuts menu
    - Alt+L -> Move to list of notes
    - Alt+O -> Open collections popup
    - Alt+C -> Open collection dropdown
    - Alt+R -> Refresh all notes
    - Alt+Q -> Quit the app
#### Collections scene
- Alt+T -> Title of collections
- Alt+U -> Url of server
- Alt+C -> Collection at server
- Alt+A -> Add/Create/Update collection 
- Alt+L -> Move to collections list
- Alt+N -> New collection
- Alt+D -> Delete collection
- Alt+N -> New collection
- Alt+R -> Remove collection
- Alt+D -> Set collection as default
- Alt+Q -> Close collection popup
#### Create note popup
- Alt+C -> Navigate through collections using arrows
- Enter -> Create note

### Multi-modal Visualization
- Icons for buttons
    - You can hover over them to see descriptive tooltip

### Navigation
- We have shortcuts
- You can navigate in note and collection lists with up and down arrow keys
- You can change between note list and collection list with left and right arrow keys

### User feedback

- We implemented centralized error handling to handle all errors similarly and show the popups to the user.
- An error is thrown when for example:
    - Making a note title empty
    - File is too large
    - When server doesn't respond
- If the server is not found when creating a collection, it shows the status message and doesn't let the user create.
- If some servers do not respond, the app asks the user to remove these collections or make the server available.
- Irreversible actions such as deleting a note or deleting a collection must be confirmed through a popup
- Examples of informational popup implementations are updating the default collection, deleting notes or deleting collections
