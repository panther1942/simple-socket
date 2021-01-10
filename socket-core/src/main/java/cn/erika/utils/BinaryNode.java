package cn.erika.utils;

import java.util.LinkedList;
import java.util.List;

public class BinaryNode<T extends Comparable<T>> {
    private BinaryNode<T> parent;
    private BinaryNode<T> left;
    private BinaryNode<T> right;
    private int folder = 0;
    private int depth = 0;
    private T value;

    public BinaryNode() {
        this.folder = 0;
        this.depth = 0;
    }

    private BinaryNode(BinaryNode<T> parent) {
        this.parent = parent;
    }

    public int getFolder() {
        return folder;
    }

    public int getDepth() {
        return depth;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void addNode(T value) {
        if (this.value == null) {
            this.value = value;
        } else if (this.value.compareTo(value) > 0) {
            if (this.left != null) {
                this.left.addNode(value);
            } else {
                this.left = new BinaryNode<>(this);
                this.left.setValue(value);
            }
        } else {
            if (this.right != null) {
                this.right.addNode(value);
            } else {
                this.right = new BinaryNode<>(this);
                this.right.setValue(value);
            }
        }
    }

    public List<T> firstOrder() {
        List<T> result = new LinkedList<>();
        result.add(this.value);
        if (left != null) {
            result.addAll(left.firstOrder());
        }
        if (right != null) {
            result.addAll(right.firstOrder());
        }
        return result;
    }

    private void firstOrder(NodeHandler handler) {
        handler.handler(this);
        if (left != null) {
            left.firstOrder(handler);
        }
        if (right != null) {
            right.firstOrder(handler);
        }
    }

    public List<T> middleOrder() {
        List<T> result = new LinkedList<>();
        if (left != null) {
            result.addAll(left.middleOrder());
        }
        result.add(this.value);
        if (right != null) {
            result.addAll(right.middleOrder());
        }
        return result;
    }

    private void middleOrder(NodeHandler handler) {
        handler.handler(this);
        if (left != null) {
            left.middleOrder(handler);
        }
        if (right != null) {
            right.middleOrder(handler);
        }
    }

    public List<T> lastOrder() {
        List<T> result = new LinkedList<>();
        if (left != null) {
            result.addAll(left.lastOrder());
        }
        if (right != null) {
            result.addAll(right.lastOrder());
        }
        result.add(this.value);
        return result;
    }

    private void lastOrder(NodeHandler handler) {
        handler.handler(this);
        if (left != null) {
            left.lastOrder(handler);
        }
        if (right != null) {
            right.lastOrder(handler);
        }
    }

    private BinaryNode<T> leftHand() {
        if (this.right == null) {
            return this;
        }
        BinaryNode b = this.right;
        BinaryNode c = this.right.left;
        swapParent(b);
        this.right = c;
        b.left = this;
        if (c != null) {
            c.parent = this;
        }
        return b;
    }

    private BinaryNode<T> rightHand() {
        if (this.left == null) {
            return this;
        }
        BinaryNode b = this.left;
        BinaryNode c = this.left.right;
        swapParent(b);
        this.left = c;
        b.right = this;
        if (c != null) {
            c.parent = this;
        }
        return b;
    }

    private void swapParent(BinaryNode<T> node) {
        BinaryNode<T> parent = this.parent;
        if (parent != null) {
            if (this.equals(parent.left)) {
                parent.left = node;
            } else {
                parent.right = node;
            }
        }
        node.parent = parent;
        this.parent = node;
        flush();
    }

    private BinaryNode<T> swapRight() {
        BinaryNode<T> b = this.right;
        BinaryNode<T> c = this.right.left;
        swapParent(b);
        c.left = this;
        this.right = null;
        return b;
    }

    private void flush() {
        lastOrder(node -> node.folder = node.flushFolder(0));
        lastOrder(node -> node.depth = node.flushDepth(0));
    }

    private int flushFolder(int depth) {
        if (parent != null) {
            return parent.flushFolder(depth + 1);
        } else {
            return depth;
        }
    }

    private int flushDepth(int init) {
        int leftFolder = init;
        int rightFolder = init;
        if (left != null) {
            leftFolder = left.flushDepth(init + 1);
        }
        if (right != null) {
            rightFolder = right.flushDepth(init + 1);
        }
        return leftFolder > rightFolder ? leftFolder : rightFolder;
    }

    public BinaryNode<T> getRoot() {
        if (parent != null) {
            return parent.getRoot();
        } else {
            return this;
        }
    }

    private interface NodeHandler {
        void handler(BinaryNode node);
    }

    public void balance() {
        flush();
        BinaryNode top = this;
        if (top.left == null && top.right == null) {
            return;
        } else if (top.left == null) {
            top = swapRight();
            top.balance();
        }

        if (top.depth > 1 && top.right == null) {
            top = rightHand();
            top.balance();
        } else if (top.right != null) {
            int depthDiff = top.left.depth - top.right.depth;
            if (depthDiff > 1) {
                top = rightHand();
                top.balance();
            } else if (depthDiff < -1) {
                top = leftHand();
                top.balance();
            } else {
                top.left.balance();
                top.right.balance();
            }
        }
    }
}
