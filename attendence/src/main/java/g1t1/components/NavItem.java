package g1t1.components;

import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.utils.events.routing.OnNavigateEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class NavItem extends HBox {
    private final StringProperty iconLiteralProperty = new SimpleStringProperty();
    // Needs to be a string property if not scene builder throws a tantrum
    private final StringProperty targetPageProperty = new SimpleStringProperty();

    @FXML
    private Label lblNavTitle;
    @FXML
    private FontIcon icon;

    public NavItem() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("NavItem.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        iconLiteralProperty.addListener((observable, oldValue, newValue) -> {
            icon.setIconLiteral(newValue);
        });

        Router.emitter.subscribe(OnNavigateEvent.class, (e) -> {
            if (e.newPage().getPageName() == this.getTargetPage()) {
                selected();
            } else {
                unselected();
            }
        });

        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNavClick(MouseEvent event) {
        Router.changePage(getTargetPage());
    }

    private void selected() {
        this.lblNavTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        this.lblNavTitle.setUnderline(true);
    }

    private void unselected() {
        // Reset style
        this.lblNavTitle.setStyle("");
        this.lblNavTitle.setUnderline(false);
    }

    public String getText() {
        return textProperty().get();
    }

    public void setText(String value) {
        textProperty().set(value);
    }

    public StringProperty textProperty() {
        return this.lblNavTitle.textProperty();
    }

    public String getIconLiteral() {
        return iconLiteralProperty().get();
    }

    public void setIconLiteral(String value) {
        iconLiteralProperty().set(value);
    }

    public StringProperty iconLiteralProperty() {
        return this.iconLiteralProperty;
    }

    public PageName getTargetPage() {
        return PageName.valueOf(targetPageProperty().get());
    }

    public void setTargetPage(PageName targetPageName) {
        targetPageProperty().set(targetPageName.toString());
    }

    public StringProperty targetPageProperty() {
        return this.targetPageProperty;
    }
}
