# ASmartMusicRecommender
Recommend music based on sound analysis instead of author/genre.

Goals:
- Analyze music using only the objective information contained in a sound file.
- Link users to matching songs.

Code Layout:
- Backend
    - Analysis - The different types of analysing each song, such as Spotify or our own analysis.
    - Algorithm - Our custom sound analysis algorithm and the math used in it.
    - Spotify - Interact with Spotify API to get their analysis of music, create playlists for user.
    - Helper - Classes that help with miscellaneous functions, like parsing Json and .mp3 files.
- Frontend
    - UI - The visual organization of the program.
    - Control - How the program responds to user input.
    - Graphics - Images and fonts used in the program.

# Information for Alpha Assignment: 
To build/test:
- Clone repository into IntelliJ and set Java level to 19.
- May have to restart or click a button to sync Maven dependencies.
- Open Maven tab on the right and click "compile"/"test".

To run:
- Clone repository into IntelliJ and set Java level to 19.
- May have to restart or click a button to sync Maven dependencies.
- Run main method in front-end.

# Information for users/developers:
To develop:
- Clone repository and set Java level to 19.
- Assign yourself an issue or write a new issue to work on.
- If multiple people are assigned to the same issue, communicate to prevent merge conflicts.

To install:
- Make sure you have Java installed.
- Download asmr.jar file from release.
- Double-click asmr.jar file to run program.
