package Backend.Spotify;

import Backend.Analysis.SpotifyAnalysis;
import Backend.ParseJson;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
     * @throws IllegalArgumentException If trackId doesn't refer to a Spotify song.
     */
    public static SpotifyAnalysis getTrackFeatures(String trackId) {
        // Request track features.
        String accessToken = auth.getAccessCode();
        String url = API_URL + trackId;
        String jsonString;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) (new URL(url).openConnection());
            connection.setRequestProperty("Authorization", accessToken);
            connection.connect();
            InputStreamReader isr;
            if (connection.getResponseCode() >= 400)
                isr = new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8);
            else
                isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                json.append(line);
                line = reader.readLine();
            }
            jsonString = json.toString();
        }
        catch (MalformedURLException e) { throw new IllegalArgumentException("SpotifyAPI: Invalid URL - " + e.getMessage()); }
        catch (IOException e) { throw new IllegalStateException("SpotifyAPI: Failed to connect to spotify" + e.getMessage()); }

        // Parse response into a SpotifyAnalysis object.
        return new SpotifyAnalysis(
                ParseJson.getDouble(jsonString, "acousticness"),
                ParseJson.getDouble(jsonString, "danceability"),
                ParseJson.getDouble(jsonString, "energy"),
                ParseJson.getDouble(jsonString, "instrumentalness"),
                ParseJson.getDouble(jsonString, "liveness"),
                ParseJson.getDouble(jsonString, "speechiness"),
                ParseJson.getDouble(jsonString, "valence"),
                ParseJson.getDouble(jsonString, "loudness"),
                ParseJson.getDouble(jsonString, "tempo"),
                ParseJson.getInt(jsonString, "duration_ms"),
                ParseJson.getInt(jsonString, "key"),
                ParseJson.getInt(jsonString, "mode"),
                ParseJson.getInt(jsonString, "time_signature")
        );
    }

    //endregion

}