package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository. does at a high level.
 *
 * @author ZonePG
 */
public class Repository {

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     *
     */
    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        Commit.COMMITS_DIR.mkdir();
        Blob.BLOBS_DIR.mkdir();
        Branch.BRANCHE_DIR.mkdir();
        Remote.REMOTE_DIR.mkdir();

        Commit initialCommit = new Commit();
        initialCommit.save();

        StagingArea stagingArea = new StagingArea();
        stagingArea.save();

        Branch.setCommitId("master", initialCommit.getHash());
        HEAD.setBranchName("master");
    }

    public static void addCommand(String fileName) {
        File newFile = Utils.join(CWD, fileName);
        if (!newFile.exists()) {
            Utils.exitWithMessage("File does not exist.");
        }

        Blob newBlob = new Blob(readContents(newFile));
        String newBlobId = newBlob.getId();
        Commit currentCommit = Commit.load(Branch.getCommitId(HEAD.getBranchName()));
        StagingArea stagingArea = StagingArea.load();
        // If the current working version of the file is identical to the version in the current
        // commit, do not stage it to be added
        if (newBlobId.equals(currentCommit.getBlobId(fileName))) {
            stagingArea.clear();
            stagingArea.save();
            return;
        }
        // and remove it from the staging area if it is already there
        if (newBlobId.equals(stagingArea.getAddition().get(fileName))) {
            stagingArea.getAddition().remove(fileName);
        }
        stagingArea.getAddition().put(fileName, newBlobId);
        // The file will no longer be staged for removal (see gitlet rm), if it was at the time
        // of the command.
        stagingArea.getRemoval().remove(fileName);
        newBlob.save();
        stagingArea.save();
    }

    private static void commit(String message, String currentCommitId, String mergedCommitId) {
        StagingArea stagingArea = StagingArea.load();
        if (stagingArea.getAddition().isEmpty() && stagingArea.getRemoval().isEmpty()) {
            exitWithMessage("No changes added to the commit.");
        }
        if (message.isEmpty()) {
            exitWithMessage("Please enter a commit message.");
        }

        // A commit will save and start tracking any files that were staged for addition
        // but were not tracked by its parent.
        Commit newCommit = new Commit(message, currentCommitId, mergedCommitId);
        for (Map.Entry<String, String> entry : stagingArea.getAddition().entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            newCommit.getBlobs().put(fileName, blobId);
        }
        for (String fileName : stagingArea.getRemoval()) {
            newCommit.getBlobs().remove(fileName);
        }

        Branch.setCommitId(HEAD.getBranchName(), newCommit.getHash());
        stagingArea.clear();
        stagingArea.save();
        newCommit.save();
    }

    public static void commitCommand(String message) {
        String currentCommitId = Branch.getCommitId(HEAD.getBranchName());
        commit(message, currentCommitId, null);
    }

    public static void rmCommand(String fileName) {
        StagingArea stagingArea = StagingArea.load();
        if (stagingArea.getAddition().containsKey(fileName)) {
            stagingArea.getAddition().remove(fileName);
            stagingArea.save();
            return;
        }

        Commit currentCommit = Commit.load(Branch.getCommitId(HEAD.getBranchName()));
        if (currentCommit.getBlobs().containsKey(fileName)) {
            stagingArea.getRemoval().add(fileName);
            Utils.join(CWD, fileName).delete();
            stagingArea.save();
            return;
        }

        exitWithMessage("No reason to remove the file.");
    }

    public static void logCommand() {
        String commitId = Branch.getCommitId(HEAD.getBranchName());
        while (commitId != null) {
            Commit commit = Commit.load(commitId);
            assert commit != null;
            System.out.println(commit);
            commitId = commit.getFirstParentId();
        }
    }

    public static void globalLogCommand() {
        List<String> commitIdList = Utils.plainFilenamesIn(Commit.COMMITS_DIR);
        assert commitIdList != null;
        for (String commitId : commitIdList) {
            Commit commit = Commit.load(commitId);
            assert commit != null;
            System.out.println(commit);
        }
    }

    public static void findCommand(String message) {
        List<String> commitIdList = Utils.plainFilenamesIn(Commit.COMMITS_DIR);
        assert commitIdList != null;
        StringBuilder builder = new StringBuilder();
        for (String commitId : commitIdList) {
            Commit commit = Commit.load(commitId);
            assert commit != null;
            if (commit.getMessage().equals(message)) {
                builder.append(commitId).append("\n");
            }
        }
        String output = builder.toString();
        if (output.isEmpty()) {
            exitWithMessage("Found no commit with that message.");
        }
        System.out.println(output);
    }

    private static void printListString(List<String> stringList) {
        for (String string : stringList) {
            System.out.println(string);
        }
        System.out.println();
    }

    private static List<String> getUntrackedFiles(StagingArea stagingArea, Commit currentCommit,
            List<String> cwdFileNames) {
        List<String> result = new ArrayList<>();
        for (String fileName : cwdFileNames) {
            boolean tracked = currentCommit.getBlobs().containsKey(fileName);
            boolean staged = stagingArea.getAddition().containsKey(fileName);
            // untracked files
            if (!staged && !tracked) {
                result.add(fileName);
            }
        }
        Collections.sort(result);
        return result;
    }

    private static List<String> getModificationsNotStagedForCommit(StagingArea stagingArea,
            Commit currentCommit, List<String> cwdFileNames) {
        List<String> result = new ArrayList<>();
        for (String fileName : cwdFileNames) {
            File file = Utils.join(CWD, fileName);
            Blob blob = new Blob(readContents(file));
            // case1: Tracked in the current commit, changed in the working directory, but not
            // staged; or
            boolean tracked = currentCommit.getBlobs().containsKey(fileName);
            boolean changed = !blob.getId().equals(currentCommit.getBlobs().get(fileName));
            boolean staged = stagingArea.getAddition().containsKey(fileName);
            if (tracked && changed && !staged) {
                result.add(fileName + " (modified)");
                continue;
            }
            // case2: Staged for addition, but with different contents than in the working
            // directory; or
            changed = !blob.getId().equals(stagingArea.getAddition().get(fileName));
            if (staged && changed) {
                result.add(fileName + " (modified)");
            }
        }
        // case3: Staged for addition, but deleted in the working directory; or
        for (String fileName : stagingArea.getAddition().keySet()) {
            if (!cwdFileNames.contains(fileName)) {
                result.add(fileName + " (deleted)");
            }
        }
        // case4: Not staged for removal, but tracked in the current commit and deleted from the
        // working directory.
        for (String fileName : currentCommit.getBlobs().keySet()) {
            boolean stagedForRemoval = stagingArea.getRemoval().contains(fileName);
            boolean cwdContains = cwdFileNames.contains(fileName);
            if (!stagedForRemoval && !cwdContains) {
                result.add(fileName + " (deleted)");
            }
        }
        Collections.sort(result);
        return result;
    }

    public static void statusCommand() {
        // print branches
        System.out.println("=== Branches ===");
        List<String> branches = Utils.plainFilenamesIn(Branch.BRANCHE_DIR);
        assert branches != null;
        System.out.println("*" + HEAD.getBranchName());
        Collections.sort(branches);
        for (String branch : branches) {
            if (!branch.equals(HEAD.getBranchName())) {
                System.out.println(branch);
            }
        }
        System.out.println();

        // print staged file
        StagingArea stagingArea = StagingArea.load();
        System.out.println("=== Staged Files ===");
        List<String> stagedFiles = new ArrayList<>(stagingArea.getAddition().keySet());
        printListString(stagedFiles);

        // print removed file
        System.out.println("=== Removed Files ===");
        List<String> removedFiles = new ArrayList<>(stagingArea.getRemoval());
        printListString(removedFiles);

        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit currentCommit = Commit.load(
                Objects.requireNonNull(Branch.getCommitId(HEAD.getBranchName())));
        List<String> cwdFileNames = Utils.plainFilenamesIn(CWD);
        assert cwdFileNames != null;
        List<String> modificationsNotStagedForCommit = getModificationsNotStagedForCommit(
                stagingArea, currentCommit, cwdFileNames);
        printListString(modificationsNotStagedForCommit);

        System.out.println("=== Untracked Files ===");
        List<String> untrackedFiles = getUntrackedFiles(stagingArea, currentCommit, cwdFileNames);
        printListString(untrackedFiles);
    }

    private static void checkoutFile(String commitId, String fileName) {
        Commit commit = Commit.load(commitId);
        if (commit == null) {
            exitWithMessage("No commit with that id exists.");
            return;
        }
        String blobId = commit.getBlobs().get(fileName);
        if (blobId == null) {
            exitWithMessage("File does not exist in that commit.");
        }
        byte[] blobContents = Utils.readContents(Utils.join(Blob.BLOBS_DIR, blobId));
        Utils.writeContents(Utils.join(CWD, fileName), (Object) blobContents);
    }

    private static void checkoutCommit(Commit commit) {
        StagingArea stagingArea = StagingArea.load();
        String currentCommitId = Branch.getCommitId(HEAD.getBranchName());
        assert currentCommitId != null;
        Commit currentCommit = Commit.load(currentCommitId);
        assert currentCommit != null;
        List<String> cwdFileNames = Utils.plainFilenamesIn(CWD);
        assert cwdFileNames != null;
        Set<String> fileNames = commit.getBlobs().keySet();
        List<String> untrackedFiles = getUntrackedFiles(stagingArea, currentCommit, cwdFileNames);
        if (!untrackedFiles.isEmpty()) {
            for (String untrackedFileName : untrackedFiles) {
                if (fileNames.contains(untrackedFileName)) {
                    exitWithMessage(
                            "There is an untracked file in the way; delete it, or add and commit "
                                    + "it first.");
                }
            }
        }
        if (commit.getHash().equals(currentCommitId)) {
            return;
        }
        // Takes all files in the commit at the head of the given branch, and puts them in the
        // working directory,
        // overwriting the versions of the files that are already there if they exist.
        for (String fileName : fileNames) {
            String blobId = commit.getBlobs().get(fileName);
            byte[] blobContents = Utils.readContents(Utils.join(Blob.BLOBS_DIR, blobId));
            Utils.writeContents(Utils.join(CWD, fileName), (Object) blobContents);
        }

        for (String fileName : currentCommit.getBlobs().keySet()) {
            if (!fileNames.contains(fileName)) {
                Utils.join(CWD, fileName).delete();
            }
        }
        // The staging area is cleared,
        stagingArea.clear();
        stagingArea.save();
    }

    private static void checkoutBranch(String branchName) {
        String commitId = Branch.getCommitId(branchName);
        if (commitId == null) {
            exitWithMessage("No such branch exists.");
            return;
        }
        if (branchName.equals(HEAD.getBranchName())) {
            exitWithMessage("No need to checkout the current branch.");
        }
        Commit commit = Commit.load(commitId);
        checkoutCommit(commit);
        // the given branch will now be considered the current branch (HEAD).
        HEAD.setBranchName(branchName);
    }

    public static void checkoutCommand(String[] args) {
        // checkout -- [file name]
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                exitWithMessage("Incorrect operands.");
            }
            checkoutFile(Branch.getCommitId(HEAD.getBranchName()), args[2]);
            return;
        }

        // checkout [commit id] -- [file name]
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                exitWithMessage("Incorrect operands.");
            }
            checkoutFile(args[1], args[3]);
            return;
        }

        // checkout [branch name]
        if (args.length == 2) {
            checkoutBranch(args[1]);
            return;
        }

        exitWithMessage("Incorrect operands.");
    }

    public static void branchCommand(String branchName) {
        if (Utils.join(Branch.BRANCHE_DIR, branchName).exists()) {
            exitWithMessage("A branch with that name already exists.");
        }

        String currentCommitId = Branch.getCommitId(HEAD.getBranchName());
        Branch.setCommitId(branchName, currentCommitId);
    }

    public static void rmBranchCommand(String branchName) {
        File findBranchFile = Utils.join(Branch.BRANCHE_DIR, branchName);
        if (!findBranchFile.exists()) {
            exitWithMessage("A branch with that name does not exist.");
        }
        if (HEAD.getBranchName().equals(branchName)) {
            exitWithMessage("Cannot remove the current branch.");
        }
        findBranchFile.delete();
    }

    public static void resetCommand(String commitId) {
        Commit commit = Commit.load(commitId);
        if (commit == null) {
            exitWithMessage("No commit with that id exists.");
            return;
        }
        checkoutCommit(commit);
        Branch.setCommitId(HEAD.getBranchName(), commit.getHash());
    }

    private static String getSplitPointCommitId(String currentCommitId, String mergedCommitId) {
        Set<String> commitSet = new HashSet<>();
        Queue<String> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(currentCommitId);
        while (!bfsQueue.isEmpty()) {
            String commitId = bfsQueue.remove();
            Commit commit = Commit.load(commitId);
            commitSet.add(commitId);
            if (commit.getFirstParentId() != null) {
                bfsQueue.add(commit.getFirstParentId());
            }
            if (commit.getSecondParentId() != null) {
                bfsQueue.add(commit.getSecondParentId());
            }
        }

        bfsQueue.add(mergedCommitId);
        while (!bfsQueue.isEmpty()) {
            String commitId = bfsQueue.remove();
            Commit commit = Commit.load(commitId);
            if (commitSet.contains(commitId)) {
                return commitId;
            }
            if (commit.getFirstParentId() != null) {
                bfsQueue.add(commit.getFirstParentId());
            }
            if (commit.getSecondParentId() != null) {
                bfsQueue.add(commit.getSecondParentId());
            }
        }

        return null;
    }


    private static String conflictFileContents(String currentBlobId, String mergedBlobId) {
        String currentContents;
        String mergedContents;
        if (currentBlobId == null) {
            currentContents = "";
        } else {
            currentContents = Utils.readContentsAsString(Utils.join(Blob.BLOBS_DIR, currentBlobId));
        }
        if (mergedBlobId == null) {
            mergedContents = "";
        } else {
            mergedContents = Utils.readContentsAsString(Utils.join(Blob.BLOBS_DIR, mergedBlobId));
        }
        return "<<<<<<< HEAD\n" + currentContents + "=======\n" + mergedContents + ">>>>>>>\n";
    }

    private static void processConflict(StagingArea stagingArea, String fileName,
            String currentBlobId, String mergedBlobId) {
        String newContents = conflictFileContents(currentBlobId, mergedBlobId);
        Blob newBlob = new Blob(newContents.getBytes(StandardCharsets.UTF_8));
        newBlob.save();
        File file = Utils.join(CWD, fileName);
        Utils.writeContents(file, newContents);
        stagingArea.getAddition().put(fileName, newBlob.getId());
    }

    private static boolean processMerge(StagingArea stagingArea, Commit splitPointCommit,
            Commit currentCommit, Commit mergedCommit) {
        boolean conflict = false;
        HashMap<String, String> splitBlobs = splitPointCommit.getBlobs();
        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        HashMap<String, String> mergedBlobs = mergedCommit.getBlobs();
        for (String fileName : mergedBlobs.keySet()) {
            // modified in the given branch since the split point
            String mergedBlobId = mergedBlobs.get(fileName);
            String splitBlobId = splitBlobs.get(fileName);
            String currentBlobId = currentBlobs.get(fileName);
            // case1: Any files that have been modified in the given branch since the split
            // point, but not modified in the current branch since the split point should be
            // changed to their versions in the given branch
            if (splitBlobId != null && !mergedBlobId.equals(splitBlobId)) {
                if (splitBlobId.equals(currentBlobId)) {
                    checkoutFile(mergedCommit.getHash(), fileName);
                    stagingArea.getAddition().put(fileName, mergedBlobId);
                    continue;
                }
            }
            // case3: keep same

            // case5: Any files that were not present at the split point and are present only in
            // the given branch should be checked out and staged.
            if (splitBlobId == null && currentBlobId == null) {
                checkoutFile(mergedCommit.getHash(), fileName);
                stagingArea.getAddition().put(fileName, mergedBlobId);
                continue;
            }
            // case7: keep same

            // case8: or the contents of one are changed and the other file is deleted,
            if (splitBlobId != null && !mergedBlobId.equals(splitBlobId) && currentBlobId == null) {
                conflict = true;
                processConflict(stagingArea, fileName, currentBlobId, mergedBlobId);
            }
        }

        for (String fileName : currentBlobs.keySet()) {
            String currentBlobId = currentBlobs.get(fileName);
            String splitBlobId = splitBlobs.get(fileName);
            String mergedBlobId = mergedBlobs.get(fileName);
            // case2: keep same
            // case4: keep same
            // case6: Any files present at the split point, unmodified in the current branch, and
            // absent in the given branch should be removed (and untracked).
            if (currentBlobId.equals(splitBlobId)) {
                if (mergedBlobId == null) {
                    Utils.join(CWD, fileName).delete();
                    stagingArea.getRemoval().add(fileName);
                    continue;
                }
            }
            // case8: the contents of both are changed and different from other
            if (splitBlobId != null && mergedBlobId != null) {
                if (!currentBlobId.equals(splitBlobId) && !mergedBlobId.equals(splitBlobId)) {
                    if (!currentBlobId.equals(mergedBlobId)) {
                        conflict = true;
                        processConflict(stagingArea, fileName, currentBlobId, mergedBlobId);
                    }
                }
            }
            // case8: or the contents of one are changed and the other file is deleted,
            if (splitBlobId != null && !currentBlobId.equals(splitBlobId) && mergedBlobId == null) {
                conflict = true;
                processConflict(stagingArea, fileName, currentBlobId, mergedBlobId);
            }
            // case8: or the file was absent at the split point and has different contents in the
            // given and current branches.
            if (splitBlobId == null && currentBlobId != null && mergedBlobId != null) {
                if (!currentBlobId.equals(mergedBlobId)) {
                    conflict = true;
                    processConflict(stagingArea, fileName, currentBlobId, mergedBlobId);
                }
            }
        }
        return conflict;
    }

    public static void mergeCommand(String branchName) {
        StagingArea stagingArea = StagingArea.load();
        if (!stagingArea.getAddition().isEmpty() || !stagingArea.getRemoval().isEmpty()) {
            exitWithMessage("You have uncommitted changes.");
        }
        String mergedCommitId = Branch.getCommitId(branchName);
        if (mergedCommitId == null) {
            exitWithMessage("A branch with that name does not exist.");
            return;
        }
        Commit mergedCommit = Commit.load(mergedCommitId);
        assert mergedCommit != null;
        if (HEAD.getBranchName().equals(branchName)) {
            exitWithMessage("Cannot merge a branch with itself.");
        }
        String currentCommitId = Branch.getCommitId(HEAD.getBranchName());
        assert currentCommitId != null;
        Commit currentCommit = Commit.load(currentCommitId);
        assert currentCommit != null;
        List<String> cwdFileNames = Utils.plainFilenamesIn(CWD);
        assert cwdFileNames != null;
        List<String> untrackedFiles = getUntrackedFiles(stagingArea, currentCommit, cwdFileNames);
        if (!untrackedFiles.isEmpty()) {
            for (String untrackedFileName : untrackedFiles) {
                if (mergedCommit.getBlobId(untrackedFileName) != null) {
                    exitWithMessage(
                            "There is an untracked file in the way; delete it, or add and commit "
                                    + "it first.");
                }
            }
        }
        if (!getUntrackedFiles(stagingArea, currentCommit, cwdFileNames).isEmpty()) {
            exitWithMessage(
                    "There is an untracked file in the way; delete it, or add and commit it first"
                            + ".");
        }
        String splitPointCommitId = getSplitPointCommitId(currentCommitId, mergedCommitId);
        // If the split point is the same commit as the given branch, then we do nothing;
        if (splitPointCommitId.equals(mergedCommitId)) {
            exitWithMessage("Given branch is an ancestor of the current branch.");
        }
        //  If the split point is the current branch, then the effect is to check out the given
        //  branch
        if (splitPointCommitId.equals(currentCommitId)) {
            checkoutCommit(mergedCommit);
            Branch.setCommitId(HEAD.getBranchName(), mergedCommitId);
            exitWithMessage("Current branch fast-forwarded.");
        }
        Commit splitPointCommit = Commit.load(splitPointCommitId);
        assert splitPointCommit != null;
        boolean conflict = processMerge(stagingArea, splitPointCommit, currentCommit,
                mergedCommit);

        stagingArea.save();
        commit(String.format("Merged %s into %s.", branchName, HEAD.getBranchName()),
                currentCommitId, mergedCommitId);
        if (conflict) {
            exitWithMessage("Encountered a merge conflict.");
        }
    }

    public static void addRemoteCommand(String remoteName, String remoteGitPath) {
        if (Remote.getRemoteGitPath(remoteName) != null) {
            exitWithMessage("A remote with that name already exists.");
        }

        remoteGitPath = remoteGitPath.replace("/", File.separator);
        File remoteDir = new File(remoteGitPath);
        Remote.addRemoteGitPath(remoteName, remoteGitPath);
    }

    public static void rmRemoteCommand(String remoteName) {
        if (Remote.getRemoteGitPath(remoteName) == null) {
            exitWithMessage("A remote with that name does not exist.");
        }
        Remote.removeRemoteGitPath(remoteName);
    }

    public static void pushCommand(String remoteName, String remoteBranchName) {
        String remoteGitPath = Remote.getRemoteGitPath(remoteName);
        String remoteCwd = remoteGitPath + "/..";
        if (remoteGitPath == null) {
            exitWithMessage("Remote directory not found.");
        }
        String localBranchName = HEAD.getBranchName();
        List<String> commitHistory = new ArrayList<>();

        String localCommitId = Branch.getCommitId(localBranchName);
        String remoteCommitId = Branch.getRemoteCommitId(remoteGitPath, remoteBranchName);
        boolean isHistory = false;
        while (localCommitId != null) {
            Commit commit = Commit.load(localCommitId);
            commitHistory.add(localCommitId);
            if (localCommitId.equals(remoteCommitId)) {
                isHistory = true;
                break;
            }
            assert commit != null;
            localCommitId = commit.getFirstParentId();
        }

        if (!isHistory) {
            exitWithMessage("Please pull down remote changes before pushing.");
        }

        for (String commitId : commitHistory) {
            Commit commit = Commit.load(commitId);
            assert commit != null;
            commit.saveOnRemoteGitPath(remoteGitPath);
        }
    }
}
