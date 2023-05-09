package Backend.Spotify;

import Backend.Analysis.SpotifyAnalysis;
import Backend.Helper.HttpRequest;
import Backend.Helper.ParseJson;

/**
 * @author Ethan Carnahan, Eric Kumar
 * Used to interact with Spotify.
 */
public class SpotifyAPI {

  private static final String FEATURES_URL = "https://api.spotify.com/v1/audio-features/";
  private static final String USERS_URL = "https://api.spotify.com/v1/users/";
  private static final String CREATE_PLAYLIST_URL = "https://api.spotify.com/v1/users/";
  private static final String VIEW_PLAYLIST_URL = "https://api.spotify.com/v1/playlists/";
  private static final String SEARCH_SONG_URL = "https://api.spotify.com/v1/search?q=";
  private static final String TRACK_URL = "https://api.spotify.com/v1/tracks/";
  private static final String JSON_TYPE = "application/json";
  private static final SpotifyAuth auth = new SpotifyAuth();
  private static String USER_ID = "";

  //region Public methods

  /**
   * Sets the username to use for future SpotifyAPI calls.
   *
   * @param userId The user's Spotify username.
   * @return The display name of the Spotify user, or username if there isn't one.
   * @throws RuntimeException if username does not exist or something else goes wrong.
   */
  public static String setUserId(String userId) {
    String accessToken = auth.getAccessCode();
    String url = USERS_URL + userId;
    String jsonString;
    try {
      jsonString = HttpRequest.getJsonFromUrl(url, accessToken);
    } catch (RuntimeException e) {
      throw new RuntimeException("Spotify API: Invalid username - " + e.getMessage());
    }

    USER_ID = userId;
    System.out.println("SpotifyAPI: Set user ID to " + USER_ID);

    try {
      return ParseJson.getString(jsonString, "display_name");
    } catch (RuntimeException e) { // Happens if display_name is null
      return userId;
    }
  }

  /**
   * Gets the Spotify URL of a song.
   *
   * @param trackId The random string after "track/" in the url of a song.
   * @return Spotify URL of the specified song.
   * @throws RuntimeException if something goes wrong. It could be so many things.
   */
  public static String getTrackURL(String trackId) {
    String accessToken = auth.getAccessCode();
    String url = TRACK_URL + "/" + trackId;
    String jsonString;
    try {
      jsonString = HttpRequest.getJsonFromUrl(url, accessToken);
    } catch (RuntimeException e) {
      throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage());
    }

    return ParseJson.getString(ParseJson.getObject(jsonString, "external_urls"), "spotify");
  }

  /**
   * Gets the Spotify URL of multiple songs.
   *
   * @param trackIds The random strings after "track/" in the url of a song.
   * @return Spotify URL of the specified songs.
   * @throws RuntimeException if something goes wrong. It could be so many things.
   */
  public static String[] getTrackURLs(String[] trackIds) {
    String[] result = new String[trackIds.length];
    String accessToken = auth.getAccessCode();
    StringBuilder url = new StringBuilder(TRACK_URL + "?ids=" + trackIds[0]);
    for (int i = 1; i < trackIds.length; i++) {
      url.append(",");
      url.append(trackIds[i]);
    }
    String jsonString;
    try {
      jsonString = HttpRequest.getJsonFromUrl(url.substring(0, url.length()-1), accessToken);
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage());
    }

    String[] tracks = ParseJson.getArray(jsonString, "tracks");
    for (int i = 0; i < tracks.length; i++) {
      result[i] = ParseJson.getString(ParseJson.getObject(tracks[i], "external_urls"), "spotify");
    }

    return result;
  }

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
    String url = FEATURES_URL + trackId;
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
   * @return The URL to a random song from Spotify
   */

  public static String[] randomSong(int num) {
    String[] spotifyIds = new String[num - 1];

    String song = "";
    String accessToken = auth.getAccessCode();
    // A list of all characters that can be chosen.
    String characters = "abcdefghijklmnopqrstuvwxyz";

    // Gets a random character from the characters string.
    String randomCharacter = String.valueOf(
        characters.charAt((int) Math.floor(Math.random() * characters.length())));

    // Places the wildcard character at the beginning, or both beginning and end, randomly.
    switch ((int) Math.round(Math.random())) {
      case 0 -> song = randomCharacter + "$";
      case 1 -> song = "$" + randomCharacter + "$";
    }
    System.out.println("SpotifyAPI: Getting " + num + " random songs using query " + song);

    String responseString;
    try {
      String url = SEARCH_SONG_URL + song
          + "&type=track"
          + "&limit="+num;

      responseString = HttpRequest.getJsonFromUrl(url, accessToken);
      String tracks = ParseJson.getObject(responseString, "tracks");
      String[] items = ParseJson.getArray(tracks, "items");
      for (int i = 0; i < num - 1; i++) {

        //System.out.println("Track: " + track);
        //System.out.println("Items:" + Arrays.toString(items));

        String id = ParseJson.getString(items[i],"id");
        spotifyIds[i] = id;
//      responseString = HttpRequest.getJsonFromUrl(SEARCH_TRACK_URL + id, accessToken);
//      spotifyLink = ParseJson.getString(ParseJson.getObject(responseString, "external_urls"), "spotify");
        //System.out.println("Random song Link: " + spotifyLink);
      }

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage());
    }
    return spotifyIds;
  }

  //endregion

}