import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleTextUI {

    public static void main(String[] argv) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;
        CmdBranch here = root;
        padTo(41, 0, "=");
        System.out.println();
        prompt(here);
        while ((s = in.readLine()) != null && s.length() != 0) {
            padTo(41, 0, "=");
            System.out.println();
            try {
                here = modify(here, s);
            } catch (Exception e) {
                System.out.println("Err: " + e.getMessage());
            }
            prompt(here);
        }
    }

    private static CmdBranch modify(CmdBranch here, String s) {
        int i = cvToInt(s);
        if (i > here.children.size()) {
            throw new RuntimeException("Invalid Input, Try again");
        }
        if (i == 0) {
            if(here.parent == null) {
                throw new RuntimeException("Can't go back");
            }
            here = here.parent;
        } else {
            here = here.children.get(i - 1);
            if (here.children.isEmpty()) {
                // We're at a leaf, execute
                execute(here);

                // Go back up 1 level
                here = here.parent;
            }
        }
        return here;
    }

    private static void execute(CmdBranch here) {
        List<CmdBranch> ancestors = here.getAncestors();
        System.out.print("Executing:");
		String[] cmdParts = new String[ancestors.size()];
		int i = 0;
        for (CmdBranch anc : ancestors) {
            System.out.print(" " + anc.cmd);
            cmdParts[i] = anc.cmd;
            i++;
        }
        System.out.println();
        Exec.run(cmdParts);
    }

    private static void prompt(CmdBranch here) {
        System.out.print("|");
        int length = 0;
        for (CmdBranch anc : here.getAncestors()) {
            System.out.print(" " + anc.label + " |");
            length += anc.label.length() + 3;
        }
        padTo(40, length, "|");
        System.out.println();
        System.out.println();
        
        int i = 0;
        printOption(i, "Go Back");
        for (CmdBranch child : here.children) {
            printOption(++i, child.label);
        }
        System.out.println();
        System.out.print("> ");
    }

    private static void printOption(int i, String label) {
        String key = intToCv(i);
        padTo(5, key.length(), " ");
        System.out.print(key);
        System.out.print(" : ");
        System.out.println(label);
    }

    private static void padTo(int i, int filled, String c) {
        while (i-- > filled) {
            System.out.print(c);
        }
    }

    private static class CmdBranch {
        final String cmd;
        final String label;
        final List<CmdBranch> children = new ArrayList<CmdBranch>();
        final CmdBranch parent;

        List<CmdBranch> getAncestors() {
            List<CmdBranch> ret = new ArrayList<CmdBranch>();
            CmdBranch it = this;
            while(it.parent != null){
                ret.add(it);
                it = it.parent;

            }
            Collections.reverse(ret);
            return ret;
        }

        public CmdBranch(CmdBranch parent, String cmd) {
            this(parent, cmd, cmd);
        }

        public CmdBranch(CmdBranch parent, String cmd, String label) {
            this.cmd = cmd;
            this.label = label;
            this.parent = parent;
            if (parent != null) {
                parent.children.add(this);
            }
        }

        @Override
        public String toString() {
            return label;
        }

    }

    static String intToCv(int i) {
        String ret = "";
        do {
            ret = (i % 2 == 0 ? "c" : "v") + ret;
            i = i / 2;
        } while (i > 0);
        return ret;
    }

    static int cvToInt(String cv) {
        if (cv == null || cv.length() == 0) {
            throw new RuntimeException("Invalid Input, Try again");
        }
        if (cv.startsWith("c") && cv.length() > 1) {
            throw new RuntimeException("Invalid Input, Try again");            
        }
        int i = 0;
        for (byte c : cv.getBytes()) {
            i = i * 2;
            if ((byte) 'v' == c) {
                i++;
            } else if ((byte) 'c' != c) {
                throw new RuntimeException("Invalid Input, Try again");
            }
        }
        return i;
    }

    // This part is application specific
    static CmdBranch root = new CmdBranch(null, null);
    static {
        new CmdBranch(root, "qmc2", "games");
        new CmdBranch(root, "projectM-pulseaudio", "visuals");
        CmdBranch movie = new CmdBranch(root, "mplayer", "movies");
        File movieDir = new File("/home/jeremyc/Vids");
        for (File mov : movieDir.listFiles()) {
            new CmdBranch(movie, "-fs " + mov.getAbsolutePath(), mov.getName());
        }
        new CmdBranch(movie, movieDir.getAbsolutePath() + "/*", "All");
        CmdBranch kill = new CmdBranch(root, "killall", "kill");
        new CmdBranch(kill, "-9 projectM-pulseaudio", "visuals");
        new CmdBranch(kill, "qmc2", "video games");
        new CmdBranch(kill, "mplayer", "movies");
        new CmdBranch(kill, "xwii", "wiiMote (careful)");
    }
}
