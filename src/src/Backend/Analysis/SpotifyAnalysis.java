package Backend.Analysis;

/**
 * @author Ethan Carnahan
 * The sound analysis of a song given by Spotify's "Audio Features".
 * See <a href="https://developer.spotify.com/documentation/web-api/reference/get-audio-features">Spotify API</a>
 */
public class SpotifyAnalysis implements SoundAnalysis {

    //region Fields, Constants, Constructor
    // 0.0-1.0 Values
    private final float acousticness, danceability, energy, instrumentalness, liveness, speechiness, valence;
    // > 1.0 Values
    private final float loudness, tempo;
    // Integer Values.
    private final int duration_ms, key, mode, time_signature;

    // Maximum difference = 1.0 * number of analysis values.
    private static final double MAX_DIFFERENCE = 13.0;
    // Exponentially adjusts the value of individual differences less than 1.0.
    private static final double DIFFERENCE_EXPONENT = 2.0;
    // Multiplies arctan bounds from pi/2 to 1.
    private static final double ARCTAN_MULTIPLIER = 2.0 / Math.PI;

    public SpotifyAnalysis(float acousticness, float danceability, float energy, float instrumentalness, float liveness, float speechiness, float valence, float loudness, float tempo, int duration_ms, int key, int mode, int time_signature) {
        this.acousticness = acousticness;
        this.danceability = danceability;
        this.energy = energy;
        this.instrumentalness = instrumentalness;
        this.liveness = liveness;
        this.speechiness = speechiness;
        this.valence = valence;
        this.loudness = loudness;
        this.tempo = tempo;
        this.duration_ms = duration_ms;
        this.key = key;
        this.mode = mode;
        this.time_signature = time_signature;
    }
    //endregion

    //region Methods
    @Override
    public double compareTo(SoundAnalysis other) {
        if (!(other instanceof SpotifyAnalysis otherSpotify))
            throw new IllegalArgumentException("Incompatible sound analysis types.");

        double difference = 0.0;

        // 0.0-1.0 Values: Just subtract to get a value between 0.0-1.0.
        difference +=
                Math.pow(Math.abs(otherSpotify.acousticness - this.acousticness), DIFFERENCE_EXPONENT) +
                Math.pow(Math.abs(otherSpotify.danceability - this.danceability), DIFFERENCE_EXPONENT) +
                Math.pow(Math.abs(otherSpotify.energy - this.energy), DIFFERENCE_EXPONENT) +
                Math.pow(Math.abs(otherSpotify.instrumentalness - this.instrumentalness), DIFFERENCE_EXPONENT) +
                Math.pow(Math.abs(otherSpotify.liveness - this.liveness), DIFFERENCE_EXPONENT) +
                Math.pow(Math.abs(otherSpotify.speechiness - this.speechiness), DIFFERENCE_EXPONENT) +
                Math.pow(Math.abs(otherSpotify.valence - this.valence), DIFFERENCE_EXPONENT);

        // > 1.0 Values: Use arctan(difference or ratio) to clamp value between 0.0-1.0.
        // If this.tempo is somehow zero, assume tempos are very different.
        difference += Math.pow(ARCTAN_MULTIPLIER * Math.atan(Math.abs(otherSpotify.loudness - this.loudness)), DIFFERENCE_EXPONENT);
        if (this.tempo == 0.0)
            difference += 1.0;
        else
            difference += Math.pow(ARCTAN_MULTIPLIER * Math.atan(Math.abs(otherSpotify.tempo / (this.tempo == 0.0 ? 1.0 : this.tempo))), DIFFERENCE_EXPONENT);

        // Integer Values: Calculate 0.0-1.0 difference with separate methods.
        difference += Math.pow(compareDuration(otherSpotify.duration_ms, this.duration_ms), DIFFERENCE_EXPONENT);
        difference += Math.pow(compareKey(otherSpotify.key, this.key), DIFFERENCE_EXPONENT);
        difference += Math.pow(compareMode(otherSpotify.mode, this.mode), DIFFERENCE_EXPONENT);
        difference += Math.pow(compareTimeSignature(otherSpotify.time_signature, this.time_signature), DIFFERENCE_EXPONENT);

        // Divide by MAX_DIFFERENCE to get match value between 0.0-1.0.
        return 1.0 - (difference / MAX_DIFFERENCE);
    }

    // These methods return 1.0 for different values, not similar.

    // Use same method as > 1.0 values but for integers.
    // Surely durations won't be zero.
    private static double compareDuration(int otherDuration, int thisDuration) {
        return ARCTAN_MULTIPLIER * Math.atan(Math.atan((double) otherDuration / thisDuration));
    }

    // Musical notes are circular, so 0/11 (C, B) are closer than 0/6 (C, F#).
    // -1 means Spotify doesn't know. If both are -1, it might be the same song.
    private static double compareKey(int otherKey, int thisKey) {
        if (otherKey == -1 && thisKey == -1)
            return 0.0;
        if (otherKey == -1 || thisKey == -1)
            return 1.0;
        int MAX_KEY_DIFFERENCE = 6;
        return (double) Math.min(Math.abs(otherKey - thisKey), 12 - Math.abs(otherKey - thisKey)) / MAX_KEY_DIFFERENCE;
    }

    // Major/minor modes sound very different.
    private static double compareMode(int otherMode, int thisMode) {
        return otherMode == thisMode ? 0.0 : 1.0;
    }

    // All time signatures sound equally very different from each other.
    private static double compareTimeSignature(int otherTimeSignature, int thisTimeSignature) {
        return otherTimeSignature == thisTimeSignature ? 0.0 : 1.0;
    }
    //endregion
}
