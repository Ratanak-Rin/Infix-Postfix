import java.util.Iterator;

public interface Tree<E> extends Iterable<E> {
    Position<E> root();
    Position<E> parent(Position<E> p) throws IllegalArgumentException;
    Iterable<Position<E>> children(Position<E> p)
                                      throws IllegalArgumentException;

    int numChildren(Position<E> p)    throws IllegalArgumentException;
    boolean isInternal(Position<E> p) throws IllegalArgumentException;
    boolean isExternal(Position<E> p) throws IllegalArgumentException;
    boolean isRoot(Position<E> p)     throws IllegalArgumentException;
    
    int size();
    boolean isEmpty();
    Iterator<E> iterator();            //iterator for elements
    Iterable<Position<E>> positions(); //iterable collection of positions

    //tree traversal
    public Iterable<Position<E>> preorder();
    public Iterable<Position<E>> postorder();
    public Iterable<Position<E>> breadthFirst();
}
