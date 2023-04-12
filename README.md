# ASmartMusicRecommender
Recommend music based on sound analysis instead of author/genre.

Goals:
- Analyze music using only the objective information contained in a sound file.
- Link users to matching songs.

Code Layout:
- Backend
    - Analysis - The different types of analysing each song, such as Spotify or our own analysis.
    - Spotify - Interact with Spotify API to get their analysis of music, create playlists for user.
    - Helper - Classes that help with miscellaneous functions, like parsing Json and .mp3 files.
- Frontend
    - WIP
