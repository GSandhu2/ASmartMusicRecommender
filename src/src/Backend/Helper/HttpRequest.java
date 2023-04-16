package Backend.Helper;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Make/receive http requests.
 * Throws RuntimeException on error responses or IO errors.
 * Throws IllegalArgumentException on bad Url.
 */
public class HttpRequest {
    // Used to send GET request for Json.
    public static String getJsonFromUrl(String url, String authorization) throws RuntimeException {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) (new URL(url).openConnection());
            connection.setRequestProperty("Authorization", authorization);
            connection.connect();

            return getResponse(connection);
        }
        catch (MalformedURLException e) { throw new IllegalArgumentException("HttpRequest: Invalid URL - " + e.getMessage()); }
        catch (IOException e) { throw new RuntimeException("HttpRequest: IOException" + e.getMessage()); }
    }

    // Used to send POST request for Json using Content-Type "application/x-www-form-urlencoded".
    public static String postAndGetJsonFromUrlBody(String url, String body) throws RuntimeException {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) (new URL(url).openConnection());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // Means parameters are in URL.
            connection.setDoOutput(true);
            connection.connect();
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().close();

            return getResponse(connection);
        }
        catch (MalformedURLException e) { throw new IllegalArgumentException("HttpRequest: Invalid URL - " + e.getMessage()); }
        catch (IOException e) { throw new RuntimeException("HttpRequest: IOException - " + e.getMessage()); }
    }

    // Gets Http response from request.
    private static String getResponse(HttpsURLConnection connection) throws IOException {
        InputStreamReader isr;
        if (connection.getResponseCode() >= 400) {
            isr = new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8);
            throw new RuntimeException("HttpRequest: Error response - " + readInputStream(isr));
        } else {
            isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
            return readInputStream(isr);
        }
    }

    // Gets String from input stream.
    private static String readInputStream(InputStreamReader isr) throws IOException {
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder json = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            json.append(line);
            line = reader.readLine();
        }
        isr.close();
        return json.toString();
    }
}
