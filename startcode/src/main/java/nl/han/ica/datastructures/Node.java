package nl.han.ica.datastructures;

public class Node<T> {
    T data;
    Node<T> next;

    Node(T d) {
        data = d;
        next = null;
    }
}
