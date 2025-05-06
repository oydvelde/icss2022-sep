package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private Node<T> head;
    private int size;

    @Override
    public void addFirst(T value) {
        Node<T> node = new Node<>(value);
        node.next = head;
        head = node;
        size++;
    }

    @Override
    public void clear() {
        head = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        if (index < 0 || index >= size){
            System.out.println("Index out of bounds.");
        }

        Node<T> temp = head;

        for (int i = 0; i < index; i++) {
            temp = temp.next;
        }
        Node<T> node = new Node<>(value);
        node.next = temp.next;
        temp.next = node;
    }

    @Override
    public void delete(int pos) {
        if (pos < 0 || pos >= size){
            System.out.println("Position out of bounds.");
        }

        Node<T> temp = head;

        if (pos == 0){
            removeFirst();
            return;
        }

        for (int i = 0; i < pos; i++) {
            temp = temp.next;
        }
        temp.next = temp.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size){
            System.out.println("Position out of bounds.");
            return null;
        }

       Node<T> node = head;
        for(int i = 0; i < pos; i++){
            node = node.next;
        }
        return node.data;
    }

    @Override
    public void removeFirst() {
        if (head != null) {
            head = head.next;
            size--;
        }
    }

    @Override
    public T getFirst() {
        if (head == null) return null;

        return head.data;
    }

    @Override
    public int getSize() {
        return size;
    }
}