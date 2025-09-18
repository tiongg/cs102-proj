package g1t1.components;

import g1t1.App;
import g1t1.models.scenes.Page;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

public class NavItem extends HBox {
    private final StringProperty iconLiteralProperty = new SimpleStringProperty();
    private final ObjectProperty<Page> targetPageProperty = new SimpleObjectProperty<>();

    @FXML
    private Label lblNavTitle;
    @FXML
    private FontIcon icon;

    public NavItem() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("NavItem.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        iconLiteralProperty.addListener(e -> {
            icon.setIconLiteral(((StringProperty) e).getValue());
        });
        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNavClick(MouseEvent event) {
        App.changePage(getTargetPage());
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

    public Page getTargetPage() {
        return targetPageProperty().get();
    }

    public void setTargetPage(Page targetPage) {
        targetPageProperty().set(targetPage);
    }

    public ObjectProperty<Page> targetPageProperty() {
        return this.targetPageProperty;
    }
}
