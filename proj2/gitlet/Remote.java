package gitlet;

import java.io.File;

public class Remote {

    public static final File REMOTE_DIR = Utils.join(Repository.GITLET_DIR, "/remote");

    public static void addRemoteGitPath(String remoteName, String remoteGitPath) {
        Utils.writeContents(Utils.join(REMOTE_DIR, remoteName), remoteGitPath);
    }

    public static void removeRemoteGitPath(String remoteName) {
        Utils.join(REMOTE_DIR, remoteName).delete();
    }

    public static String getRemoteGitPath(String remoteName) {
        File remoteFile = Utils.join(REMOTE_DIR, remoteName);
        if (!remoteFile.exists()) {
            return null;
        }
        return Utils.readContentsAsString(remoteFile);
    }
}
