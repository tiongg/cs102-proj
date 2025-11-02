package g1t1.models.scenes;

public abstract class PageController<T> {
    protected T props;

    /**
     * Called when the page is swapped to
     */
    public void onMount() {
    }

    public final void onMount(Object props) {
        this.props = (T) props;
        this.onMount();
    }

    /**
     * Called when a page is swapped off
     */
    public void onUnmount() {

    }
}
