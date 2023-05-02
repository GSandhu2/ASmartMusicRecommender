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
  private static final String GET_SONG_URL = "https://api.spotify.com/v1/search?q=";
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
      System.out.println("Trying to get playlist id:" + id);
      String playlistAdditionUrl = VIEW_PLAYLIST_URL + id + "/tracks?uris=" + uris;
      System.out.println(playlistAdditionUrl);
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
      StringBuilder url = new StringBuilder(GET_SONG_URL);
      url.append(song);
      url.append("&type=track");
      responseString = HttpRequest.getJsonFromUrl(url.toString(), accessToken);
      System.out.println("Response String" + responseString);
      String id = ParseJson.getString(responseString, "id");

    } catch (RuntimeException e) {
      throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage());
    }
    return responseString;
  }


  //endregion

}