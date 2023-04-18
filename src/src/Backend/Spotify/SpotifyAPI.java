package Backend.Spotify;

import Backend.Analysis.SpotifyAnalysis;
import Backend.Helper.HttpRequest;

/**
 * @author Ethan Carnahan
 * Used to interact with Spotify.
 */
public class SpotifyAPI {
    private static final String API_URL = "https://api.spotify.com/v1/audio-features/";
    private static final SpotifyAuth auth = new SpotifyAuth();

    //region Public methods
    /**
     * Gets Spotify's track analysis of a song.
     * @param trackId The random string after "track/" in the url of a song.
     * @return Spotify's basic track analysis.
     * @throws RuntimeException if something goes wrong. It could be so many things.
     */
    public static SpotifyAnalysis getTrackFeatures(String trackId) {
        // Request track features.
        String accessToken = auth.getAccessCode();
        String url = API_URL + trackId;
        String jsonString;
        try { jsonString = HttpRequest.getJsonFromUrl(url, accessToken); }
        catch (RuntimeException e) { throw new RuntimeException("SpotifyAPI: Failed to connect to Spotify - " + e.getMessage()); }

        // Parse response into a SpotifyAnalysis object.
        return new SpotifyAnalysis(jsonString);
    }

    //endregion

}