package coolclk.bedwarsgames.util;

import java.io.File;
import java.net.URI;

public class FileBuilder {
    public interface Predicate<T> {
        boolean predicate(T object);
    }

    private File file;

    private FileBuilder(File file) {
        this.file = file;
    }

    private <T extends File> FileBuilder setFile(T other) {
        this.file = other;
        return this;
    }

    private boolean predicate;
    public FileBuilder ifPredicate(Predicate<File> predicate) {
        this.predicate = predicate.predicate(this.file);
        return this;
    }

    public <T extends File> FileBuilder orFile(T other) {
        return !this.predicate ? this.setFile(other) : this;
    }

    public File build() {
        return this.file;
    }

    @SuppressWarnings("unused")
    public static FileBuilder create(String pathname) {
        return new FileBuilder(new File(pathname));
    }

    @SuppressWarnings("unused")
    public static FileBuilder create(URI uri) {
        return new FileBuilder(new File(uri));
    }

    @SuppressWarnings("unused")
    public static FileBuilder create(String parent, String child) {
        return create(new File(parent), child);
    }

    public static FileBuilder create(File parent, String child) {
        return new FileBuilder(new File(parent, child));
    }
}
