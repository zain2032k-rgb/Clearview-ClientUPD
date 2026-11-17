package com.clearview;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/** Makes the keyboard sounds from scratch (no audio files) and plays them through a small mixer thread. */
public final class KeySynth {
    private static final int RATE = 44100;
    private static final int VARIANTS = 6;
    private static final int CHUNK = 256;

    private static final int PROFILES = KeyProfile.values().length;
    private static final short[][][] PRESS = new short[PROFILES][VARIANTS][];
    private static final short[][][] RELEASE = new short[PROFILES][VARIANTS][];
    private static final short[][][] SPACE = new short[PROFILES][VARIANTS][];

    private static final List<Voice> VOICES = new CopyOnWriteArrayList<>();
    private static final Random RND = new Random();

    private static volatile boolean ready;
    private static volatile boolean failed;
    private static boolean started;

    private static final class Voice {
        final short[] data;
        final float gl;
        final float gr;
        int pos;

        Voice(short[] data, float gl, float gr) {
            this.data = data;
            this.gl = gl;
            this.gr = gr;
        }
    }

    public static synchronized void init() {
        if (started) return;
        started = true;
        Thread t = new Thread(KeySynth::run, "ClearView-Keys");
        t.setDaemon(true);
        t.start();
    }

    public static void play(KeyProfile profile, boolean release, boolean space, int key, int volume) {
        if (!started) init();
        if (!ready || failed || volume <= 0) return;
        if (VOICES.size() > 24) return;
        int p = profile.ordinal();
        int v = RND.nextInt(VARIANTS);
        short[] data = release ? RELEASE[p][v] : (space ? SPACE[p][v] : PRESS[p][v]);
        float pan = (((key * 31 + 7) & 0xFF) / 255f - 0.5f) * 0.7f;
        float vol = (volume / 100f) * (release ? 0.8f : 1f);
        VOICES.add(new Voice(data, vol * (pan > 0 ? 1f - pan : 1f), vol * (pan < 0 ? 1f + pan : 1f)));
    }

    private static void run() {
        try {
            build();
            AudioFormat fmt = new AudioFormat(RATE, 16, 2, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, 1024 * 4);
            line.start();
            ready = true;

            float[] acc = new float[CHUNK * 2];
            byte[] out = new byte[CHUNK * 4];
            while (true) {
                Arrays.fill(acc, 0f);
                for (Voice v : VOICES) {
                    int n = Math.min(CHUNK, v.data.length - v.pos);
                    for (int i = 0; i < n; i++) {
                        float s = v.data[v.pos + i] / 32768f;
                        acc[2 * i] += s * v.gl;
                        acc[2 * i + 1] += s * v.gr;
                    }
                    v.pos += n;
                }
                VOICES.removeIf(v -> v.pos >= v.data.length);
                for (int i = 0; i < acc.length; i++) {
                    int iv = (int) (Math.tanh(acc[i]) * 32767.0);
                    out[2 * i] = (byte) iv;
                    out[2 * i + 1] = (byte) (iv >> 8);
                }
                line.write(out, 0, out.length);
            }
        } catch (Throwable t) {
            failed = true;
        }
    }

    private static void build() {
        Random r = new Random(1234);
        for (KeyProfile kp : KeyProfile.values()) {
            for (int v = 0; v < VARIANTS; v++) {
                PRESS[kp.ordinal()][v] = tone(r, kp, false, false);
                RELEASE[kp.ordinal()][v] = tone(r, kp, true, false);
                SPACE[kp.ordinal()][v] = tone(r, kp, false, true);
            }
        }
    }

    // {frequency Hz, amplitude, decay ms}
    private static double[][] partials(KeyProfile kp) {
        switch (kp) {
            case THOCKY:
                return new double[][]{{95, 1.0, 95}, {190, 0.6, 60}, {330, 0.3, 40}};
            case CLICKY:
                return new double[][]{{180, 0.5, 40}, {2800, 0.5, 14}, {4200, 0.3, 10}};
            case MARBLE:
                return new double[][]{{160, 0.5, 45}, {900, 0.6, 55}, {1450, 0.45, 40}, {2300, 0.3, 30}};
            default:
                return new double[][]{{140, 0.9, 70}, {280, 0.5, 50}, {470, 0.25, 35}};
        }
    }

    // {noise amount, noise decay ms, noise smoothing 0..1, length ms, attack ms}
    private static double[] noise(KeyProfile kp) {
        switch (kp) {
            case THOCKY:
                return new double[]{0.25, 10, 0.93, 180, 1.0};
            case CLICKY:
                return new double[]{0.7, 6, 0.35, 80, 0.3};
            case MARBLE:
                return new double[]{0.3, 8, 0.6, 130, 0.5};
            default:
                return new double[]{0.35, 14, 0.88, 150, 1.2};
        }
    }

    private static short[] tone(Random r, KeyProfile kp, boolean release, boolean space) {
        double[][] parts = partials(kp);
        double[] nz = noise(kp);
        double freqMul = (release ? 1.35 : space ? 0.78 : 1.0) * (1 + (r.nextDouble() - 0.5) * 0.08);
        double ampMul = release ? 0.45 : space ? 1.15 : 1.0;
        double decMul = release ? 0.5 : space ? 1.25 : 1.0;
        double lenMs = nz[3] * (release ? 0.5 : space ? 1.3 : 1.0);
        double attackMs = nz[4] * (release ? 0.6 : 1.0);

        double[] amps = new double[parts.length];
        for (int i = 0; i < parts.length; i++) amps[i] = parts[i][1] * ampMul * (1 + (r.nextDouble() - 0.5) * 0.2);

        int n = (int) (RATE * lenMs / 1000.0);
        short[] out = new short[n];
        double lp = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) RATE;
            double tm = t * 1000.0;
            double s = 0;
            for (int p = 0; p < parts.length; p++) {
                s += amps[p] * Math.sin(2 * Math.PI * parts[p][0] * freqMul * t) * Math.exp(-tm / (parts[p][2] * decMul));
            }
            double white = r.nextDouble() * 2 - 1;
            lp = nz[2] * lp + (1 - nz[2]) * white;
            s += nz[0] * ampMul * lp * 3.0 * Math.exp(-tm / (nz[1] * decMul));
            s *= Math.min(1.0, tm / attackMs);
            s *= Math.min(1.0, (n - i) / (n * 0.1));
            out[i] = (short) (Math.tanh(s * 0.8) * 0.85 * 32767.0);
        }
        return out;
    }

    private KeySynth() {}
}
