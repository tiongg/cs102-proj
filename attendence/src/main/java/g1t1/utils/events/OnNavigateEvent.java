package g1t1.utils.events;

import g1t1.models.scenes.Page;

public class OnNavigateEvent {
    private final Page newPage;

    public OnNavigateEvent(Page newPage) {
        this.newPage = newPage;
    }

    public Page getNewPage() {
        return this.newPage;
    }
}
