package trainix.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import trainix.service.RoutineGeneratorService;

import java.util.List;

@Route("routines")
public class RoutineView extends VerticalLayout {

    private final RoutineGeneratorService generator;
    private final TextField goalField   = new TextField("🎯 What's your goal?");
    private final Grid<String> stepsGrid = new Grid<>();

    @Autowired
    public RoutineView(RoutineGeneratorService generator) {
        this.generator = generator;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("🔄 Routine Generator"));
        goalField.setWidth("400px");
        Button generateBtn = new Button("🧠 Generate Routine");
        add(goalField, generateBtn);

        stepsGrid.addColumn(step -> step).setHeader("Step").setAutoWidth(true);
        add(stepsGrid);

        generateBtn.addClickListener(e -> onGenerate());
    }

    private void onGenerate() {
        String goal = goalField.getValue();
        if (goal == null || goal.isBlank()) {
            Notification.show("Please enter a goal first", 2000, Notification.Position.TOP_CENTER);
            return;
        }
        List<String> steps = generator.generateRoutine(goal);
        if (steps.isEmpty()) {
            Notification.show("Couldn’t generate a routine. Try rephrasing your goal.", 2000, Notification.Position.TOP_CENTER);
        } else {
            stepsGrid.setItems(steps);
        }
    }
}
