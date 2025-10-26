package g1t1.scenes;

import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;

import java.util.List;

public class ReportsViewController extends PageController {
    @Override
    public void onMount() {
        List<ClassSession> pastSessions = AuthenticationContext.getCurrentUser().getPastSessions();
        
    }
}
