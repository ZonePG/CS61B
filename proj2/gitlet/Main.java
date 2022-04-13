package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        validateNotEmptyArgs(args);

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validateNumArgs(args, 1);
                Repository.initCommand();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateCWD();
                validateNumArgs(args, 2);
                Repository.addCommand(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                validateCWD();
                validateNumArgs(args, 2);
                Repository.commitCommand(args[1]);
                break;
            case "rm":
                validateCWD();
                validateNumArgs(args, 2);
                Repository.rmCommand(args[1]);
                break;
            case "log":
                validateCWD();
                validateNumArgs(args, 1);
                Repository.logCommand();
                break;
            case "global-log":
                validateCWD();
                validateNumArgs(args, 1);
                Repository.globalLogCommand();
                break;
            case "find":
                validateCWD();
                validateNumArgs(args, 2);
                Repository.findCommand(args[1]);
                break;
            case "status":
                validateCWD();
                validateNumArgs(args, 1);
                Repository.statusCommand();
                break;
            case "checkout":
                validateCWD();
                Repository.checkoutCommand(args);
                break;
            case "branch":
                validateCWD();
                validateNumArgs(args, 2);
                Repository.branchCommand(args[1]);
                break;
            case "rm-branch":
                validateCWD();
                validateNumArgs(args, 2);
                Repository.rmBranchCommand(args[1]);
                break;
            case "reset":
                validateCWD();
                validateNumArgs(args, 2);
                Repository.resetCommand(args[1]);
                break;
            case "merge":
                validateCWD();
                validateNumArgs(args, 2);
                Repository.mergeCommand(args[1]);
                break;
            default:
                validateCWD();
                Utils.exitWithMessage("No command with that name exists.");
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNotEmptyArgs(String[] args) {
        if (args.length == 0) {
            Utils.exitWithMessage("Please enter a command.");
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            Utils.exitWithMessage("Incorrect operands.");
        }
    }

    public static void validateCWD() {
        if (!Repository.GITLET_DIR.exists()) {
            Utils.exitWithMessage("Not in an initialized Gitlet directory.");
        }
    }
}
