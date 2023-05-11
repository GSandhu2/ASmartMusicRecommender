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

# Information for Alpha Release: 
Demo functionality:
- Matches Spotify songs to ~50 other Spotify songs at random.
- UI is working, but prints match results to console.
- No local sound analysis for now.

To install:
- Install most recent available version of IntelliJ (either community or ultimate version works)
- Link: https://www.jetbrains.com/idea/download/
- Open IntelliJ and use "Get from VCS" to clone repository.
- Go to File -> Project Structure and set Java level to 19 (may also need to select/install SDK).
- (Important) Email ethancar@uw.edu with your Spotify email so I can give you access to the Spotify API, otherwise you'll see an authorization error in the console. Spotify only allows "a maximum of 25 users in develoment mode".

To build: Open Maven tab on the right and run ASmartMusicRecommender/Lifecycle/compile.
To test: Right click on test folder and click "Run all tests".

To run: Run main method in frontend/Home.java.

Use cases: Right now, our primary use case is finding songs similar sounding to the song that you like.

# Information for users/developers:
To develop:
- Clone repository into the most recent available version of IntelliJ and set Java level to 19.
- Assign yourself an issue or write a new issue to work on.
- If multiple people are assigned to the same issue, communicate to prevent merge conflicts.

To install:
- Make sure you have Java installed.
- Download asmr.jar file from release.
- Double-click asmr.jar file to run program.
