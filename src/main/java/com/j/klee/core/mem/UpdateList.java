package com.j.klee.core.mem;

import com.j.klee.expr.Expr;

public class UpdateList {
    public Array root;

    public UpdateNode head;

    public UpdateList() {
    }

    public UpdateList(Array root, UpdateNode head) {
        this.root = root;
        this.head = head;
    }

    public UpdateList(UpdateList b) {
        this.root = b.root;
        this.head = b.head;
    }

    public void extend(Expr index, Expr value) {
        if (root != null) {
            assert (root.domain == index.getWidth());
            assert (root.range == value.getWidth());
        }
        head = new UpdateNode(head, index, value);
    }

    public long hash() {
        return 0;
    }

    public static class UpdateNode {
        public long hashValue;
        public UpdateNode next;
        public Expr index, value;
        public int size;

        public UpdateNode(UpdateNode next, Expr index, Expr value) {
            this.next = next;
            this.index = index;
            this.value = value;
            computeHash();
            size = next == null ? 1 : next.size + 1;
        }

        /* TODO: compare, but do we need that? */

        public void computeHash() {
            hashValue = index.hash() ^ value.hash();
            if (next != null) {
                hashValue ^= next.hashValue;
            }
        }
    }

    public static class Array {
        public String name;

        // TODO: what's size used for?
        public Expr size;

        public Expr.Width domain, range;

        // List<ConstantExpr> constantValues; // TODO: do we need this?

        public Array(String name, Expr size, Expr.Width domain, Expr.Width range) {
            this.name = name;
            this.size = size;
            this.domain = domain;
            this.range = range;
        }

        public Array(String name, Expr size) {
            this(name, size, Expr.Width.Int32, Expr.Width.Int8);
        }
    }
}