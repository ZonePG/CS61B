package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class StagingArea implements Serializable {

    // fileName -> blobId
    private HashMap<String, String> addition;
    // fileName
    private HashSet<String> removal;

    public static final File STAGE_FILE = Utils.join(Repository.GITLET_DIR, "/staging_area");

    public StagingArea() {
        addition = new HashMap<>();
        removal = new HashSet<>();
    }

    public static StagingArea load() {
        return Utils.readObject(STAGE_FILE, StagingArea.class);
    }

    public void save() {
        Utils.writeObject(STAGE_FILE, this);
    }

    public HashMap<String, String> getAddition() {
        return addition;
    }

    public HashSet<String> getRemoval() {
        return removal;
    }

    public void clear() {
        addition.clear();
        removal.clear();
    }
}
