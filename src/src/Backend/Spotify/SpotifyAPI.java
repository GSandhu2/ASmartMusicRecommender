package Backend.Spotify;

import Backend.Analysis.SpotifyAnalysis;
import Backend.Helper.HttpRequest;
import Backend.Helper.ParseJson;


/**
 * @author Ethan Carnahan, Eric Kumar
 * Used to interact with Spotify.
 */
public class SpotifyAPI {

  private static final String API_URL = "https://api.spotify.com/v1/audio-features/";
  private static final String CREATE_PLAYLIST_URL = "https://api.spotify.com/v1/users/";
  private static final String VIEW_PLAYLIST_URL = "https://api.spotify.com/v1/playlists/";
  private static final String SEARCH_SONG_URL = "https://api.spotify.com/v1/search?q=";
  private static final String SEARCH_TRACK_URL = "https://api.spotify.com/v1/tracks/";
  private static final String USER_ID = "eric_123*";
  private static final String JSON_TYPE = "application/json";
  private static final SpotifyAuth auth = new SpotifyAuth();

  //region Public methods

  /**
   * Gets Spotify's track analysis of a song.
   *
   * @param trackId The random string after "track/" in the url of a song.
   * @return Spotify's basic track analysis.
   * @throws RuntimeException if something goes wrong. It could be so many things.
   */
  public static SpotifyAnalysis getTrackFeatures(String trackId) {
    // Request track features.
    String accessToken = auth.getAccessCode();
    String url = API_URL + trackId;
    String jsonString;
    try {
      jsonString = HttpRequest.getJsonFromUrl(url, accessToken);
    } catch (RuntimeException e) {
      throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage());
    }

    // Parse response into a SpotifyAnalysis object.
    return new SpotifyAnalysis(jsonString, trackId);
  }

  /**
   * Creates a playlist from a list of songs
   *
   * @param trackIds List of the random strings after "track/" in the url of a song.
   * @throws RuntimeException if something goes wrong. It could be so many things.
   */
  public static void createPlaylist(String[] trackIds) {
    String accessToken = auth.getAccessCode();
    String playlistCreationUrl = CREATE_PLAYLIST_URL + USER_ID + "/playlists";
    String uris = String.join(",", trackIds);
    StringBuilder body = new StringBuilder();
    body.append("{\"name\": \"ASMR playlist\",");
    body.append("\"description\": \"Playlist created by ASMR\",");
    body.append("\"public\": false}");
    String responseString;
    try {
      responseString = HttpRequest.postAndGetJsonFromUrlBody(playlistCreationUrl, body.toString(), JSON_TYPE,
          accessToken);
      String id = ParseJson.getString(responseString, "id");
      String playlistAdditionUrl = VIEW_PLAYLIST_URL + id + "/tracks?uris=" + uris;
      HttpRequest.postAndGetJsonFromUrlBody(playlistAdditionUrl, "", null, accessToken);

    } catch (RuntimeException e) {
      throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage());
    }
  }

  /**
   * Fetches a random song from Spotify
   *
   * @throws RuntimeException if something goes wrong. It could be so many things.
   * @returns song returns a random song from Spotify
   */

  public static String randomSong() {
    StringBuilder song = new StringBuilder();
    String accessToken = auth.getAccessCode();
    // A list of all characters that can be chosen.
    String characters = "abcdefghijklmnopqrstuvwxyz";

    // Gets a random character from the characters string.
    char randomCharacter = characters.charAt((int) Math.floor(Math.random() * characters.length()));

    // Places the wildcard character at the beginning, or both beginning and end, randomly.
    switch ((int) Math.round(Math.random())) {
      case 0:
        song.append(randomCharacter + '%');
        break;
      case 1:
        song.append('%' + randomCharacter + '%');
        break;
    }
    String responseString;
    try {
      StringBuilder url = new StringBuilder(SEARCH_SONG_URL);
      url.append(song);
      url.append("&type=track");
      url.append("&limit=1");
      responseString = HttpRequest.getJsonFromUrl(url.toString(), accessToken);
      System.out.println("Response String:" + responseString);
      String track = ParseJson.getObject(responseString, "tracks");
      System.out.println("Track: " + track);
      String[] items = ParseJson.getArray(track, "items");
      System.out.println("Items:" + items);
      String id = items[9];
      StringBuilder url2 = new StringBuilder(SEARCH_TRACK_URL);
      url2.append(id);
      System.out.println("Second url:" + url2);
      responseString = HttpRequest.getJsonFromUrl(url2.toString(), accessToken);
      String spotifyLink = ParseJson.getString(responseString, "spotify");
      System.out.println("Song Link:" + spotifyLink);
    } catch (RuntimeException e) {
      throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage());
    }
    return responseString;
  }


  //endregion

}