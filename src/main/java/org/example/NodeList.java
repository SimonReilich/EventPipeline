package org.example;

import org.example.nodes.Group;
import org.example.nodes.Node;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeList extends ArrayList<Node> {

    @Override
    public boolean add(Node node) {
        Set<String> deps = node.requires().stream()
                .filter(t -> t.length() > 1)
                .collect(Collectors.toSet());

        int i = 0;
        while (!deps.isEmpty() && i < this.size()) {
            i++;
            deps.remove(this.get(i - 1).getOutput());
        }
        this.add(i, node);
        this.subList(0, i).forEach(n -> {
            if (n.requires().contains(node.getOutput()) && !node.requires().contains(n.getOutput())) {
                this.remove(n);
                this.add(n);
            }
        });
        if (node instanceof Group) {
            ((Group) node).init();
        }
        return true;
    }
}