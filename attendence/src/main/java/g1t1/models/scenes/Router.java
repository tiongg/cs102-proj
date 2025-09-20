package g1t1.models.scenes;

import g1t1.utils.EventEmitter;
import g1t1.utils.events.OnNavigateEvent;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

public class Router {
    public static final HashMap<PageName, Page> scenes = new HashMap<>();
    public static final EventEmitter<Object> emitter = new EventEmitter<>();
    private static Page currentPage;

    public static void initialize() {
        Map<PageName, String> scenePaths = Map.ofEntries(
                entry(PageName.Login, "scenes/LoginView.fxml"),
                entry(PageName.Register, "scenes/RegisterView.fxml"),
                entry(PageName.PastRecords, "scenes/ReportsView.fxml"),
                entry(PageName.MyClasses, "scenes/MyClassesView.fxml"),
                entry(PageName.Onboard, "scenes/OnboardView.fxml"),
                entry(PageName.StartSession, "scenes/StartSessionView.fxml"),
                entry(PageName.Settings, "scenes/SettingsView.fxml")
        );

        scenePaths.forEach((key, value) -> {
            try {
                Page page = new Page(key, value);
                scenes.put(key, page);
            } catch (Exception e) {
                System.err.println("Error loading " + key + " from " + value);
                e.printStackTrace();
            }
        });
    }

    public static void changePage(PageName newPageName) {
        Page page = scenes.get(newPageName);
        currentPage = page;
        emitter.emit(new OnNavigateEvent(page));
        page.getController().onMount();
    }

    public static Page getCurrentPage() {
        return currentPage;
    }
}
