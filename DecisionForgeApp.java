import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DecisionForgeApp extends JFrame {

    public DecisionForgeApp() {
        setTitle("DecisionForge AI - Discrete Structures Decision Support");
        setSize(920, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("Scenario B: CoursePath", buildCoursePathTab());
        tabs.addTab("Scenario D: SecureSteps", buildSecureStepsTab());
        add(tabs);
    }

    // SCENARIO B: COURSEPATH
    private JPanel buildCoursePathTab() {
        JCheckBox chkP = new JCheckBox("P: Prerequisite Completed", true);
        JCheckBox chkS = new JCheckBox("S: Seat Available", true);
        JCheckBox chkH = new JCheckBox("H: Hold Exists on Account", false);
        JCheckBox chkD = new JCheckBox("D: Deadline Not Passed", true);

        JTextArea[] views = createFourViews();
        JButton btnEval = new JButton("Evaluate CoursePath");

        Runnable evaluate = () -> {
            boolean P = chkP.isSelected(), S = chkS.isSelected(), H = chkH.isSelected(), D = chkD.isSelected();
            boolean eligible = P && S && !H && D;

            // Decision Tree Routing
            String outcome = !D ? "Registration Closed" : H ? "Resolve a Hold" : !P ? "Check Prerequisite" : !S ? "Choose Another Section" : "Ready to Register";
            String trace = String.format(
                "Deadline Open (D=%b) -> %s\n" +
                "No Hold (¬H=%b)      -> %s\n" +
                "Prereq Met (P=%b)    -> %s\n" +
                "Seat Open (S=%b)     -> Outcome: %s",
                D, !D ? "STOP: Registration Closed" : "Continue",
                !H, (!D ? "Skipped" : H ? "STOP: Resolve a Hold" : "Continue"),
                P, (!D || H ? "Skipped" : !P ? "STOP: Check Prerequisite" : "Continue"),
                S, outcome
            );

            // View 1: Explanation & Checklist
            views[0].setText(String.format(
                "RECOMMENDATION: %s\nRule: Eligible = P ∧ S ∧ ¬H ∧ D  -->  %b\n\n" +
                "CONDITION BREAKDOWN:\n" +
                "• [P] Prerequisite: %s\n• [S] Seat Open:    %s\n• [¬H] No Hold:     %s\n• [D] Deadline OK:  %s\n\n" +
                "OFFICIAL VERIFICATION CHECKLIST:\n" +
                "[ ] Confirm grade 'C' or higher in prerequisite on transcript.\n" +
                "[ ] Check student portal for bursar or advising holds.\n" +
                "[ ] Verify no schedule conflicts and confirm add/drop deadline.",
                outcome.toUpperCase(), eligible,
                status(P, "Missing prerequisite"), status(S, "Section full"),
                status(!H, "Active hold"), status(D, "Deadline passed")
            ));

            // View 2: Truth Table & De Morgan's Equivalence
            StringBuilder tt = new StringBuilder("P   S   H   D  | ¬H | Eligible | Routing Outcome\n--------------------------------------------------------\n");
            for (int i = 15; i >= 0; i--) {
                boolean p = (i & 8) != 0, s = (i & 4) != 0, h = (i & 2) != 0, d = (i & 1) != 0;
                boolean res = p && s && !h && d;
                String rowOut = !d ? "Deadline Passed" : h ? "Resolve a Hold" : !p ? "Check Prerequisite" : !s ? "Choose Another Section" : "Ready to Register";
                String mark = (p == P && s == S && h == H && d == D) ? " <-- CURRENT" : "";
                tt.append(String.format("%-3s %-3s %-3s %-2s | %-2s | %-8s | %s%s\n", b(p), b(s), b(h), b(d), b(!h), b(res), rowOut, mark));
            }
            tt.append("\nDE MORGAN'S LAW (INELIGIBILITY):\n¬(P ∧ S ∧ ¬H ∧ D)  ≡  ¬P ∨ ¬S ∨ H ∨ ¬D");
            views[1].setText(tt.toString());

            // View 3: Expression Tree & Decision Tree
            Node root = new Node("∧", new Node("∧", new Node("∧", new Node("P"), new Node("S")), new Node("¬", new Node("H"), null)), new Node("D"));
            views[2].setText(
                "EXPRESSION TREE: ((P ∧ S) ∧ ¬H) ∧ D\n" +
                "       [∧]\n      /   \\\n    [∧]    [D]\n   /   \\\n [∧]    [¬]\n / \\     |\n[P] [S] [H]\n\n" +
                "TRAVERSALS:\n" +
                "• Preorder:  " + root.preorder() + "\n" +
                "• Inorder:   " + root.inorder() + "\n" +
                "• Postorder: " + root.postorder() + "\n\n" +
                "DECISION TREE TRACE:\n" + trace
            );

            // View 4: Knowledge Graph
            Graph kg = new Graph();
            kg.link("Jordan (Student)", "requests", "Upper-Level Course");
            kg.link("Jordan (Student)", "has_status", "Hold (H=" + H + ")");
            kg.link("Upper-Level Course", "requires", "Prerequisite (P=" + P + ")");
            kg.link("Upper-Level Course", "has_capacity", "Seat Available (S=" + S + ")");
            kg.link("Upper-Level Course", "governed_by", "Deadline (D=" + D + ")");
            kg.link("Academic Advisor", "can_resolve", "Hold (H=" + H + ")");
            views[3].setText("COURSEPATH KNOWLEDGE GRAPH (ADJACENCY LIST):\n\n" + kg);
        };

        btnEval.addActionListener(e -> evaluate.run());
        evaluate.run();
        return assembleTab("CoursePath Inputs", new Component[]{chkP, chkS, chkH, chkD, new JLabel("Rule: P ∧ S ∧ ¬H ∧ D"), btnEval}, views);
    }

    // SCENARIO D:
    private JPanel buildSecureStepsTab() {
        JCheckBox chkP = new JCheckBox("P: Password Passed", true);
        JCheckBox chkV = new JCheckBox("V: 2FA Passed", true);
        JCheckBox chkL = new JCheckBox("L: Account Unlocked", true);

        JTextArea[] views = createFourViews();
        JButton btnEval = new JButton("Simulate Login");

        Runnable evaluate = () -> {
            boolean P = chkP.isSelected(), V = chkV.isSelected(), L = chkL.isSelected();
            boolean granted = P && V && L;

            String outcome = !L ? "Contact Support / Help Desk" : !P ? "Retry Password / Account Recovery" : !V ? "Retry 2FA Verification" : "Access Granted";
            String trace = String.format(
                "Account Unlocked (L=%b) -> %s\n" +
                "Password Valid (P=%b)   -> %s\n" +
                "2FA Verified (V=%b)     -> Outcome: %s",
                L, !L ? "STOP: Contact Support" : "Continue",
                P, (!L ? "Skipped" : !P ? "STOP: Retry / Recovery" : "Continue"),
                V, outcome
            );

            // View 1: Explanation
            views[0].setText(String.format(
                "OUTCOME: %s\nRule: AccessGranted = P ∧ V ∧ L  -->  %b\n\n" +
                "SECURITY GATE BREAKDOWN:\n" +
                "• [L] Account Unlocked: %s\n• [P] Password Check:   %s\n• [V] 2FA Verification: %s\n\n" +
                "RECOVERY GUIDANCE:\n%s",
                outcome.toUpperCase(), granted,
                status(L, "Account locked"), status(P, "Invalid password"), status(V, "2FA failed"),
                granted ? "• All checks passed. Session authorized." :
                !L ? "• Locked for security. Contact Help Desk to verify identity." :
                !P ? "• Check Caps Lock and retry, or use password reset." : "• Re-sync authenticator app or use a backup code."
            ));

            // View 2: Truth Table & De Morgan's Law
            StringBuilder tt = new StringBuilder("P   V   L  | Granted | Safe Recovery Outcome\n----------------------------------------------------\n");
            for (int i = 7; i >= 0; i--) {
                boolean p = (i & 4) != 0, v = (i & 2) != 0, l = (i & 1) != 0;
                String rowOut = !l ? "Contact Support" : !p ? "Retry / Recovery" : !v ? "Retry 2FA" : "Access Granted";
                String mark = (p == P && v == V && l == L) ? " <-- CURRENT" : "";
                tt.append(String.format("%-3s %-3s %-2s | %-7s | %s%s\n", b(p), b(v), b(l), b(p && v && l), rowOut, mark));
            }
            tt.append("\nDE MORGAN'S LAW (ACCESS DENIED):\n¬(P ∧ V ∧ L)  ≡  ¬P ∨ ¬V ∨ ¬L");
            views[1].setText(tt.toString());

            // View 3: Expression Tree & Decision Tree
            Node root = new Node("∧", new Node("∧", new Node("P"), new Node("V")), new Node("L"));
            views[2].setText(
                "EXPRESSION TREE: (P ∧ V) ∧ L\n" +
                "     [∧]\n    /   \\\n  [∧]    [L]\n /   \\\n[P]  [V]\n\n" +
                "TRAVERSALS:\n" +
                "• Preorder:  " + root.preorder() + "\n" +
                "• Inorder:   " + root.inorder() + "\n" +
                "• Postorder: " + root.postorder() + "\n\n" +
                "DECISION TREE TRACE:\n" + trace
            );

            // View 4: Knowledge Graph
            Graph kg = new Graph();
            kg.link("Sam (Account)", "submits", "Password Check (P=" + P + ")");
            kg.link("Sam (Account)", "completes", "2FA Step (V=" + V + ")");
            kg.link("Sam (Account)", "monitored_by", "Lockout Policy (L=" + L + ")");
            kg.link("Lockout Policy (L=" + L + ")", "escalates_to", "Help Desk / Support");
            kg.link("2FA Step (V=" + V + ")", "unlocks", "System Session");
            views[3].setText("SECURESTEPS KNOWLEDGE GRAPH (ADJACENCY LIST):\n\n" + kg);
        };

        btnEval.addActionListener(e -> evaluate.run());
        evaluate.run();
        return assembleTab("SecureSteps Simulator", new Component[]{chkP, chkV, chkL, new JLabel("Rule: P ∧ V ∧ L"), new JLabel("Mode: Learning Simulator"), btnEval}, views);
    }

    // EXPRESSION TREE & KNOWLEDGE GRAPH
    static class Node {
        String val;
        Node left, right;
        Node(String val) { this.val = val; }
        Node(String val, Node left, Node right) { this.val = val; this.left = left; this.right = right; }

        String preorder() {
            return (val + " " + (left != null ? left.preorder() : "") + " " + (right != null ? right.preorder() : "")).trim().replaceAll("\\s+", " ");
        }
        String inorder() {
            if (left == null && right == null) return val;
            if (right == null) return "(" + val + " " + left.inorder() + ")";
            return "(" + left.inorder() + " " + val + " " + right.inorder() + ")";
        }
        String postorder() {
            return ((left != null ? left.postorder() : "") + " " + (right != null ? right.postorder() : "") + " " + val).trim().replaceAll("\\s+", " ");
        }
    }

    static class Graph {
        private final Map<String, List<String>> adj = new LinkedHashMap<>();
        void link(String src, String rel, String dst) {
            adj.computeIfAbsent(src, k -> new ArrayList<>()).add("--(" + rel + ")--> [" + dst + "]");
            adj.computeIfAbsent(dst, k -> new ArrayList<>());
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            adj.forEach((k, edges) -> {
                sb.append("[").append(k).append("]\n");
                if (edges.isEmpty()) sb.append("   └─ (Terminal Node)\n");
                else edges.forEach(e -> sb.append("   └─ ").append(e).append("\n"));
            });
            return sb.toString();
        }
    }

    // UI & FORMATTING
    private JTextArea[] createFourViews() {
        JTextArea[] areas = new JTextArea[4];
        for (int i = 0; i < 4; i++) {
            areas[i] = new JTextArea();
            areas[i].setFont(new Font("Monospaced", Font.PLAIN, 13));
            areas[i].setEditable(false);
            areas[i].setMargin(new Insets(10, 10, 10, 10));
        }
        return areas;
    }

    private JPanel assembleTab(String title, Component[] controls, JTextArea[] views) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridLayout(3, 2, 10, 5));
        top.setBorder(BorderFactory.createTitledBorder(title));
        for (Component c : controls) top.add(c);

        JTabbedPane subTabs = new JTabbedPane();
        String[] names = {"1. Explanation", "2. Truth Table & Equivalence", "3. Trees & Traversals", "4. Knowledge Graph"};
        for (int i = 0; i < 4; i++) subTabs.addTab(names[i], new JScrollPane(views[i]));

        panel.add(top, BorderLayout.NORTH);
        panel.add(subTabs, BorderLayout.CENTER);
        return panel;
    }

    private String b(boolean val) { return val ? "T" : "F"; }
    private String status(boolean pass, String failReason) { return pass ? "PASS (True)" : "FAIL (" + failReason + ")"; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DecisionForgeApp().setVisible(true));
    }
}
