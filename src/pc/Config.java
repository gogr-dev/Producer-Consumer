package pc;

import java.io.PrintStream;

/** Parsed command-line options for {@link ProducerConsumer}, with defaults. */
public final class Config {

    public int seconds = 20;
    public int producers = 2;
    public int consumers = 2;
    public int capacity = 5;
    public int maxSleepMs = 500;
    public String impl = "semaphore";
    public boolean quiet = false;
    public boolean help = false;

    /**
     * Parse {@code --key value} / {@code --flag} arguments.
     *
     * <p>For backward compatibility, three bare integers (e.g. {@code 20 4 2}) are also
     * accepted as {@code seconds producers consumers}, matching the original program.
     *
     * @throws IllegalArgumentException if an option is unknown or a value is invalid.
     */
    public static Config parse(String[] args) {
        Config c = new Config();

        // Legacy positional form: "<seconds> <producers> <consumers>".
        if (args.length == 3 && isInt(args[0]) && isInt(args[1]) && isInt(args[2])) {
            c.seconds = positive("seconds", args[0]);
            c.producers = positive("producers", args[1]);
            c.consumers = positive("consumers", args[2]);
            return c;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-h", "--help" -> c.help = true;
                case "--quiet" -> c.quiet = true;
                case "--seconds" -> c.seconds = positive("seconds", value(args, ++i, arg));
                case "--producers" -> c.producers = positive("producers", value(args, ++i, arg));
                case "--consumers" -> c.consumers = positive("consumers", value(args, ++i, arg));
                case "--capacity" -> c.capacity = positive("capacity", value(args, ++i, arg));
                case "--max-sleep" -> c.maxSleepMs = nonNegative("max-sleep", value(args, ++i, arg));
                case "--impl" -> c.impl = impl(value(args, ++i, arg));
                default -> throw new IllegalArgumentException("unknown option '" + arg + "'");
            }
        }
        return c;
    }

    public static void printUsage(PrintStream out) {
        out.println("""
                Usage: java pc.ProducerConsumer [options]
                       java pc.ProducerConsumer <seconds> <producers> <consumers>   (legacy form)

                Options:
                  --seconds N     how long to run the simulation   (default 20)
                  --producers N   number of producer threads       (default 2)
                  --consumers N   number of consumer threads        (default 2)
                  --capacity N    bounded-buffer capacity           (default 5)
                  --max-sleep MS  max random sleep per operation    (default 500)
                  --impl NAME     semaphore | monitor | lock | queue (default semaphore)
                  --quiet         print only the final summary
                  -h, --help      show this help

                Examples:
                  java pc.ProducerConsumer
                  java pc.ProducerConsumer --seconds 5 --producers 4 --consumers 2 --impl lock
                  java pc.ProducerConsumer --seconds 10 --quiet""");
    }

    private static String value(String[] args, int i, String flag) {
        if (i >= args.length) {
            throw new IllegalArgumentException("missing value for " + flag);
        }
        return args[i];
    }

    private static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int positive(String name, String s) {
        int v = parseInt(name, s);
        if (v < 1) {
            throw new IllegalArgumentException(name + " must be >= 1, got " + v);
        }
        return v;
    }

    private static int nonNegative(String name, String s) {
        int v = parseInt(name, s);
        if (v < 0) {
            throw new IllegalArgumentException(name + " must be >= 0, got " + v);
        }
        return v;
    }

    private static int parseInt(String name, String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be an integer, got '" + s + "'");
        }
    }

    private static String impl(String s) {
        if (!Buffers.IMPLS.contains(s)) {
            throw new IllegalArgumentException("impl must be one of " + Buffers.IMPLS + ", got '" + s + "'");
        }
        return s;
    }
}
