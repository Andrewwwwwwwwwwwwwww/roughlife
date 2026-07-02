package io.github.andrewwwwwwwwwwwwwww.roughlife.client;

/** Client-side copy of the synced survival stats, written by the network receiver. */
public final class ClientStats {
    private ClientStats() {}

    public static volatile int thirst = 20;
    public static volatile int temperature = 20;
}
