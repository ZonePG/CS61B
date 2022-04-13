package gitlet;

import java.io.File;

public class HEAD {
    public static final File HEAD_FILE = Utils.join(Repository.GITLET_DIR, "head");

    public static void setBranchName(String branchName) {
        Utils.writeContents(HEAD_FILE, branchName);
    }

    public static String getBranchName() {
        return Utils.readContentsAsString(HEAD_FILE);
    }
}
