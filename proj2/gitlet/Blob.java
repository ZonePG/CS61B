package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    public static final File BLOBS_DIR = Utils.join(Repository.GITLET_DIR, "/blobs");

    private final byte[] contents;
    private final String blobId;

    public Blob(byte[] contents) {
        this.contents = contents;
        this.blobId = calcHash();
    }

    private String calcHash() {
        return Utils.sha1((Object) this.contents);
    }

    public String getId() {
        return blobId;
    }

    public void save() {
        Utils.writeContents(Utils.join(BLOBS_DIR, blobId), (Object) contents);
    }
}
