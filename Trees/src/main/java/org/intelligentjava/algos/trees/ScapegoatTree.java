package org.intelligentjava.algos.trees;

import java.util.ArrayList;
import java.util.List;

import org.intelligentjava.algos.trees.utils.MathUtils;

/**
 * Scapegoat tree non recursive implementation.
 * Warning: not sure if my implementations is really correct, didn't have time to learn more about scapegoat trees.
 * 
 * @author Ignas Lelys
 * @created Jul 28, 2011
 * 
 */
public class ScapegoatTree extends AbstractSelfBalancingBinarySearchTree {

    /** Alpha parameter. */
    private static double alpha = 0.5;
    
    private int maxSize = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node createNode(int value, Node parent, Node left, Node right) {
        return new Node(value, parent, left, right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node insert(int element) {
        Node inserted = super.insert(element);
        int height = getNodeHeight(inserted);
        if (height > getHAlpha()) {
            Node scapegoat = findScapegoatNode(inserted);
            Node scapegoatParent = scapegoat.parent;
            boolean scapegoatOnParentsLeft = scapegoatParent != null && scapegoatParent.left == scapegoat;
            Node rebuiltSubtree = rebuildTree(getSubtreeSize(scapegoat), scapegoat);
            rebuiltSubtree.parent = scapegoatParent;
            if (scapegoatParent != null) {
                if (scapegoatOnParentsLeft) {
                    scapegoatParent.left = rebuiltSubtree;
                } else {
                    scapegoatParent.right = rebuiltSubtree;
                }
            }
            if (scapegoat == root) {
                root = rebuiltSubtree;
            }
            maxSize = getSize();
        }
        return inserted;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Node delete(int element) {
        Node deletedNode = super.delete(element);
        if (getSize() < alpha * maxSize) {
            rebuildTree(getSize(), root);
        }
        return deletedNode;
    }
    
    /**
     * Finds scapegoat node which is used for rebalancing the tree.
     * 
     * @return Scapegoat node.
     */
    protected Node findScapegoatNode(Node node) {
        int size = 1;
        int height = 0;
        int totalSize = 0;
        while (node.parent != null) {
            height++;
            totalSize = 1 + size + getSubtreeSize(getSibling(node));
            if (height > Math.floor(MathUtils.logarithm(1 / alpha, totalSize))) {
                return node.parent;
            }
            node = node.parent;
            size = totalSize;
        }
        return null;
    }

    /**
     * Rebuilds unbalanced tree.
     * Found this implementation much clearer and easier to make it work: https://github.com/satchamo/Scapegoat-Tree/blob/master/scapegoat.py
     * Could't get implementations from pdfs to work.
     * 
     * @param size Size of subtree.
     * @param scapegoat Scapegoat is the root of subtree of {@link size} number of nodes.
     * 
     * @return Balanced subtree.
     */
    protected Node rebuildTree(int size, Node scapegoat) {
        List<Node> nodes = new ArrayList<Node>();
        flattenTree(scapegoat, nodes);
        return buildTree(nodes, 0, size - 1);
    }
    
    /**
     * Build balanced tree from flattened tree.
     */
    private Node buildTree(List<Node> nodes, int start, int end) {
        int middle = (int)Math.ceil(((double)(start + end)) / 2.0);
        if (start > end) {
            return null;
        }
        // middle becomes root of subtree instead of scapegoat
        Node node = nodes.get(middle);
        
        // recursively get left and right nodes
        Node leftNode = buildTree(nodes, start, middle - 1);
        node.left = leftNode;
        if (leftNode != null) {
            leftNode.parent = node;
        }
        Node rightNode = buildTree(nodes, middle + 1, end);
        node.right = rightNode;
        if (rightNode != null) {
            rightNode.parent = node;
        }
        return node;
    }
    
    /**
     * Flatten tree function. Put whole subtree into list of nodes.
     */
    private void flattenTree(Node subtreeRoot, List<Node> nodes) {
        if (subtreeRoot == null) {
            return;
        }
        flattenTree(subtreeRoot.left, nodes);
        nodes.add(subtreeRoot);
        flattenTree(subtreeRoot.right, nodes);
    }

    /**
     * @return Node's sibling.
     */
    // TODO move to AbstractBinaySearchTree and use in other trees where needed.
    private Node getSibling(Node node) {
        if (node.parent != null) {
            if (node.parent.left == node) {
                return node.parent.right;
            } else {
                return node.parent.left;
            }
        }
        return null;
    }
    
    /**
     * Calculate size of subtree.
     * 
     * @param node
     *            Subtree root node.
     * @return Number of elements in the subtree.
     */
    // TODO move to AbstractBinaySearchTree
    protected int getSubtreeSize(Node node) {
        if (node == null) {
            return 0;
        }
        if (node.isLeaf()) {
            return 1;
        } else {
            int sum = 1;
            sum += getSubtreeSize(node.left);
            sum += getSubtreeSize(node.right);
            return sum;
        }
    }
    
    // TODO move to AbstractBinaySearchTree
    protected int getNodeHeight(Node node) {
        if (node == null) {
            return -1;
        } else if (node.parent == null) {
            return 0;
        } else {
            return getNodeHeight(node.parent) + 1;
        }
    }

    private int getHAlpha() {
        return (int)Math.floor(MathUtils.logarithm(1 / alpha, (double)getSize()));
    }

}