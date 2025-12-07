package com.uql;

import java.util.LinkedList;
import java.util.Objects;

public class Node {
    String type;
    String value;
    LinkedList<Node> children = new LinkedList<>();

    public Node(String type) {
        this.type = type;
    }

    public Node(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public Node addLast(Node node) {
        children.addLast(node);
        return this;
    }

    public Node addFirst(Node node) {
        children.addFirst(node);
        return this;
    }

    public String treeString(int level) {
        StringBuilder builder = new StringBuilder("Node[" + type + " = '" + value + "']");
        if (!children.isEmpty()) {
            level++;
            builder.append("\n");
            for (int i = 0; i < children.size(); i++) {
                builder.append("   ".repeat(level - 1))
                        .append("└─ ")
                        .append(children.get(i).treeString(level));
                if (i != children.size() - 1) {
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(type, node.type) && Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
