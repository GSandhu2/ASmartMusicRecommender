package Backend.Spotify;

import Backend.Analysis.SpotifyAnalysis;

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
                getJsonDouble(jsonString, "acousticness"),
                getJsonDouble(jsonString, "danceability"),
                getJsonDouble(jsonString, "energy"),
                getJsonDouble(jsonString, "instrumentalness"),
                getJsonDouble(jsonString, "liveness"),
                getJsonDouble(jsonString, "speechiness"),
                getJsonDouble(jsonString, "valence"),
                getJsonDouble(jsonString, "loudness"),
                getJsonDouble(jsonString, "tempo"),
                getJsonInt(jsonString, "duration_ms"),
                getJsonInt(jsonString, "key"),
                getJsonInt(jsonString, "mode"),
                getJsonInt(jsonString, "time_signature")
        );
    }


    //endregion

    //region Json methods
    // Throws IllegalArgumentException if key doesn't exist in json object.

    // Gets a json value key:{object} from a json object.
    private static String getJsonObject() {
        return null;
    }

    // Gets a json value key:[array] from a json object.
    private static String[] getJsonArray() {
        return null;
    }

    // Gets a json value key:"string" from a json object.
    private static String getJsonString(String jsonObject, String key) {
        int start = jsonObject.indexOf("\"" + key + "\"") + key.length() + 6;
        int end = jsonObject.indexOf(",", start);
        if (end == -1) // Key refers to last value in json object.
            end = jsonObject.length() - 2;
        if (start == -1)
            throw new IllegalArgumentException("Spotify API: Json value not found - " + start);

        return jsonObject.substring(start, end);
    }

    private static String getJsonNumber(String jsonObject, String key) {
        int start = jsonObject.indexOf("\"" + key + "\"") + key.length() + 5;
        int end = jsonObject.indexOf(",", start);
        if (end == -1) // Key refers to last value in json object.
            end = jsonObject.length() - 1;
        if (start == -1)
            throw new IllegalArgumentException("Spotify API: Json value not found - " + start);

        return jsonObject.substring(start, end);
    }

    // Gets a json value key:integer from a json object.
    private static int getJsonInt(String jsonObject, String key) {
        return Integer.parseInt(getJsonNumber(jsonObject, key));
    }

    // Gets a json value key.double from a json object.
    private static double getJsonDouble(String jsonObject, String key) {
        return Double.parseDouble(getJsonNumber(jsonObject, key));
    }
    //endregion
}