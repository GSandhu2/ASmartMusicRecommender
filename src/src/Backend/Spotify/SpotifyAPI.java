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
  private static final String PLAYLIST_URL = "https://api.spotify.com/v1/playlists/";
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

    try {
      String displayName = ParseJson.getString(jsonString, "display_name");
      return displayName;
    } catch (RuntimeException e) { // Happens if display_name is null
      return userId;
    }
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
    String playlistCreationUrl = USERS_URL + USER_ID + "/playlists";
    String uris = String.join(",", trackIds);
    System.out.println("TrackIds parameter:" + uris);
    StringBuilder body = new StringBuilder();
    body.append("{\"name\": \"ASMR playlist\",");
    body.append("\"description\": \"Playlist created by ASMR\",");
    body.append("\"public\": false}");
    String responseString;
    try {
      responseString = HttpRequest.postAndGetJsonFromUrlBody(playlistCreationUrl, body.toString(), JSON_TYPE,
          accessToken);
      System.out.println("Response String" + responseString);
      String id = ParseJson.getString(responseString, "id");
//      id = id.substring(2);
      System.out.println("Trying to get playlist id:" + id);
      String playlistAdditionUrl = PLAYLIST_URL + id + "/tracks?uris=" + uris;
      System.out.println(playlistAdditionUrl);
      HttpRequest.postAndGetJsonFromUrlBody(playlistAdditionUrl, "", null, accessToken);

    } catch (RuntimeException e) {
      throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage());
    }
//    return responseString;
  }

  //endregion

}