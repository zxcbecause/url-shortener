package io.github.zxcbecause.shortener.link;

public final class LinkExceptions {

    private LinkExceptions() {
    }

    public static class NotFound extends RuntimeException {
        public NotFound(String code) {
            super("Short link '" + code + "' does not exist");
        }
    }

    public static class Expired extends RuntimeException {
        public Expired(String code) {
            super("Short link '" + code + "' has expired");
        }
    }

    public static class AliasTaken extends RuntimeException {
        public AliasTaken(String alias) {
            super("Alias '" + alias + "' is already taken");
        }
    }
}
