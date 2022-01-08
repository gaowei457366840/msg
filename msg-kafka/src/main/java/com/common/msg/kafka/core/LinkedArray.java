package com.common.msg.kafka.core;

import java.util.Iterator;
import java.util.List;

import lombok.NonNull;


public class LinkedArray<T>
        implements Iterable<T> {
    private Node<T> current;
    private Node<T> first;
    private Node<T> last;
    private int size;

    public LinkedArray() {
    }

    public LinkedArray(List<T> list) {
        for (T t : list) {
            add(t);
        }
    }

    public synchronized void add(T key) {
        if (this.last == null) {
            this.last = new Node<>(key, null, null);
            this.last.setNext(this.last);
            this.first = this.last;
            this.current = this.first;
        } else {
            Node<T> x = new Node<>(key, this.last, this.first);
            this.last.setNext(x);
            this.last = x;
        }
        this.size++;
    }

    public synchronized T next() {
        if (this.current == null) return null;
        T t = this.current.key;
        this.current = this.current.getNext();
        return t;
    }

    public int size() {
        return this.size;
    }

    public synchronized void remove(T key) {
        Node<T> node = this.first;
        int count = 0;
        while (count < this.size) {
            if (node.getKey().equals(key)) {
                if (this.size == 1) {
                    node.setNext(null);
                    node.setPrevious(null);
                    this.current = null;
                    this.first = null;
                    this.last = null;
                } else if (node.getPrevious() != null) {
                    node.getPrevious().setNext(node.getNext());
                    if (node.getNext().getPrevious() != null) {
                        node.getNext().setPrevious(node.getPrevious());
                    }
                } else {
                    this.last.setNext(node.next);
                    node.getNext().setPrevious(null);
                    this.first = node.getNext();
                }

                if (node.equals(this.current)) {
                    this.current = node.getNext();
                } else if (node.equals(this.last)) {
                    this.last = node.getPrevious();
                }

                this.size--;
                return;
            }
            node = node.getNext();
            count++;
        }
    }

    public synchronized void clear() {
        this.size = 0;
        this.current = null;
        this.first = null;
        this.last = null;
    }

    public boolean isEmpty() {
        return (this.size == 0);
    }


    @NonNull
    public Iterator<T> iterator() {
        return new Iter(this.first);
    }

    private static class Node<T> {
        private Node<T> next;
        private Node<T> prev;
        private T key;

        Node(T key, Node<T> prev, Node<T> next) {
            this.key = key;
            this.prev = prev;
            this.next = next;
        }

        Node<T> getNext() {
            return this.next;
        }

        void setNext(Node<T> next) {
            this.next = next;
        }

        void setPrevious(Node<T> prev) {
            this.prev = prev;
        }

        public void setKey(T key) {
            this.key = key;
        }

        Node<T> getPrevious() {
            return this.prev;
        }

        public T getKey() {
            return this.key;
        }


        public String toString() {
            return this.key.toString();
        }
    }

    class Iter
            implements Iterator<T> {
        private Node<T> node;
        private int count = 0;

        Iter(Node<T> node) {
            this.node = node;
        }


        public boolean hasNext() {
            return (this.count < LinkedArray.this.size);
        }


        public T next() {
            Node<T> oldNode = this.node;
            this.node = oldNode.getNext();
            this.count++;
            return oldNode.getKey();
        }

        public void remove() {
        }
    }
}


