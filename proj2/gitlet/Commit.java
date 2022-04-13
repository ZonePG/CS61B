package gitlet;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Represents a gitlet commit object. does at a high level.
 *
 * @author ZonePG
 */
public class Commit implements Serializable {

    /**
     * The message of this Commit.
     */
    public static final File COMMITS_DIR = Utils.join(Repository.GITLET_DIR, "/commits");
    private final String message;
    private final Date timestamp;
    private final String firstParentId;
    private final String secondParentId;
    private final String hash;
    // fileName, blobId
    private HashMap<String, String> blobs;

    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.firstParentId = null;
        this.secondParentId = null;
        hash = calcHash();
        blobs = new HashMap<>();
    }

    public Commit(String message, String firstParentId, String secondParentId) {
        this.message = message;
        this.timestamp = new Date();
        this.firstParentId = firstParentId;
        this.secondParentId = secondParentId;
        hash = calcHash();

        Commit firstParentCommit = load(firstParentId);
        blobs = new HashMap<>();
        blobs.putAll(firstParentCommit.blobs);
    }

    private String calcHash() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return Utils.sha1((Object) bos.toByteArray());
    }

    public String getHash() {
        return hash;
    }

    public String getFirstParentId() {
        return firstParentId;
    }

    public String getSecondParentId() {
        return secondParentId;
    }

    public void save() {
        File file = Utils.join(COMMITS_DIR, this.getHash());
        Utils.writeObject(file, this);
    }

    public void saveOnRemoteGitPath(String remoteGitPath) {
        File file = Utils.join(remoteGitPath + "/commits", this.getHash());
        Utils.writeObject(file, this);
    }

    public static Commit load(String commitId) {
        if (commitId.length() < 40) {
            List<String> commitIdList = Utils.plainFilenamesIn(COMMITS_DIR);
            assert commitIdList != null;
            for (String findCommitId : commitIdList) {
                if (findCommitId.startsWith(commitId)) {
                    commitId = findCommitId;
                    break;
                }
            }
        }

        File file = Utils.join(COMMITS_DIR, commitId);
        if (!file.exists()) {
            return null;
        } else {
            return Utils.readObject(file, Commit.class);
        }
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public String getBlobId(String fileName) {
        return blobs.get(fileName);
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        String dummyString = "===\n";
        String commitString = String.format("commit %s\n", hash);

        String pattern = "EEE MMM dd HH:mm:ss yyyy Z";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("en", "US"));
        String date = simpleDateFormat.format(timestamp);
        String dateString = String.format("Date: %s\n", date);

        String messageString = String.format("%s\n", message);
        return dummyString + commitString + dateString + messageString;
    }
}
