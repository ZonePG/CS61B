package gitlet;

import java.io.File;

public class Branch {
    public static final File BRANCHE_DIR = Utils.join(Repository.GITLET_DIR, "/branches");

    public static void setCommitId(String branchName, String commitId) {
        File branchFile = Utils.join(BRANCHE_DIR, branchName);
        Utils.writeContents(branchFile, commitId);
    }

    public static String getCommitId(String branchName) {
        File branchFile = Utils.join(BRANCHE_DIR, branchName);
        if (!branchFile.exists()) {
            return null;
        } else {
            return Utils.readContentsAsString(branchFile);
        }
    }
}
