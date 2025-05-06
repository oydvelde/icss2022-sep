package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T> {
    private IHANLinkedList<T> linkedList;

    public HANStack() {
        this.linkedList = new HANLinkedList<>();
    }

    @Override
    public void push(T value) {
        linkedList.addFirst(value);
    }

    @Override
    public T pop() {
        if (linkedList.getFirst() == null) return null;

        T value = linkedList.getFirst();
        linkedList.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        if (linkedList.getFirst() == null) return null;

        return linkedList.getFirst();
    }
}
